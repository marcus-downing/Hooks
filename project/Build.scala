import sbt._
import Keys._

object HooksBuild extends Build {
  lazy val root = Project("root", file(".")) settings(
    organization := "cc.minotaur",

    name := "hooks",

    version := "0.1",
    
    
    //  dependencies
    libraryDependencies += "org.scalatest" % "scalatest_2.9.0" % "1.6.1",

    libraryDependencies += "org.clapper" %% "classutil" % "0.4.3",

    //libraryDependencies += "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7"

    
    //  build settings
    scalaVersion := "2.9.0",
    
    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    crossScalaVersions := Seq(
      //"2.8.0", 
      //"2.8.1", 
      "2.9.0", 
      //"2.9.0-1",
      "2.9.1"
      //"2.9.1-1"
      ),
    
    initialCommands in console := "import hooks._",
    
    
    //  publishing
    //resolvers ++= List(
    //  ScalaToolsSnapshots,
    //  "Sonatype snapshots" at sonatypeSnapshots,
    //  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
    //),
    
    
    publishMavenStyle := true,

    publishTo <<= (version) { version: String =>
      val sonatypeSnapshots = "http://oss.sonatype.org/content/repositories/snapshots/"
      val sonatypeStaging = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  
      if (version.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at sonatypeSnapshots)
      else
        Some("staging" at sonatypeStaging)
    },
    
    publishArtifact in Test := false,
    
    pomIncludeRepository := { _ => false },
    
    credentials += Credentials(file("/home/mdowning/.gnupg/secring.gpg")),
    

    pomExtra := <xml:group>
      <url>http://www.minotaur.cc/hooks.html</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:git://github.com/marcusatbang/hooks.git</connection>
        <developerConnection>scm:git:git@github.com:marcusatbang/hook.git</developerConnection>
        <url>git@github.com:marcusatbang/hooks.git</url>
      </scm>
      <developers>
        <developer>
          <id>marcusatbang</id>
          <name>Marcus Downing</name>
          <email>marcus@bang-on.net</email>
        </developer>
      </developers>
    </xml:group>
  )

  val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7"

}
