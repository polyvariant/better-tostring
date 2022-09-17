addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.4.13")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-yaml" % "0.14.1"
)
