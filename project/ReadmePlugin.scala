import sbt.AutoPlugin

import sbt.Def
import sbt._
import sbtghactions.GenerativeKeys._
import sbt.Keys.crossScalaVersions

object ReadmePlugin extends AutoPlugin {

  val readmeGenerate = taskKey[String]("Generate README.md")
  val readmeWrite = taskKey[Unit]("Write README.md")
  val readmeCheck = taskKey[Unit]("Check the contents of README.md")

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    readmeGenerate := Def.task {
      val template = IO.read(file("README.md"))

      def pattern(inside: String) = s"""<!-- SCALA VERSIONS START -->$inside<!-- SCALA VERSIONS END -->"""

      val versionsGrouped = crossScalaVersions.value.groupBy {
        case s if s.startsWith("2.13") => "2.13"
        case s if s.startsWith("2.12") => "2.12"
        case s                         => "3"
      }

      val versionsString = List("2.12", "2.13", "3")
        .map { prefix =>
          "- " + versionsGrouped(prefix).mkString(", ")
        }
        .mkString("\n")

      pattern("(?s)(.+)").r.replaceAllIn(template, pattern(s"\n$versionsString\n"))
    }.value,
    readmeWrite := Def.task {
      IO.write(file("README.md"), readmeGenerate.value)
    }.value,
    readmeCheck := Def.task {
      val expected = IO.read(file("README.md")).trim
      val actual = readmeGenerate.value.trim
      if (expected != actual)
        sys.error(s"""README.md mismatch! Expected:
                     |$expected
                     |Actual:
                     |$actual
                     |
                     |Run `sbt readmeWrite` and commit the result to try again.""".stripMargin)
    }.value
  )

}
