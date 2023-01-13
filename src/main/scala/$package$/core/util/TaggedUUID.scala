package $project_name_camel_case$.core.util

import $project_name_camel_case$.prelude.{*, given}

trait TaggedUUID[T <: String](val uuid: UUID)(using T: ValueOf[T]) {

  final override def toString = s"${T.value}_${uuid}"

}

trait TaggedUUIDCompanion[T <: String, TUUID <: TaggedUUID[T]](using
    T: ValueOf[T]
) {

  def fromUUID(uuid: UUID): TUUID

  final type Tag = T

  final def tag: Tag = T.value

  final def fromString(s: String) = s match {
    case s"${t}_${uuid}" if t == tag =>
      UUID.fromString(uuid).map(fromUUID)
    case _ => None
  }

  given Show[TUUID] = Show.fromToString

  given Encoder[TUUID] = tid => Json.fromString(tid.toString)

  given Decoder[TUUID] = Decoder[String].emap { s =>
    Either.fromOption(
      fromString(s),
      s"Expected a tagged UUID of `$tag`, but found `$s`"
    )
  }

  given Meta[TUUID] = {
    import doobie.postgres.implicits.*
    Meta[UUID].imap(fromUUID)(_.uuid)
  }

  given HttpSchema[TUUID] = HttpSchema.string.description("A tagged UUID")

  given HttpCodec.PlainCodec[TUUID] = HttpCodec.string.mapDecode { idStr =>
    fromString(idStr) match {
      case None => {
        val msg = s"Invalid $tag ID `$idStr`"
        HttpDecodeResult.Error(msg, Exception(msg))
      }
      case Some(tuuid) => HttpDecodeResult.Value(tuuid)
    }
  }(_.show)

}
