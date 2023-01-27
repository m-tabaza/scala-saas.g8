package $package$.config

import fs2.io.file.Path
import $package$.config.Env
import $package$.prelude.{*, given}

case class AppSecret(secret: String)
object AppSecret {

  given Decoder[AppSecret] = deriveJson.deriveDecoder

  def load[F[_]: Async](using env: Env) = env match {
    case Env.Dev =>
      Config.loadJsonFile[F, AppSecret] {
        Path("./config/appsecret.config.json")
      }
    case Env.Prod =>
      Config.getEnv("APP_SECRET").map(AppSecret(_))
  }

}
