package $project_name_camel_case$.core

import $project_name_camel_case$.config
import $project_name_camel_case$.core.users.UserImpl
import $project_name_camel_case$.prelude.{*, given}

class $project_name_pascal_case$Impl[F[_]: Async](using Transactor[F])(using
    config.AppSecret,
    config.Messagebird
) extends $project_name_pascal_case$Algebra[F] {

  override val users = UserImpl[F]

}
