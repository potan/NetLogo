package org.nlogo.log

import
  scala.{ collection, concurrent },
    collection.mutable.ListBuffer,
    concurrent.{ Await, duration },
      duration._

import
  java.net.URL

import
  akka.{ actor, pattern, util },
    actor.{ Actor, ActorRef, PoisonPill, Props, ReceiveTimeout },
    pattern.ask,
    util.Timeout

/*

 The `LogDirector` class constitutes a set of three Akka actors that work together to manage a log and its periodic flushing

 LogDirector:
 -Handles external messages regarding:
 --Flushing the log buffer
 --Appending to the log buffer
 --Abruptly closing down the system
 --Properly closing down the system
 -Also handles internal messages to flush the buffer (coming from `LogFlushReminder`)
 -Transmits flushed log messages to preconfigured URLs (which are passed in at the time of object creation)

 LogBufferManager:
 -Keeps the log buffer in proper state
 -Handles messages from the Director pertaining to:
 --Adding to the buffer
 --Flushing all contents from the buffer
 ---When flushing, coalesces continuous series of items into the longest string(s) possible
 ----For example, if the length limit is '10':
      * Buffer:    ["apple", "ant", "artichoke", "ab", "a", "abcd", "abc", "abcde"]
      * Coalesced: [["apple", "ant"] (8), ["artichoke"] (9), ["ab", "a", "abcd", "abc"] (10), ["abcde"] (5)]

 LogFlushReminder (Optional):
 -Sends messages to `LogDirector` to tell it to empty itself.
 -Is only activated if `LogDirector` is in "continuous mode".

 */

// An actor controller; receives logging data and figures out what to do with it
class LogDirector(val mode: LogSendingMode, destinations: URL*) extends Actor {

  import LogManagementMessage._
  import LoggingServerMessage._

  require(destinations.size > 0)

  // Actors paired with their start conditions
  // This way, if an actor is added into this class, it is unlikely that the programmer will forget to start it
  private def newActor[T <: Actor : scala.reflect.ClassTag] : ActorRef = context.system.actorOf(Props[T])

  private val manager  = newActor[LogBufferManager]
  private val reminder = newActor[LogFlushReminder]
  private val director = self

  private val actorConditionTuple = List((manager, true), (reminder, mode == LogSendingMode.Continuous))
  actorConditionTuple foreach (x => if (!x._2) x._1 ! PoisonPill)

  import DirectorMessage._
  override def receive = {
    case Flush =>
      implicit val timeout = Timeout(2.seconds)
      transmitFormatted(Await.result(manager ? Read, timeout.duration).asInstanceOf[Seq[String]])

    case ToDirectorWrite(x) =>
      manager ! Write(x)

    case ToDirectorAbandon =>
      actorConditionTuple foreach (_._1 ! Abandon)
      abandonLog()
      sender ! FromDirectorClosed
      self ! PoisonPill

    case ToDirectorFinalize =>
      implicit val timeout = Timeout(10.seconds)
      actorConditionTuple map (_._1) filterNot (_ == manager) foreach (_ ! Finalize)
      transmitFormatted(Await.result(manager ? Finalize, timeout.duration).asInstanceOf[Seq[String]])
      finalizeLog()
      sender ! FromDirectorClosed
      self ! PoisonPill
  }

  private def abandonLog()  { transmit(ToServerAbandon.toString) }
  private def finalizeLog() { transmit(ToServerFinalize.toString) }

  private def transmitFormatted(messages: Seq[String]) {
    val msgListOpt = if (messages.filterNot(_.isEmpty).isEmpty) None else Some(messages)
    msgListOpt map (_ map (ToServerWrite(_).toString)) getOrElse (List(ToServerPulse.toString)) foreach transmit
  }

  private def transmit(message: String) {
    destinations foreach (LoggingServerHttpHandler.sendMessage(message, _))
  }


  /**************************************************
   *               End of outer class               *
   **************************************************/


