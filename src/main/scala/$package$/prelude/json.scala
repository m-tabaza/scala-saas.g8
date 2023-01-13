package $project_name_camel_case$.prelude

import io.circe.*

import scala.compiletime.summonAll
import scala.deriving.Mirror

object deriveJson {

  export io.circe.generic.semiauto.{deriveDecoder, deriveEncoder, deriveCodec}

  inline def deriveEnumDecoder[T](using m: Mirror.SumOf[T]): Decoder[T] = {
    val elemInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[T]]]
        .map(_.value)

    val elemNames =
      summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[String]]]
        .map(_.value)

    val mapping = (elemNames zip elemInstances).toMap

    Decoder[String].emap { name =>
      mapping.get(name).fold(Left(s"Invalid value `\$name`"))(Right(_))
    }
  }

  inline def deriveEnumEncoder[T](using m: Mirror.SumOf[T]): Encoder[T] = {
    val elemInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[T]]]
        .map(_.value)

    val elemNames =
      summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[String]]]
        .map(_.value)

    val mapping = (elemInstances zip elemNames).toMap

    Encoder[String].contramap[T](mapping.apply)
  }

  inline def deriveEnumCodec[T](using m: Mirror.SumOf[T]): Codec[T] =
    Codec.from(deriveEnumDecoder[T], deriveEnumEncoder[T])

}
