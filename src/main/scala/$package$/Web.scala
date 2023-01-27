package $package$

import cats.effect.*
import doobie.util.transactor.Transactor
import org.http4s.server.Router
import $package$.config.Env
import $package$.core.$project_name_pascal_case$Algebra
import $package$.core.$project_name_pascal_case$Impl
import $package$.http.prelude.*
import $package$.prelude.{*, given}
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.*
import sttp.tapir.swagger.SwaggerUI

type F[A] = IO[A]

def impl = for {
  given Env <- Resource.eval(Env.load[F])
  given config.Postgres <- Resource.eval(config.Postgres.load[F])
  given config.AppSecret <- Resource.eval(config.AppSecret.load[F])
  given config.Messagebird <- Resource.eval(config.Messagebird.load[F])
  given Transactor[F] <- {
    val pg = summon[config.Postgres]
    http.ServerUtil.hikariTransactor[F](
      host = pg.host,
      dbName = pg.db,
      pgUser = pg.user,
      pgPassword = pg.password
    )
  }
} yield $project_name_pascal_case$Impl[F]

object Web extends IOApp.Simple {

  val server = impl.flatMap { impl =>
    given $project_name_pascal_case$Algebra[F] = impl

    val routes = List(
      http.DefaultRoutes[F] :: Nil,
      http.users.UserRoutes[F]
    ).flatten

    val docs = OpenAPIDocsInterpreter().toOpenAPI(
      routes.map(_.spec),
      "$project_name_pascal_case$ API",
      "1.0.0"
    )

    val swaggerRoutes =
      routeInterpreter[F].toRoutes(SwaggerUI[F](docs.toYaml))

    val httpApp = Router(
      "/" -> (routes.map(_.logic).reduce(_ <+> _) <+> swaggerRoutes)
    ).orNotFound

    http.ServerUtil.serverResource[F]("0.0.0.0", 4000, httpApp)
  }

  override def run = server.useForever.as(ExitCode.Success)

}
