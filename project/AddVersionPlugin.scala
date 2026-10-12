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

    // segments are compared numerically where possible, so that 3.10.0 sorts after 3.9.0 (and RC10 after RC9)
    private def segments(s: String): List[Either[BigInt, String]] =
      "[0-9]+|[^0-9.]+".r.findAllIn(s).map(seg => if (seg.forall(_.isDigit)) Left(BigInt(seg)) else Right(seg)).toList

    private implicit val segmentOrdering: Ordering[Either[BigInt, String]] = Ordering.by {
      case Left(n)  => (0, n, "")
      case Right(s) => (1, BigInt(0), s)
    }

    // if mainPart is the same, RCs go first
    implicit val ordering: Ordering[ParsedVersion] = {
      import scala.math.Ordering.Implicits._

      Ordering.by { (v: ParsedVersion) =>
        (
          segments(v.mainPart),
          v.rcPart match {
            case Some(rc) => (0, segments(rc)) // RCs come before non-RCs
            case None     => (1, Nil: List[Either[BigInt, String]])
          }
        )
      }
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
