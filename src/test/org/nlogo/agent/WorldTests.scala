// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuiteLike
import org.nlogo.api.WorldDimensions

class WorldTests extends AbstractTestWorld with FunSuiteLike {

  val worldSquare = new WorldDimensions(-2, 2, -2, 2)
  val worldRectangle = new WorldDimensions(-3, 3, -2, 2)
  val turtles5 = Array(Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0))
  val turtles2 = Array(Array(0, 1), Array(0, -1))
  val link1 = Array(0, 1)

  override def makeWorld(dimensions: WorldDimensions) =
    new World() {
      createPatches(dimensions)
      realloc()
    }
  override def makeTurtle(world: World, cors: Array[Int]) =
    new Turtle(world, world.turtles(),
               cors(0).toDouble, cors(1).toDouble)
  override def makeLink(world: World, ends: Array[Int]) =
    new Link(world, world.getTurtle(ends(0)),
             world.getTurtle(ends(1)), world.links)
  test("IteratorSkipsDeadTurtles1_2D") {
    testIteratorSkipsDeadTurtles1(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles2_2D") {
    testIteratorSkipsDeadTurtles2(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles3_2D") {
    testIteratorSkipsDeadTurtles3(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles4_2D") {
    testIteratorSkipsDeadTurtles4(worldSquare, turtles5)
  }
  test("Shufflerator1_2D") {
    testShufflerator1(worldSquare, turtles5)
  }
  test("LinkDistance_2D") {
    testLinkDistance(worldSquare, turtles2, link1)
  }
  test("ShortestPath_2D") {
    testShortestPath(worldRectangle)
  }
  test("ShortestPathHorizontalCylinder") {
    val world = makeWorld(worldRectangle)
    world.changeTopology(false, true)
    expect(3.0)(world.topology.shortestPathY(2, -2))
    expect(-2.0)(world.topology.shortestPathX(2, -2))
  }
  test("ChangePublishedAfterWorldResize_2D") {
    testChangePublishedAfterWorldResize(worldSquare, worldRectangle)
  }
}
