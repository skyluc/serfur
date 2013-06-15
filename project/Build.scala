import sbt._
import Keys._

object ProjectBuild extends Build {

  val baseSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.skyluc",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.2"
  )

  lazy val project = Project ("serfur", file("."), settings = baseSettings)

}
