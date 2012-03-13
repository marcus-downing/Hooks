import sbt._
import Keys._

object Example1Build extends Build {
  lazy val root = Project("root", file(".")) settings(
    organization := "cc.minotaur",

    name := "hooks-example-ls",

    version := "0.1",

    libraryDependencies += "cc.minotaur" %% "hooks" % "0.1",
    
    libraryDependencies += "org.scalatest" % "scalatest_2.9.0" % "1.6.1",
    
    libraryDependencies += "commons-io" % "commons-io" % "2.1",

    scalaVersion := "2.9.1",
    
    crossScalaVersions := Seq("2.8.0", "2.8.1", "2.9.0", "2.9.1"),
    
    initialCommands in console := "import hooks._"
  )

  val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7"
}
