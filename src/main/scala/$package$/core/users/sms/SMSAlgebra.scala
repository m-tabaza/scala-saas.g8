package $project_name_camel_case$.core.users.sms

import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.core.users.PhoneNumber

trait SMSAlgebra[F[_]] {

  def send(to: PhoneNumber, message: String): F[Unit]

}
