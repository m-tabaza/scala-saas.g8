package $package$.core.users

import $package$.prelude.{*, given}
import $package$.core.util.TaggedUUID
import $package$.core.util.TaggedUUIDCompanion

case class UserId(override val uuid: UUID) extends TaggedUUID["user"](uuid)
object UserId extends TaggedUUIDCompanion["user", UserId] {
  override def fromUUID(uuid: UUID) = UserId(uuid)
}
