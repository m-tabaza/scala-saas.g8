package $project_name_camel_case$.core.users

import $project_name_camel_case$.prelude.{*, given}

import auth.AuthAlgebra
import sms.SMSAlgebra

trait UserAlgebra[F[_]] {
  import UserAlgebra.*

  def auth: AuthAlgebra[F]
  given AuthAlgebra[F] = auth

  def sms: SMSAlgebra[F]
  given SMSAlgebra[F] = sms

  def createUser(
      phone: PhoneNumber,
      name: String,
      birthDate: LocalDate,
      gender: Gender,
      password: String
  ): F[CreateUserResult]

  def verifyUser(phone: PhoneNumber, otp: Short): F[VerifyUserResult]

  def login(phone: PhoneNumber, password: String): F[LoginResult]

}
object UserAlgebra {

  enum CreateUserResult {
    case Success(userId: UserId)
    case FailedToSendOTP(message: String)
    case UserAlreadyExists
  }

  enum VerifyUserResult {
    case Success
    case IncorrectOTP
    case ExpiredOTP
  }
  object VerifyUserResult {

    given Codec[VerifyUserResult] = deriveJson.deriveEnumCodec

    given HttpSchema[VerifyUserResult] = deriveHttp.deriveEnumSchema

  }

  enum LoginResult {
    case LoginSuccessful(userId: UserId, jwt: String)
    case LoginCredentialsInvalid
    case UserNotVerified
  }

}
