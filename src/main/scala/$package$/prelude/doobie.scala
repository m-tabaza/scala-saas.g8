package $package$.prelude

import scala.compiletime.summonAll
import scala.deriving.Mirror
import doobie.util.log.*

object deriveMeta {

  inline def deriveEnumMeta[T](using m: Mirror.SumOf[T]): Meta[T] = {
    val elemInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[T]]]
        .map(_.value)

    val elemNames =
      summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator
        .asInstanceOf[Iterator[ValueOf[String]]]
        .map(_.value)

    val mapping = (elemNames zip elemInstances).toMap

    Meta[String].imap { name =>
      mapping
        .get(name)
        .getOrElse(throw Exception(s"Invalid value `\$name`"))
    }(_.toString)
  }

}

given LogHandler = LogHandler { event =>
  val logPrefix =
    Thread.currentThread.getStackTrace
      .lift(1)
      .map(_.getMethodName)
      .getOrElse("UNKNOWN") + " :: "

  import cats.effect.unsafe.implicits.global

  event match {
    case ProcessingFailure(s, a, e1, e2, t) =>
      Logger.ioLogger
        .error(logPrefix + s"""Failed Resultset Processing:
         |
         |  \${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
         |
         | arguments = [\${a.mkString(", ")}]
         |   elapsed = \${e1.toMillis.toString} ms exec + \${e2.toMillis.toString} ms processing (failed) (\${(e1 + e2).toMillis.toString} ms total)
         |   failure = \${t.getMessage}
         """.stripMargin)
        .unsafeRunAsync(identity)

    case ExecFailure(s, a, e1, t) =>
      Logger.ioLogger
        .error(logPrefix + s"""Failed Statement Execution:
         |
         |  \${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
         |
         | arguments = [\${a.mkString(", ")}]
         |   elapsed = \${e1.toMillis.toString} ms exec (failed)
         |   failure = \${t.getMessage}
         """.stripMargin)
        .unsafeRunAsync(identity)
    case _ => ()
  }
}
