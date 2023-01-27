package $package$.core.users.sms

import $package$.prelude.{*, given}
import $package$.core.users.PhoneNumber

trait SMSAlgebra[F[_]] {

  def send(to: PhoneNumber, message: String): F[Unit]

}
