addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.7.7")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.10",
  "io.circe" %% "circe-yaml" % "0.14.1"
)
