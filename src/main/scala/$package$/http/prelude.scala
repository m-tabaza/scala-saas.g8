package $package$.http.prelude

import cats.ApplicativeError
import cats.implicits.*
import cats.effect.Async
import sttp.tapir.Tapir
import sttp.tapir.generic.auto.SchemaDerivation
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.json.circe.TapirJsonCirce
import $package$.core.users.auth.AuthAlgebra
import $package$.core.users.auth.UserJwtPayload

export org.http4s.HttpRoutes
export org.http4s.dsl.Http4sDsl as HttpDsl

export sttp.model.StatusCode
export sttp.tapir.Endpoint

export routeUtil.*
object routeUtil
    extends Tapir
    with OpenAPIDocsInterpreter
    with SchemaDerivation
    with TapirJsonCirce

def routeInterpreter[F[_]: Async] =
  sttp.tapir.server.http4s.Http4sServerInterpreter[F]()

lazy val userJwtInput =
  routeUtil.auth.bearer[String]().group("User Access Token")

trait HttpRoute[F[_]] extends HttpDsl[F] {

  val spec: sttp.tapir.AnyEndpoint

  val logic: org.http4s.HttpRoutes[F]

  def decodeUserJwt[F[_]](jwt: String)(using
      F: ApplicativeError[F, Throwable],
      auth: AuthAlgebra[F]
  ) = auth
    .decodeUserJwt(jwt)
    .attempt
    .map(_.leftMap(_ => ()))

}

case class PlainErrorOutput(label: String, message: String)
object PlainErrorOutput {
  given io.circe.Codec[PlainErrorOutput] = io.circe.generic.semiauto.deriveCodec
  given sttp.tapir.Schema[PlainErrorOutput] = sttp.tapir.Schema.derived
}
