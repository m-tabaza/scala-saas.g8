package $project_name_camel_case$.http.users

import $project_name_camel_case$.core.users.PhoneNumber
import $project_name_camel_case$.core.users.UserAlgebra
import $project_name_camel_case$.core.users.UserAlgebra.VerifyUserResult
import $project_name_camel_case$.http.prelude.{_, given}
import $project_name_camel_case$.prelude.{_, given}

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
