import $project_name_camel_case$.prelude.{*, given}
import $project_name_camel_case$.core.users.*
import $project_name_camel_case$.core.gyms.*

import cats.effect.unsafe.implicits.*

val (impl, destroyImpl) = $project_name_camel_case$.impl.allocated.unsafeRunSync()
