inThisBuild(
  List(
    organization := "com.kubukoz",
    homepage := Some(url("https://github.com/kubukoz/better-toString")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://kubukoz.com")
      ),
      Developer(
        "majk-p",
        "Michał Pawlik",
        "admin@michalp.net",
        url("https://michalp.net")
      )
    )
  )
)

val GraalVM11 = "graalvm-ce-java11@20.3.0"

ThisBuild / scalaVersion := "3.0.0-RC3"
ThisBuild / crossScalaVersions := Seq(
  "2.12.10",
  "2.12.11",
  "2.12.12",
  "2.12.13",
  //
  "2.13.1",
  "2.13.2",
  "2.13.3",
  "2.13.4",
  "2.13.5",
  //
  "3.0.0-M3",
  "3.0.0-RC1",
  "3.0.0-RC2",
  "3.0.0-RC3"
)

ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Branch("master")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(
  WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3"))
)
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List(
  "PGP_PASSPHRASE",
  "PGP_SECRET",
  "SONATYPE_PASSWORD",
  "SONATYPE_USERNAME"
).map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

val commonSettings = Seq(
  scalacOptions -= "-Xfatal-warnings"
)

def scalatestVersion(scalaVersion: String) =
  scalaVersion match {
    case "3.0.0-M3"  => "3.2.3"
    case "3.0.0-RC1" => "3.2.5"
    case "3.0.0-RC2" => "2.3.7"
    case _           => "3.2.8"
  }

val plugin = project.settings(
  name := "better-tostring",
  commonSettings,
  crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
  crossVersion := CrossVersion.full,
  libraryDependencies ++= Seq(
    scalaOrganization.value % (
      if (scalaVersion.value.startsWith("3"))
        s"scala3-compiler_${scalaVersion.value}"
      else "scala-compiler"
    ) % scalaVersion.value
  )
)

val tests = project.settings(
  (publish / skip) := true,
  commonSettings,
  scalacOptions ++= {
    val jar = (plugin / Compile / packageBin).value
    Seq(
      s"-Xplugin:${jar.getAbsolutePath}",
      s"-Xplugin-require:better-tostring",
      s"-Jdummy=${jar.lastModified}"
    ) //borrowed from bm4
  },
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion(scalaVersion.value) % Test
  )
)

val betterToString =
  project
    .in(file("."))
    .settings(name := "root")
    .settings(commonSettings, (publish / skip) := true)
    .aggregate(plugin, tests)
