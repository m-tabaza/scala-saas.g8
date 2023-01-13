package $project_name_camel_case$.core.users.sms

import com.messagebird.MessageBirdClient
import com.messagebird.MessageBirdServiceImpl
import com.messagebird.objects.Message
import $project_name_camel_case$.config
import $project_name_camel_case$.core.users.PhoneNumber
import $project_name_camel_case$.prelude.{*, given}

class SMSImpl[F[_]](using F: Sync[F], mbConfig: config.Messagebird)
    extends SMSAlgebra[F] {

  private val service = new MessageBirdServiceImpl(mbConfig.apiKey)
  private val client = new MessageBirdClient(service)

  override def send(phone: PhoneNumber, message: String): F[Unit] =
    F.blocking(client.sendMessage(Message("$project_name_pascal_case$", message, phone.value)))

}
