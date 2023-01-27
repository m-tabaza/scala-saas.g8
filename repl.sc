import $package$.prelude.{*, given}
import $package$.core.users.*
import $package$.core.gyms.*

import cats.effect.unsafe.implicits.*

val (impl, destroyImpl) = $project_name_camel_case$.impl.allocated.unsafeRunSync()
