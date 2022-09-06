inThisBuild(
  List(
    organization := "org.polyvariant",
    homepage := Some(url("https://github.com/polyvariant/better-tostring")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://blog.kubukoz.com")
      ),
      Developer(
        "majk-p",
        "Michał Pawlik",
        "admin@michalp.net",
        url("https://michalp.net")
      )
    ),
    versionScheme := Some("early-semver")
  )
)

val GraalVM11 = "graalvm-ce-java11@20.3.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

// for dottydoc
ThisBuild / resolvers += Resolver.JCenterRepository

ThisBuild / scalaVersion := "3.0.0"
ThisBuild / crossScalaVersions := IO.read(file("scala-versions")).split("\n").map(_.trim)

ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Branch("main")),
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

ThisBuild / githubWorkflowGeneratedCI ~= {
  _.map {
    case job if job.id == "build" =>
      job.copy(
        steps = job.steps.map {
          case step: WorkflowStep.Sbt if step.name == Some("Check that workflows are up to date") =>
            step.copy(commands = List("githubWorkflowCheck", "mergifyCheck", "readmeCheck"))
          case step                                                                               => step
        }
      )
    case job                      => job
  }
}

val commonSettings = Seq(
  scalacOptions --= Seq("-Xfatal-warnings", "-source", "future"),
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
)

val plugin = project.settings(
  name := "better-tostring",
  commonSettings,
  crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
  crossVersion := CrossVersion.full,
  libraryDependencies ++= Seq(
    scalaOrganization.value % (
      if (scalaVersion.value.startsWith("3"))
        s"scala3-compiler_3"
      else "scala-compiler"
    ) % scalaVersion.value
  ),
  Compile / unmanagedSourceDirectories ++= {
    val extraDirectoriesWithPredicates = Map[String, String => Boolean](
      ("scala-3.0.x", (_.startsWith("3.0"))),
      ("scala-3.1.x", (_.startsWith("3.1"))),
      ("scala-3.2.x", (_.startsWith("3.2")))
    )

    extraDirectoriesWithPredicates.collect {
      case (dir, predicate) if predicate(scalaVersion.value) =>
        sourceDirectory.value / "main" / dir
    }.toList
  }
)

val tests = project
  .settings(
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
    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "0.7.26" % Test),
    buildInfoKeys ++= Seq(scalaVersion),
    buildInfoPackage := "b2s.buildinfo"
  )
  .enablePlugins(BuildInfoPlugin)

val betterToString =
  project
    .in(file("."))
    .settings(name := "root")
    .settings(
      commonSettings,
      (publish / skip) := true,
      addCommandAlias("generateAll", List("githubWorkflowGenerate", "mergifyWrite", "readmeWrite").mkString(";"))
    )
    .aggregate(plugin, tests)
    .enablePlugins(MergifyPlugin)
    .enablePlugins(ReadmePlugin)
