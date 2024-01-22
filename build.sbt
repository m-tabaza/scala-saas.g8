ThisBuild / scalaVersion := "3.2.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "$project_name_camel_case$"
ThisBuild / organizationName := "$project_name_camel_case$"

lazy val root = (project in file("."))
  .enablePlugins(FlywayPlugin)
  .settings(
    name := "$project_name_cabab_case$",
    testFrameworks += new TestFramework("munit.Framework"),
    flywayUrl := FlywayConfig.dbUrl,
    flywayUser := FlywayConfig.dbUser,
    flywayPassword := FlywayConfig.dbPass,
    flywayLocations := Seq("filesystem:migrations/"),
    flywayBaselineOnMigrate := true,
    flywayCleanDisabled := FlywayConfig.cleanDisabled,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xmax-inlines",
      "500",
      "-Wconf:any:error",
      "-no-indent",
      "-rewrite"
    ),
    libraryDependencies ++= Seq(
      Deps.munit % Test,
      Deps.doobieMunit % Test,
      Deps.http4sBlazeServer,
      Deps.http4sDsl,
      Deps.logbackClassic,
      Deps.tapirHttp4sServer,
      Deps.tapirOpenapiDocs,
      Deps.tapirCirce,
      Deps.tapirSwaggerUi,
      Deps.doobieCore,
      Deps.doobieHikari,
      Deps.doobiePostgres,
      Deps.doobiePostgresCirce,
      Deps.sttpApiSpecCirceYaml,
      Deps.libPhoneNumber,
      Deps.jbcrypt,
      Deps.messagebird,
      Deps.jwtCirce
    )
  )
