package $package$.http.users

import $package$.prelude.{*, given}
import $package$.core.$project_name_pascal_case$Algebra

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
