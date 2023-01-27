package $package$.core.users.auth

import $package$.prelude.{*, given}
import $package$.core.users.UserId

case class UserJwtPayload(userId: UserId)

object UserJwtPayload {

  given Codec[UserJwtPayload] = deriveJson.deriveCodec

}
