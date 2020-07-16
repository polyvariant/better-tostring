inThisBuild(
  List(
    organization := "com.kubukoz",
    homepage := Some(url("https://github.com/kubukoz/better-toString")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://kubukoz.com")
      )
    )
  )
)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.0"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.4"),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val commonSettings = Seq(
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.12.10", "2.12.11", "2.12.12", "2.13.1", "2.13.2", "2.13.3"),
  scalacOptions -= "-Xfatal-warnings"
)

val plugin = project.settings(
  name := "better-tostring",
  commonSettings,
  crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
  crossVersion := CrossVersion.full,
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "3.1.0" % Test
  ) ++ compilerPlugins
)

def macroAnnotationsFlags(scalaVersion: String) =
  if (scalaVersion.startsWith("2.13")) Seq("-Ymacro-annotations") else Seq()

def paradise(scalaVersion: String) =
  if (scalaVersion.startsWith("2.13"))
    Seq()
  else Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)))

val examples = project.settings(
  skip in publish := true,
  commonSettings,
  scalacOptions ++= macroAnnotationsFlags(scalaVersion.value),
  libraryDependencies ++= paradise(scalaVersion.value),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-tagless-macros" % "0.11"
  ),
  scalacOptions ++= {
    val jar = (plugin / Compile / packageBin).value
    Seq(
      s"-Xplugin:${jar.getAbsolutePath}",
      s"-Xplugin-require:better-tostring",
      s"-Jdummy=${jar.lastModified}"
    ) //borrowed from bm4
  }
)

val betterToString =
  project
    .in(file("."))
    .settings(name := "root")
    .settings(commonSettings, skip in publish := true)
    .dependsOn(plugin)
    .aggregate(plugin)
