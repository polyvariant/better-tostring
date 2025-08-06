import sbt.AutoPlugin

import sbt.Def
import sbt._

object AddVersionPlugin extends AutoPlugin {

  val addVersion = inputKey[Unit]("Write the given version to scala-versions")

  case class ParsedVersion(mainPart: String, rcPart: Option[String]) {

    def render: String =
      rcPart match {
        case Some(rc) => s"$mainPart-$rc"
        case None     => mainPart
      }

  }

  object ParsedVersion {

    // if mainPart is the same, RCs go first
    implicit val ordering: Ordering[ParsedVersion] = Ordering.by { v =>
      (
        v.mainPart,
        v.rcPart match {
          case Some(rc) => s"0$rc" // prepend 0 to RCs to sort them before non-RCs
          case None     => "1" // non-RCs come after RCs
        }
      )
    }

    def parse(version: String): ParsedVersion = {
      val parts = version.split("-")
      val mainPart = parts.head
      val rcPart = if (parts.length > 1) Some(parts(1)) else None
      ParsedVersion(mainPart, rcPart)
    }

  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    addVersion := {
      // Read the version from the input
      val version = Def.spaceDelimited("<version>").parsed.mkString(" ")

      val path = file("scala-versions")
      val currentVersions = IO
        .read(path)
        .split("\n")
        .map(_.trim)
        .filterNot(_.isEmpty)
        .toSet

      val newVersions = (currentVersions + version).toSeq.map(ParsedVersion.parse).sorted.map(_.render)
      IO.write(path, newVersions.mkString("\n") + "\n")
    }
  )

}
