package $project_name_camel_case$.prelude

import java.util.UUID as JUUID

type UUID = JUUID
object UUID {

  def fromString(s: String) =
    Either.catchNonFatal(JUUID.fromString(s)).toOption

  def random[F[_]](using F: Sync[F]) = F.delay(java.util.UUID.randomUUID)

}
