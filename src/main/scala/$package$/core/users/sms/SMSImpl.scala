package $package$.core.users.sms

import com.messagebird.MessageBirdClient
import com.messagebird.MessageBirdServiceImpl
import com.messagebird.objects.Message
import $package$.config
import $package$.core.users.PhoneNumber
import $package$.prelude.{*, given}

class SMSImpl[F[_]](using F: Sync[F], mbConfig: config.Messagebird)
    extends SMSAlgebra[F] {

  private val service = new MessageBirdServiceImpl(mbConfig.apiKey)
  private val client = new MessageBirdClient(service)

  override def send(phone: PhoneNumber, message: String): F[Unit] =
    F.blocking(client.sendMessage(Message("$project_name_pascal_case$", message, phone.value)))

}
