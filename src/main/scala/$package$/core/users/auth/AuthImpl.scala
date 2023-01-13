package $project_name_camel_case$.core.users.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtCirce
import $project_name_camel_case$.config.AppSecret
import $project_name_camel_case$.prelude.{*, given}

class AuthImpl[F[_]](using F: ApplicativeThrow[F], appSecret: AppSecret)
    extends AuthAlgebra[F] {

  private val bcryptVersion = BCrypt.Version.VERSION_2B
  private val verifyer = BCrypt.verifyer(bcryptVersion)
  private val hasher = BCrypt.`with`(bcryptVersion)

  private val jwtAlgorithm = JwtAlgorithm.HS512

  override def decodeUserJwt(jwt: String) = F.fromEither {
    JwtCirce
      .decodeJson(jwt, appSecret.secret, Seq(jwtAlgorithm))
      .toEither
      .flatMap(json => json.as[UserJwtPayload])
      .leftMap(_ => Exception("Unauthorized"))
  }

  override def encodeUserJwt(payload: UserJwtPayload) = F.pure {
    JwtCirce.encode(payload.asJson, appSecret.secret, jwtAlgorithm)
  }

  override def hashPassword(plainPassword: String) =
    F.catchNonFatal(hasher.hashToString(4, plainPassword.toCharArray))

  override def verifyPassword(plain: String, hashed: String) =
    F.catchNonFatal(verifyer.verify(plain.getBytes, hashed.getBytes).verified)

}
