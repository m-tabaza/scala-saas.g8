package $project_name_camel_case$.core.users

import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.core.util.TaggedUUID
import $project_name_camel_case$.core.util.TaggedUUIDCompanion

case class UserId(override val uuid: UUID) extends TaggedUUID["user"](uuid)
object UserId extends TaggedUUIDCompanion["user", UserId] {
  override def fromUUID(uuid: UUID) = UserId(uuid)
}
