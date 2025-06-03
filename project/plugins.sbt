addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.13",
  "io.circe" %% "circe-yaml" % "0.15.2"
)
