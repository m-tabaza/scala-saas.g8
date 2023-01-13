package $project_name_camel_case$.prelude

object deriveHttp {

  inline def deriveEnumSchema[T] =
    HttpSchema.derivedEnumeration[T].defaultStringBased

}

given Encoder[Uri] = Encoder[String].contramap(_.renderString)

given Decoder[Uri] =
  Decoder[String].emap(Uri.fromString(_).leftMap(_.getMessage))

given HttpSchema[Uri] = HttpSchema.string.map {
  case s: String => Uri.fromString(s).toOption
  case _         => None
}(_.renderString)
