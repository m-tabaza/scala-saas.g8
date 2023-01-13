package $project_name_camel_case$.core.util

import $project_name_camel_case$.prelude.{*, given}

enum DayOfWeek {
  case Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
}
object DayOfWeek {

  given Meta[DayOfWeek] = deriveMeta.deriveEnumMeta

  given Codec[DayOfWeek] = deriveJson.deriveEnumCodec

  given HttpSchema[DayOfWeek] = deriveHttp.deriveEnumSchema

}
