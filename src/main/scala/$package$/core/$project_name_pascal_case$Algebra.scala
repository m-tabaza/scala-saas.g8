package $project_name_camel_case$.core

import $project_name_camel_case$.core.users.UserAlgebra

trait $project_name_pascal_case$Algebra[F[_]] {

  val users: UserAlgebra[F]
  given UserAlgebra[F] = users
  export users.given

}
