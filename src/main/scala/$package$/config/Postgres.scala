package $package$.config

import $package$.prelude.{*, given}

import fs2.io.file.Path

case class Postgres(
    host: String,
    port: Int,
    db: String,
    user: String,
    password: String
)

object Postgres {

  given Decoder[Postgres] = deriveJson.deriveDecoder

  def load[F[_]: Async](using env: Env) = env match {
    case Env.Dev =>
      Config.loadJsonFile[F, Postgres](Path("./config/pg.config.json"))
    case Env.Prod =>
      for {
        host <- Config.getEnv("PG_HOST")
        port <- Config.getEnv("PG_PORT")
        db <- Config.getEnv("PG_DB")
        user <- Config.getEnv("PG_USER")
        pass <- Config.getEnv("PG_PASS")
      } yield Postgres(
        host = host,
        port = port.toInt,
        db = db,
        user = user,
        password = pass
      )
  }

}
