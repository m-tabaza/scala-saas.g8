package $project_name_camel_case$.prelude.time

import $project_name_camel_case$.prelude.{*, given}

def utcNow[F[_]](using F: Sync[F]): F[Instant] =
  F.delay(java.time.Instant.now().atZone(java.time.ZoneId.of("UTC")).toInstant)
