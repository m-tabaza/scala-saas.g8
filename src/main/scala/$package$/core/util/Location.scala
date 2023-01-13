package $project_name_camel_case$.core.util

import $project_name_camel_case$.prelude.{*, given}
import org.postgresql.geometric.PGpoint

case class Location(lat: Double, long: Double)
object Location {

  given Meta[Location] =
    Meta[PGpoint].imap { p =>
      Location(lat = p.x, long = p.y)
    } { l =>
      PGpoint(l.lat, l.long)
    }

  given Codec[Location] = deriveJson.deriveCodec

  given HttpSchema[Location] = HttpSchema.derived

}
