package $package$.core.util

import $package$.prelude.{*, given}
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
