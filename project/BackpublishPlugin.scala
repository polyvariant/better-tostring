import sbt.Keys._

import sbt.AutoPlugin
import sbt.Def
import sbt.ThisBuild

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
