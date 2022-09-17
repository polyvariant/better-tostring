import sbt.Keys._

import sbt.AutoPlugin
import sbt.Def
import sbt.ThisBuild

/** Allows publishing an existing version for a new version of Scala. Usage:
  * {{{
  * BACKPUBLISH_VERSION=2.12.17 PROJECT_VERSION=0.3.17 sbt tlRelease
  * }}}
  */
object BackpublishPlugin extends AutoPlugin {

  override def projectSettings: Seq[Def.Setting[_]] =
    Option(System.getenv("BACKPUBLISH_VERSION")).toList.flatMap { backpublishVersion =>
      val projectVersion = Option(System.getenv("PROJECT_VERSION"))
        .getOrElse(sys.error("No PROJECT_VERSION provided"))

      println(
        s"Going to backpublish artifacts for Scala version $backpublishVersion, project version $projectVersion"
      )

      Seq(
        ThisBuild / version := projectVersion,
        ThisBuild / scalaVersion := backpublishVersion,
        ThisBuild / isSnapshot := false,
        ThisBuild / crossScalaVersions := Seq(backpublishVersion)
      )
    }

}
