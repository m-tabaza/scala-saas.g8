package $project_name_camel_case$.config

import fs2.io.file.Path
import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.config.Env

case class Messagebird(apiKey: String)
object Messagebird {

  given Decoder[Messagebird] = deriveJson.deriveDecoder

  def load[F[_]: Async](using env: Env) = env match {
    case Env.Dev =>
      Config.loadJsonFile[F, Messagebird] {
        Path("./config/messagebird.config.json")
      }
    case Env.Prod =>
      Config.getEnv("MESSAGEBIRD_API_KEY").map(Messagebird(_))
  }

}
