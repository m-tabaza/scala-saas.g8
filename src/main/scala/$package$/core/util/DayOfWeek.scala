package $package$.core.util

import $package$.prelude.{*, given}

enum DayOfWeek {
  case Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
}
object DayOfWeek {

  given Meta[DayOfWeek] = deriveMeta.deriveEnumMeta

  given Codec[DayOfWeek] = deriveJson.deriveEnumCodec

  given HttpSchema[DayOfWeek] = deriveHttp.deriveEnumSchema

}
