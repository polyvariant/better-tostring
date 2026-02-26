ThisBuild / tlBaseVersion := "0.3"
ThisBuild / organization := "org.polyvariant"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2020)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("kubukoz", "Jakub Kozłowski"),
  tlGitHubDev("majk-p", "Michał Pawlik")
)
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / tlFatalWarnings := false

Global / onChangedBuildSource := ReloadOnSourceChanges

// for dottydoc
ThisBuild / resolvers += Resolver.JCenterRepository

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / crossScalaVersions := IO.read(file("scala-versions")).split("\n").map(_.trim)

ThisBuild / githubWorkflowEnv ++= List(
  "PGP_PASSPHRASE",
  "PGP_SECRET",
  "SONATYPE_PASSWORD",
  "SONATYPE_USERNAME"
).map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

ThisBuild / githubWorkflowPublishTargetBranches := List(RefPredicate.StartsWith(Ref.Tag("v")))

// Scala 3.8+ requires JDK 17, but we can't switch individual jobs' versions yet (https://github.com/typelevel/sbt-typelevel/issues/851)
// so we just go all-in
ThisBuild / tlJdkRelease := Some(17)

ThisBuild / githubWorkflowGeneratedCI ~= {
  _.map {
    case job if job.id == "build" =>
      job.withSteps(
        job.steps.map {
          case step: WorkflowStep.Sbt if step.name == Some("Check that workflows are up to date") =>
            step.withCommands(List("githubWorkflowCheck", "readmeCheck"))
          case step                                                                               => step
        }
      )
    case job                      => job
  }
}

val commonSettings = Seq(
  scalacOptions --= Seq("-source:3.0-migration"),
  mimaPreviousArtifacts := Set.empty,
  // We don't need KP and bm4
  libraryDependencies ~= (_.filterNot(_.name == "kind-projector").filterNot(_.name == "better-monadic-for"))
)

val plugin = project
  .settings(
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
    )
  )
  .enablePlugins(BackpublishPlugin)

val tests = project
  .settings(
    commonSettings,
    scalacOptions ++= {
      val jar = (plugin / Compile / packageBin).value
      Seq(
        s"-Xplugin:${jar.getAbsolutePath}",
        s"-Xplugin-require:better-tostring",
        s"-Jdummy=${jar.lastModified}"
      ) // borrowed from bm4
    },
    libraryDependencies ++= Seq("org.scalameta" %% "munit" % "1.2.3" % Test),
    buildInfoKeys ++= Seq(scalaVersion),
    buildInfoPackage := "b2s.buildinfo",
    Compile / doc / sources := Seq()
  )
  .enablePlugins(BuildInfoPlugin, NoPublishPlugin)

val betterToString =
  project
    .in(file("."))
    .settings(name := "root")
    .settings(
      commonSettings,
      (publish / skip) := true,
      addCommandAlias("generateAll", List("githubWorkflowGenerate", "mergifyGenerate", "readmeWrite").mkString(";"))
    )
    .aggregate(plugin, tests)
    .enablePlugins(NoPublishPlugin)
    .enablePlugins(ReadmePlugin)
    .enablePlugins(AddVersionPlugin)
