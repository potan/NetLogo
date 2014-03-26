scalaVersion := "2.10.3"

name := "NetLogo"

onLoadMessage := ""

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalacOptions ++=
//  "-deprecation -unchecked -Xfatal-warnings -Xcheckinit -encoding us-ascii -optimize -Yinline-warnings -feature"
  "-unchecked -Xcheckinit -encoding us-ascii -Yinline-warnings -feature"
  .split(" ").toSeq

javacOptions ++=
//  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.7 -target 1.7"
  "-g -encoding us-ascii -source 1.7 -target 1.7"
  .format(java.io.File.pathSeparator)
  .split(" ").toSeq

// only log problems plz
ivyLoggingLevel := UpdateLogging.Quiet

// this makes jar-building and script-writing easier
retrieveManaged := true

// we're not cross-building for different Scala versions
crossPaths := false

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "tools")

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

mainClass in (Compile, run) := Some("org.nlogo.app.App")

mainClass in (Compile, packageBin) := Some("org.nlogo.app.App")

sourceGenerators in Compile <+= EventsGenerator.task

sourceGenerators in Compile <+= JFlexRunner.task

resourceGenerators in Compile <+= I18n.resourceGeneratorTask

Extensions.extensionsTask

InfoTab.infoTabTask

ModelIndex.modelIndexTask

NativeLibs.nativeLibsTask

Depend.dependTask

threed := { System.setProperty("org.nlogo.is3d", "true") }

nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }

moduleConfigurations += ModuleConfiguration("javax.media", JavaNet2Repository)

//resolvers += MavenRepository("jogamp", "http://jogamp.org/deployment/maven")

libraryDependencies ++= Seq(
  "asm" % "asm-all"  % "3.3.1",
  "org.picocontainer" % "picocontainer"  % "latest.integration" /* % "2.13.6"*/,
  "log4j" % "log4j"  % "latest.integration" /* % "1.2.16"*/,
  "javax.media" % "jmf"  % "latest.integration" /* % "2.1.1e"*/,
  "org.pegdown" % "pegdown"  % "latest.integration" /* % "1.1.0"*/,
  "org.parboiled" % "parboiled-java"  % "latest.integration" /* % "1.0.2"*/,
//  "steveroy" % "mrjadapter"  % "1.2" from "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar",
//  "org.jhotdraw" % "jhotdraw"  % "6.0b1" from "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar",
//  "ch.randelshofer" % "quaqua"  % "7.3.4" from "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar",
  "org.devzendo" % "Quaqua" % "7.3.4",
//  "ch.randelshofer" % "swing-layout"  % "7.3.4" from "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar",
//  "org.jogl" % "jogl"  % "1.1.1", // from "http://ccl.northwestern.edu/devel/jogl-1.1.1.jar",
  "net.java.dev.jogl" % "jogl" % "1.1.1-rc6",
//  "org.gluegen-rt" % "gluegen-rt"  % "1.1.1" from "http://ccl.northwestern.edu/devel/gluegen-rt-1.1.1.jar",
  "org.jmock" % "jmock" % "latest.integration" /* "2.5.1" */ % "test",
  "org.jmock" % "jmock-legacy" % "latest.integration" /* % "2.5.1" */ % "test",
  "org.jmock" % "jmock-junit4" % "latest.integration" /* % "2.5.1" */ % "test",
  "org.scalacheck" %% "scalacheck" % "latest.integration" /* % "1.10.0" */ % "test",
  "org.scalatest" %% "scalatest" % "latest.integration" /* "1.8" */ % "test",
  "org.apache.httpcomponents" % "httpclient" % "latest.integration" /*"4.2"*/,
  "org.apache.httpcomponents" % "httpmime" % "latest.integration" /*"4.2"*/,
  "com.googlecode.json-simple" % "json-simple" % "latest.integration" /*"1.1.1"*/
)

all <<= (baseDirectory, streams) map { (base, s) =>
  s.log.info("making resources/system/dict.txt and docs/dict folder")
  IO.delete(base / "docs" / "dict")
  Process("python bin/dictsplit.py").!!
}

all <<= all.dependsOn(
  packageBin in Test,
  Extensions.extensions,
  NativeLibs.nativeLibs,
  ModelIndex.modelIndex,
  InfoTab.infoTab,
  Scaladoc.docSmaller)
