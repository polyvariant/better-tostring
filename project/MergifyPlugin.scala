import sbt.AutoPlugin

import sbt.Def
import sbt._
import _root_.io.circe
import cats.implicits._
import sbt.Keys.crossScalaVersions
import org.typelevel.sbt.gha.GenerativeKeys.githubWorkflowJavaVersions
import org.typelevel.sbt.gha.GenerativeKeys.githubWorkflowOSes

object MergifyPlugin extends AutoPlugin {

  val mergifyGenerate = taskKey[String]("Generate .mergify.yml")
  val mergifyWrite = taskKey[Unit]("Write .mergify.yml")
  val mergifyCheck = taskKey[Unit]("Check the contents of .mergify.yml")

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    mergifyGenerate := Def.task {
      import circe.syntax._

      val jobs = (githubWorkflowOSes.value.toList, crossScalaVersions.value.toList, githubWorkflowJavaVersions.value.toList).mapN {
        case (os, sv, java) =>
          s"Build and Test ($os, $sv, ${java.render})"
      }

      val mergify = circe
        .Json
        .obj(
          "pull_request_rules" := circe
            .Json
            .obj(
              "name" := "Automatically merge Scala Steward PRs on CI success",
              "conditions" :=
                "author=scala-steward" +:
                  "body~=labels:.*semver-patch.*" +:
                  jobs.map { job =>
                    s"""status-success="$job""""
                  },
              "actions" := circe
                .Json
                .obj(
                  "merge" := circe
                    .Json
                    .obj(
                      "method" := "merge"
                    )
                )
            ) :: Nil
        )

      circe.yaml.printer.print(mergify)
    }.value,
    mergifyWrite := Def.task {
      IO.write(file(".mergify.yml"), mergifyGenerate.value)
    }.value,
    mergifyCheck := Def.task {
      val expected = IO.read(file(".mergify.yml")).trim
      val actual = mergifyGenerate.value.trim
      if (expected != actual)
        sys.error(s""".mergify.yml mismatch! Expected:
                     |$expected
                     |Actual:
                     |$actual
                     |
                     |Run `sbt mergifyWrite` and commit the result to try again.""".stripMargin)
    }.value
  )

}
