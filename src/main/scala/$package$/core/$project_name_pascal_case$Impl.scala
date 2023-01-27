package $package$.core

import $package$.config
import $package$.core.users.UserImpl
import $package$.prelude.{*, given}

class $project_name_pascal_case$Impl[F[_]: Async](using Transactor[F])(using
    config.AppSecret,
    config.Messagebird
) extends $project_name_pascal_case$Algebra[F] {

  override val users = UserImpl[F]

}