  /*
  A concurrent wrapper around a queue-like string buffer
  Aside from telling it to close, there are two things that you can do with it:

    1) Tell it to write some data to the buffer
    2) Tell it to give you its contents and clear itself

  This allows a threadsafe way for the buffer to be regularly filled while being periodically read/emptied
   */
  private class LogBufferManager extends Actor {

    // Only change this constant if you have a veeeeery good reason; it has been specially tuned to this value on purpose --JAB (4/26/12)
    private val MessageCharLimit = 12000

    private val dataBuffer = new ListBuffer[String]()

    override def receive = {
      case Write(data) => write(data)
      case Read        => sender ! flushBuffer()
      case Finalize    => sender ! flushBuffer(); self ! PoisonPill
      case Abandon     => self ! PoisonPill
    }

    private def write(data: String) {
      dataBuffer += data
    }

    private def flushBuffer() : Seq[String] = {

      // Sequentially accumulates the passed-in strings into strings that are as large as possible
      // while still adhering to the restriction that `accumulatedStr.size < MessageCharLimit`
      // Note: Before anyone gets clever and tries to convert this into functional-style code... please don't!
      //       When functional, this code suffered from performance issues (in both speed and memory).
      //       I understand that I could have improved the code to be better in both regards, but I think it's
      //       preferable to be as fast as possible here--which means using an imperative style  --JAB (4/30/12)
      def condenseToLimitedStrs(inContents: Array[String]): Seq[String] = {

        val bigBuffer = new ListBuffer[String]()
        val littleBuffer = new ListBuffer[String]()

        var i = 0

        while (i < inContents.size) {

          var size = 0
          var curr = inContents(i)

          // Potential massive breakage of code if `curr` is, itself, more than `MessageCharLimit` in size
          while ((size + curr.size) <= MessageCharLimit && (i < inContents.size)) {
            littleBuffer += curr
            size += curr.size
            i += 1
            if (i < inContents.size) curr = inContents(i)
          }

          bigBuffer += littleBuffer.mkString
          littleBuffer.clear()

        }

        bigBuffer.toSeq

      }

      val bufferContents = dataBuffer.toArray
      dataBuffer.clear()
      condenseToLimitedStrs(bufferContents)

    }

  }

  // Essentially, a timer that reminds the Director to request buffer flushes from the LogBufferManager
  private class LogFlushReminder extends Actor {
    context.setReceiveTimeout(3.seconds)
    override def receive = {
      case ReceiveTimeout => director ! Flush
      case Finalize       => self ! PoisonPill
      case Abandon        => self ! PoisonPill
    }
  }

  // Establishing a message-set through which the logging actors can communicate with one another
  private[log] sealed trait LogManagementMessage

  private[log] object LogManagementMessage {
    case class  Write(data: String) extends LogManagementMessage
    case object Read extends LogManagementMessage
    case object Abandon extends LogManagementMessage
    case object Finalize extends LogManagementMessage
    case object Flush extends LogManagementMessage
  }

}


// The messaging protocol to be used by logging directors
sealed trait DirectorMessage

object DirectorMessage {
  case class ToDirectorWrite(data: String) extends DirectorMessage
  case object ToDirectorAbandon extends DirectorMessage
  case object ToDirectorFinalize extends DirectorMessage
  case object FromDirectorClosed extends DirectorMessage
}


// The messaging protocol to be used by the remote, log-receiving server
private[log] sealed trait LoggingServerMessage

private[log] object LoggingServerMessage {
  private val Sep = "|"
  case class ToServerWrite(data: String) { override def toString = "write%s%s".format(Sep, data) }
  case object ToServerPulse              { override def toString = "pulse" }
  case object ToServerAbandon            { override def toString = "abandon" }
  case object ToServerFinalize           { override def toString = "finalize" }
}


// An enumeration of modes that `LogDirector` can utilize
sealed trait LogSendingMode

object LogSendingMode {
  case object Continuous extends LogSendingMode
  case object AfterLoggingCompletes extends LogSendingMode
}

