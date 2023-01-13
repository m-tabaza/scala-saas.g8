package $project_name_camel_case$.core.users.auth

import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.core.users.UserId

case class UserJwtPayload(userId: UserId)

object UserJwtPayload {

  given Codec[UserJwtPayload] = deriveJson.deriveCodec

}
