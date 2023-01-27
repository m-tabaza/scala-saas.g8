package $package$.core.users.auth

trait AuthAlgebra[F[_]] {

  def decodeUserJwt(jwt: String): F[UserJwtPayload]

  def encodeUserJwt(payload: UserJwtPayload): F[String]

  def hashPassword(plainPassword: String): F[String]

  def verifyPassword(plain: String, hashed: String): F[Boolean]

}
