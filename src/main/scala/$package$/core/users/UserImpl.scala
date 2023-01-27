package $package$.core.users

import $package$.config
import $package$.prelude.{*, given}

import auth.AuthImpl
import sms.SMSImpl
import $package$.core.users.auth.UserJwtPayload

class UserImpl[F[_]: Sync](using xa: Transactor[F])(using
    config.Messagebird,
    config.AppSecret
) extends UserAlgebra[F] {
  import UserAlgebra.*
  import UserImpl.*

  override val auth = AuthImpl[F]
  override val sms = SMSImpl[F]

  override def createUser(
      phone: PhoneNumber,
      name: String,
      birthDate: LocalDate,
      gender: Gender,
      password: String
  ) = getUserIdByPhone(phone).option.transact(xa).flatMap {
    case Some(_) => CreateUserResult.UserAlreadyExists.pure
    case None => {
      for {
        hashedPassword <- auth.hashPassword(password)
        mutations <- xa.trans.apply {
          for {
            userId <- insertUser(
              phone = phone,
              name = name,
              birthDate = birthDate,
              gender = gender,
              hashedPassword = hashedPassword
            ).unique

            otpDate <- getUserOtp(userId).option.flatMap {
              case None =>
                deleteUserOtp(userId).run *>
                  insertUserOtp(userId).unique.tupleLeft(true)
              case Some(data) => (false, data).pure
            }
            (shouldSendOtp, (otp, expiresAt)) = otpDate
          } yield (userId, otp, shouldSendOtp, expiresAt)
        }
        (userId, otp, shouldSendOtp, expiresAt) = mutations

        sendOtpAttempt <-
          if shouldSendOtp then
            sms
              .send(to = phone, message = s"Your verification code is \$otp")
              .attempt
          else ().asRight.pure
      } yield sendOtpAttempt match {
        case Left(err) => CreateUserResult.FailedToSendOTP(err.getMessage)
        case Right(_)  => CreateUserResult.Success(userId)
      }
    }
  }

  override def verifyUser(phone: PhoneNumber, otp: Short) = for {
    now <- time.utcNow
    result <- xa.trans.apply {
      for {
        userId <- getUserIdByPhone(phone).option
        otpData <- userId.flatTraverse(getUserOtp(_).option)
        result <- (userId, otpData).bisequence.fold(
          VerifyUserResult.ExpiredOTP.pure[ConnectionIO]
        ) { case (userId, (storedOtp, expiresAt)) =>
          if otp != storedOtp then
            VerifyUserResult.IncorrectOTP.pure[ConnectionIO]
          else if expiresAt.isBefore(now) then
            VerifyUserResult.ExpiredOTP.pure[ConnectionIO]
          else setUserVerified(userId).run.as(VerifyUserResult.Success)
        }
      } yield result
    }
  } yield result

  override def login(phone: PhoneNumber, password: String) = for {
    foundUser <- getUserLoginInfoByPhone(phone).option.transact(xa)

    result <- foundUser match {
      case Some((userId, hashedPassword, true)) =>
        for {
          passwordIsValid <- auth.verifyPassword(
            plain = password,
            hashed = hashedPassword
          )
          jwt <- auth.encodeUserJwt(UserJwtPayload(userId))
        } yield
          if passwordIsValid then LoginResult.LoginSuccessful(userId, jwt)
          else LoginResult.LoginCredentialsInvalid
      case Some((_, _, false)) => LoginResult.UserNotVerified.pure
      case None                => LoginResult.LoginCredentialsInvalid.pure
    }
  } yield result

}
object UserImpl {

  def getUserIdByPhone(phone: PhoneNumber): Query[UserId] =
    sql"""
    | SELECT id FROM users WHERE phone = \$phone
    """.stripMargin.query

  type UserIsVerified = Boolean
  type HashedPassword = String

  def getUserLoginInfoByPhone(
      phone: PhoneNumber
  ): Query[(UserId, HashedPassword, UserIsVerified)] =
    sql"""
    | SELECT id, hashed_password, (verified_at IS NOT NULL) FROM users
    | WHERE phone = \$phone
    """.stripMargin.query

  def insertUser(
      phone: PhoneNumber,
      name: String,
      birthDate: LocalDate,
      gender: Gender,
      hashedPassword: String
  ): Query[UserId] =
    sql"""
    | INSERT INTO users 
    | (phone, name, birth_date, gender, hashed_password)
    | VALUES (\$phone, \$name, \$birthDate, \$gender, \$hashedPassword)
    | ON CONFLICT (phone) DO NOTHING
    | RETURNING id
    """.stripMargin.query

  def getUserOtp(userId: UserId): Query[(Short, Instant)] =
    sql"""
    | SELECT value, expires_at
    | FROM user_otps
    | WHERE user_id = \$userId
    |   AND expires_at > (NOW() AT TIME ZONE 'utc')
    """.stripMargin.query

  def deleteUserOtp(userId: UserId): Update =
    sql"DELETE FROM user_otps WHERE user_id = \$userId".update

  def insertUserOtp(userId: UserId): Query[(Short, Instant)] =
    sql"""
    | INSERT INTO user_otps (user_id)
    | VALUES (\$userId)
    | RETURNING value, expires_at
    """.stripMargin.query

  def setUserVerified(userId: UserId): Update =
    sql"""
    | DELETE FROM user_otps
    | WHERE user_id = \$userId;
    |
    | UPDATE users SET verified_at = (NOW() AT TIME ZONE 'utc')
    | WHERE id = \$userId
    """.stripMargin.update

}
