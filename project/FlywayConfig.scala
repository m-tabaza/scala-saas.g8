object FlywayConfig {
  val dbHost = sys.env.get("POSTGRES_HOST").getOrElse("localhost")
  val dbName = sys.env.get("POSTGRES_DB").getOrElse("$project_cabab_case$-dev")
  val dbUser = sys.env.get("POSTGRES_USER").getOrElse("user")
  val dbPass = sys.env.get("POSTGRES_PASSWORD").getOrElse("pass")
  val dbUrl = s"jdbc:postgresql://$dbHost/$dbName"

  val cleanDisabled =
    sys.env.get("ENV").map(_.toLowerCase).exists(_ contains "prod")
}
