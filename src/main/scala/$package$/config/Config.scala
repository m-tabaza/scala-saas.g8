package $project_name_camel_case$.config

import $project_name_camel_case$.prelude.{*, given}

import fs2.io.file.Path

enum Env {
  case Dev, Prod
}
object Env {

  def load[F[_]: Async] = cats.effect.std.Env.make[F].get("ENV").map { envStr =>
    envStr.getOrElse("dev").toLowerCase match {
      case "prod" | "production" => Env.Prod
      case _                     => Env.Dev
    }
  }

}

object Config {

  def getEnv[F[_]: Async](key: String) =
    cats.effect.std.Env.make[F].get(key).flatMap {
      case Some(v) => v.pure
      case None =>
        Exception(s"Undefined environment variable `\$key`").raiseError
    }

  def loadJsonFile[F[_]: Async, C: Decoder](path: Path) = for {
    text <- fs2.io.file
      .Files[F]
      .readAll(path)
      .through(fs2.text.utf8.decode)
      .compile
      .string

    c <- Async[F].fromEither(io.circe.parser.decode[C](text))
  } yield c

}
