package $package$.http.users

import $package$.core.users.PhoneNumber
import $package$.core.users.UserAlgebra
import $package$.core.users.UserAlgebra.LoginResult
import $package$.core.users.UserId
import $package$.http.prelude.*
import $package$.prelude.{*, given}

class LoginRoute[F[_]: Async](using users: UserAlgebra[F])
    extends HttpRoute[F] {
  import LoginRoute.*

  override val spec = LoginRoute.spec

  override val logic = routeInterpreter[F].toRoutes {
    LoginRoute.spec.serverLogic { input =>
      users
        .login(phone = input.phone, password = input.password)
        .map {
          case LoginResult.LoginSuccessful(userId, jwt) =>
            LoginSuccessOutput(userId, jwt).asRight
          case LoginResult.UserNotVerified =>
            PlainErrorOutput("userNotVerified", "User is not verified").asLeft
          case LoginResult.LoginCredentialsInvalid =>
            PlainErrorOutput("invalidCredentials", "Invalid credentials").asLeft
        }
    }
  }

}
object LoginRoute {

  case class LoginInput(phone: PhoneNumber, password: String)
  object LoginInput {
    given Codec[LoginInput] = deriveJson.deriveCodec
    given HttpSchema[LoginInput] = HttpSchema.derived
  }

  case class LoginSuccessOutput(userId: UserId, jwt: String)
  object LoginSuccessOutput {
    given Codec[LoginSuccessOutput] = deriveJson.deriveCodec
    given HttpSchema[LoginSuccessOutput] = HttpSchema.derived
  }

  val spec = endpoint.post
    .description(
      "Returns an access token if credentials are valid and user is verified"
    )
    .in("user" / "login")
    .in(jsonBody[LoginInput])
    .out(jsonBody[LoginSuccessOutput])
    .errorOut(jsonBody[PlainErrorOutput] and statusCode(StatusCode.BadRequest))

}
