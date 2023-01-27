package $package$.http

import $package$.prelude.{*, given}
import $package$.http.prelude.{*, given}

class DefaultRoutes[F[_]: Async] extends HttpRoute[F] {

  override val spec = DefaultRoutes.healthCheckSpec

  override val logic = routeInterpreter[F].toRoutes {
    DefaultRoutes.healthCheckSpec.serverLogic { _ =>
      ().asRight.pure
    }
  }

}
object DefaultRoutes {

  val healthCheckSpec =
    endpoint.get
      .in("health_check")
      .out(statusCode(StatusCode.Ok))
      .description("Returns 200 OK if the server is running")

}
