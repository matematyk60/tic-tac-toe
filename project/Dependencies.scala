import sbt._

//noinspection SpellCheckingInspection
object Dependencies {

  val simulacrumVersion       = "0.19.0"
  val scalaTestVersion        = "3.2.0"
  val scalaMockVersion        = "4.4.0"
  val fs2KafkaVersion         = "1.0.0"
  val http4sVersion           = "0.21.5"
  val circeVersion            = "0.13.0"
  val doobieVersion           = "0.9.0"
  val enumeratumDoobieVersion = "1.6.0"
  val enumeratumCirceVersion  = "1.6.1"
  val logbackVersion          = "1.2.3"

  private val miscDependencies = Seq(
    "com.github.mpilquist" %% "simulacrum" % simulacrumVersion
  )

  private val loggingDependencies = Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  private val testDependencies = Seq(
    "org.scalatest" %% "scalatest"        % scalaTestVersion % Test,
    "org.scalamock" %% "scalamock"        % scalaMockVersion % Test,
    "org.tpolecat"  %% "doobie-scalatest" % doobieVersion    % Test
  )

  private val http4sDependencies = Seq(
    "org.http4s" %% "http4s-dsl"          % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe"        % http4sVersion
  )

  private val circeDependencies = Seq(
    "io.circe" %% "circe-core"    % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-jawn"    % circeVersion,
    "io.circe" %% "circe-parser"  % circeVersion
  )

  private val fs2KafkaDependencies = Seq(
    "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion
  )

  private val doobieDependencies = Seq(
    "org.tpolecat" %% "doobie-core"     % doobieVersion,
    "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion
  )

  private val enumeratumDependencies = Seq(
    "com.beachape" %% "enumeratum-doobie" % enumeratumDoobieVersion,
    "com.beachape" %% "enumeratum-circe"  % enumeratumCirceVersion
  )

  val all: Seq[ModuleID] =
    miscDependencies ++
      testDependencies ++
      fs2KafkaDependencies ++
      http4sDependencies ++
      circeDependencies ++
      doobieDependencies ++
      enumeratumDependencies ++
      loggingDependencies

  val additionalResolvers: Seq[Resolver] =
    Seq(
      Resolver.jcenterRepo,
      Resolver.mavenCentral,
      "Typesafe Repo" at "https://repo.typesafe.com/typesafe/releases/"
    )

}
