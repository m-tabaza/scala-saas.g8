package $project_name_camel_case$.http.users

import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.core.$project_name_pascal_case$Algebra

object UserRoutes {

  def apply[F[_]: Async](using root: $project_name_pascal_case$Algebra[F]) = {
    import root.given

    List(
      SignupRoute[F],
      VerifyUserRoute[F],
      LoginRoute[F]
    )
  }

}
