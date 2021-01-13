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

val commonSettings = Seq(
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq(
    "2.12.10",
    "2.12.11",
    "2.12.12",
    "2.12.13",
    //
    "2.13.1",
    "2.13.2",
    "2.13.3",
    "2.13.4"
  ),
  scalacOptions -= "-Xfatal-warnings"
)

val plugin = project.settings(
  name := "better-tostring",
  commonSettings,
  crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
  crossVersion := CrossVersion.full,
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "3.2.3" % Test
  )
)

val examples = project.settings(
  skip in publish := true,
  commonSettings,
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
  project.in(file(".")).settings(name := "root").settings(commonSettings, skip in publish := true).dependsOn(plugin).aggregate(plugin)
