name := "tic-tac-toe-backend"
version := "0.0"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val baseSettings = Seq(
  scalaVersion := "2.13.3",
  resolvers ++= Dependencies.additionalResolvers,
  libraryDependencies ++= Dependencies.all,
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  scalacOptions ++= CompilerOpts.scalacOptions,
  parallelExecution in Test := false
)

lazy val `tic-tac-toe-backend` =
  project
    .in(file("."))
    .settings(baseSettings: _*)