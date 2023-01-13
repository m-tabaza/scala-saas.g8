package $project_name_camel_case$.http.users

import $project_name_camel_case$.core.users.PhoneNumber
import $project_name_camel_case$.core.users.UserAlgebra
import $project_name_camel_case$.core.users.UserAlgebra.LoginResult
import $project_name_camel_case$.core.users.UserId
import $project_name_camel_case$.http.prelude.*
import $project_name_camel_case$.prelude.{*, given}

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
