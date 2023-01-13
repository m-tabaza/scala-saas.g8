package $project_name_camel_case$.core.users

import $project_name_camel_case$.prelude.{*, given}

enum Gender {
  case Male, Female
}
object Gender {

  lazy val stringMap = Gender.values.toList.flatMap { g =>
    (g.toString.toLowerCase, g) :: (g.toString, g) :: Nil
  }.toMap

  given Order[Gender] = Order.by(_.ordinal)

  given Meta[Gender] = deriveMeta.deriveEnumMeta

  given Codec[Gender] = deriveJson.deriveEnumCodec

  given HttpSchema[Gender] = deriveHttp.deriveEnumSchema
    

}
