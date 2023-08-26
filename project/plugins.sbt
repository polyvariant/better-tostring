addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.5.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.3",
  "io.circe" %% "circe-yaml" % "0.14.1"
)
