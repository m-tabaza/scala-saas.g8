package $package$.http.users

import $package$.core.users.Gender
import $package$.core.users.PhoneNumber
import $package$.core.users.UserAlgebra
import $package$.core.users.UserAlgebra.CreateUserResult
import $package$.http.prelude.*
import $package$.prelude.{_, given}
import $package$.core.users.UserId

class SignupRoute[F[_]: Async](using users: UserAlgebra[F])
    extends HttpRoute[F] {
  import SignupRoute.*

  override val spec = SignupRoute.spec

  override val logic = routeInterpreter[F].toRoutes {
    SignupRoute.spec.serverLogic { input =>
      users
        .createUser(
          phone = input.phone,
          name = input.name,
          birthDate = input.birthDate,
          gender = input.gender,
          password = input.password
        )
        .map {
          case CreateUserResult.Success(userId) =>
            SignupSuccessOutput(userId).asRight
          case CreateUserResult.FailedToSendOTP(msg) =>
            PlainErrorOutput("failed_to_send_otp", msg).asLeft
          case CreateUserResult.UserAlreadyExists =>
            PlainErrorOutput(
              "user_already_exists",
              "User with the same phone number already exists"
            ).asLeft
        }
    }
  }

}
object SignupRoute {

  case class SignupInput(
      name: String,
      password: String,
      phone: PhoneNumber,
      gender: Gender,
      birthDate: LocalDate
  )
  object SignupInput {
    given Codec[SignupInput] = deriveJson.deriveCodec
    given HttpSchema[SignupInput] = HttpSchema.derived
  }

  case class SignupSuccessOutput(userId: UserId)
  object SignupSuccessOutput {
    given Codec[SignupSuccessOutput] = deriveJson.deriveCodec
    given HttpSchema[SignupSuccessOutput] = HttpSchema.derived
  }

  val spec = endpoint.post
    .description("Creates a user awaiting verification and sends an OTP")
    .in("user" / "signup")
    .in(jsonBody[SignupInput])
    .out(jsonBody[SignupSuccessOutput])
    .errorOut(jsonBody[PlainErrorOutput] and statusCode(StatusCode.BadRequest))

}
