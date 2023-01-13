package $project_name_camel_case$.prelude

import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger as CatsLogger

import scala.quoted.*

class Logger[F[_]: Sync](val context: Map[String, String]) {
  import Logger.*

  private val catsLogger: CatsLogger[F] = Slf4jLogger.getLogger[F]

  inline def info(msg: String): F[Unit] =
    log(msg, None, LogLevel.Info, context, catsLogger)

  inline def error(msg: String): F[Unit] =
    log(msg, None, LogLevel.Error, context, catsLogger)

  inline def error(msg: String, underlying: Throwable): F[Unit] =
    log(msg, Some(underlying), LogLevel.Error, context, catsLogger)

  def add(key: String, value: String) = Logger[F](context.updated(key, value))

  def ::(tuple: (String, String)) = add.tupled(tuple)

}
object Logger {

  enum LogLevel {
    case Info, Error
  }

  inline def log[F[_]](
      msg: String,
      error: Option[Throwable],
      level: LogLevel,
      context: Map[String, String],
      logger: CatsLogger[F]
  ): F[Unit] = \${ logCode('msg, 'error, 'level, 'context, 'logger) }

  def logCode[F[_]: Type](
      msg: Expr[String],
      error: Expr[Option[Throwable]],
      logLevel: Expr[LogLevel],
      context: Expr[Map[String, String]],
      logger: Expr[CatsLogger[F]]
  )(using Quotes) = '{
    val l = \$logger
    val m = \$msg
    val level = \$logLevel
    val e = \$error
    val ctx = \$context

    val contextStr: String =
      if ctx.isEmpty then ""
      else " ::: " + ctx.asJson.toString

    (level, e) match {
      case (LogLevel.Info, _)        => l.info(m + contextStr)
      case (LogLevel.Error, Some(e)) => l.error(e)(m + contextStr)
      case (LogLevel.Error, None)    => l.error(m + contextStr)
    }
  }

  val ioLogger = Slf4jLogger.getLogger[cats.effect.IO]

  inline def debug[A](inline a: A): A = \${ debugCode('a) }

  private def debugCode[A: Type](a: Expr[A])(using Quotes): Expr[A] = {
    import quotes.reflect.*

    val term = a.asTerm
    val sym = term.symbol
    val name = a.asTerm match {
      case Inlined(_, _, Ident(ident)) => Expr(ident)
      case _                           => Expr(sym.fullName)
    }
    val tpe = Expr(Type.show[A])
    val pos = Position.ofMacroExpansion
    val posStr = Expr {
      val path = pos.sourceFile.getJPath.map(_.toString).getOrElse("")
      s"\$path:\${pos.startLine + 1}:\${pos.startColumn + 1}"
    }

    '{
      val aVal = \$a
      println(
        s"\${\$name} :: (\${compiletime.codeOf(\$a)}) : \${\$tpe} = \$aVal @ \${\$posStr}"
      )
      aVal
    }
  }

}
