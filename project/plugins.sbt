addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.9")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-yaml" % "0.14.1"
)
