package $package$.prelude.time

import $package$.prelude.{*, given}

def utcNow[F[_]](using F: Sync[F]): F[Instant] =
  F.delay(java.time.Instant.now().atZone(java.time.ZoneId.of("UTC")).toInstant)
