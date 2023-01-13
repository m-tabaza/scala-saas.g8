package $project_name_camel_case$.prelude

class QueryChecks extends munit.FunSuite with doobie.munit.IOChecker {
  override val transactor = xa
}

def xa = makeTestTransactor(
  host = "localhost",
  dbName = "$project_name_cabab_case$-dev",
  pgUser = "user",
  pgPassword = "pass"
)

/** Used for testing only */
def makeTestTransactor(
    host: String,
    dbName: String,
    pgUser: String,
    pgPassword: String
) = Transactor.fromDriverManager[cats.effect.IO](
  "org.postgresql.Driver",
  s"jdbc:postgresql://\$host/\$dbName",
  pgUser,
  pgPassword
)
