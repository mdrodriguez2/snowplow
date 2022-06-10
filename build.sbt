name := "snowplow"
version := "0.1"
scalaVersion := "2.12.15"

val Http4sVersion              = "0.21.22"
val CirceVersion               = "0.13.0"
val ZIOVersion                 = "1.0.6"
val PureConfigVersion          = "0.17.1"
val ZIOInteropVersion          = "2.4.0.0"
val OrganizeImportsVersion     = "0.6.0"
val ScalaTestVersion           = "3.2.11"
val ZIOHttpVersion             = "1.0.0.0-RC19"
val JsonSchemaValidatorVersion = "5.1.0"

resolvers += "releases" at "https://nexus.corp.twilio.com/content/repositories/releases"

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"          %% "zio"              % ZIOVersion,
  "com.github.wi101" %% "embroidery"       % "0.1.1",
  "dev.zio"          %% "zio-interop-cats" % ZIOInteropVersion,
  "dev.zio"          %% "zio-test"         % ZIOVersion % "test",
  "dev.zio"          %% "zio-test-sbt"     % ZIOVersion % "test",
  // Http4s
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s" %% "http4s-circe"        % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  "org.http4s" %% "http4s-client"       % Http4sVersion,
  // Circe
  "io.circe" %% "circe-generic"        % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  "io.circe" %% "circe-parser"         % CirceVersion,
  //pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  //Schema validator
  "io.rest-assured" % "json-schema-validator" % JsonSchemaValidatorVersion,
  "io.rest-assured" % "json-schema-validator" % JsonSchemaValidatorVersion % Test,
  //testing
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
scalafmtOnCompile := true

// scalafix; run with `scalafixEnable` followed by `scalafixAll`
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % OrganizeImportsVersion

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
