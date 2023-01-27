package $package$.http.users

import $package$.core.users.PhoneNumber
import $package$.core.users.UserAlgebra
import $package$.core.users.UserAlgebra.VerifyUserResult
import $package$.http.prelude.{_, given}
import $package$.prelude.{_, given}

class VerifyUserRoute[F[_]: Async](using users: UserAlgebra[F])
    extends HttpRoute[F] {

  override val spec = VerifyUserRoute.spec

  override val logic = routeInterpreter[F].toRoutes {
    VerifyUserRoute.spec.serverLogic { input =>
      users.verifyUser(phone = input.phone, otp = input.otp).map(_.asRight)
    }
  }

}
object VerifyUserRoute {

  case class VerifyUserInput(phone: PhoneNumber, otp: Short)
  object VerifyUserInput {
    given Codec[VerifyUserInput] = deriveJson.deriveCodec
    given HttpSchema[VerifyUserInput] = HttpSchema.derived
  }

  val spec = endpoint.post
    .description("Marks the user verified if the OTP is valid")
    .in("user" / "verify")
    .in(jsonBody[VerifyUserInput])
    .out(jsonBody[VerifyUserResult])

}
