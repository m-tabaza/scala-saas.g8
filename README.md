# Setup
* Install Scala tools
  - Install a JDK and SBT (Scala Build Tool) using [`cs setup`](https://get-coursier.io/docs/cli-setup)
  - Install the [Metals extension](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) for VSCode, or use IntelliJ IDEA
* [Install Docker](https://docs.docker.com/get-docker/)
* [Install Docker-Compose](https://docs.docker.com/compose/install/)

To run the project:
* `cd` into the `backend` directory
* Run `docker-compose up -d`
* Run `sbt`
  Within the SBT shell:
  - To run the server, run `~reStart`
  - To run all tests, run `test`
  - To reset the database, run `flywayClean;flywayMigrate`
  - To enter a REPL with a root algebra implementation in scope, run `console`, then `:load repl.sc`
* To insert seed data, run `sh scripts/load-seeds.sh`

When using metals:
* Make sure to switch from using bloop to using the SBT build server using the following command in the VSCode command palette:
```
> Metals: switch build server
```
* Wait for Metals to finish compilation - avoid running `reStart` before Metals is done compiling (keep an eye on the "Outptut" tab next to "Terminal")

# Learning
Scala 3 (in the backend) is used in a purely functional style. Many purely functional libraries from the [Typelevel](https://typelevel.org/) ecosystem are heavily utilized, and are better mastered as soon as possible:
* [Cats](https://typelevel.org/cats/): purely functional abstractions
* [Cats Effect](https://typelevel.org/cats-effect/): asynchronous IO and concurrency primitives
* [Skunk](https://tpolecat.github.io/skunk/): Postgres database access
* [fs2](https://fs2.io/#/): streaming
* [Tapir](https://tapir.softwaremill.com/en/latest/): HTTP client and server abstractions
* [Circe](https://circe.github.io/circe/): JSON serialization

Following are the recommended steps to take in order to become productive as soon as possible.

## Learn Scala Itself
To be productive, you need to have a solid grasp over these concepts:
* Functions
* Algebraic data modeling using `case class`es and `enum`s
* Functional error handling (`Either`, `Option`)
* Generics (polymorphism)
* Higher-order functions
* Implicits (contextual abstractions)
* Type classes
* Higher-kinded types (polymorphic effects, tagless final)

If you prefer books, read [Functional Programming in Scala (Manning)](https://www.manning.com/books/functional-programming-in-scala). If you prefer online video courses, use [Functional Programming Principles in Scala (Coursera)](https://www.coursera.org/learn/scala-functional-programming)

## Learn Cats
Cats is used almost in every file in the project, and it's essential to know how to navigate code that uses it. These are the most commonly used abstractions from Cats:
* [Functor](https://typelevel.org/cats/typeclasses/functor.html)
* [Applicative](https://typelevel.org/cats/typeclasses/applicative.html)/ApplicativeError
* [Monad](https://typelevel.org/cats/typeclasses/monad.html)/MonadError
* [Traverse](https://typelevel.org/cats/typeclasses/traverse.html)

If you prefer books, read [Scala with Cats (Underscore)](https://underscore.io/books/scala-with-cats/). If you prefer online video courses, use [Scala with Cats (RocktheJVM)](https://rockthejvm.com/p/cats).

[This repository](https://github.com/hemanth/functional-programming-jargon) contains a cheat sheet for many purely functional terms used by Cats. It can be very useful when starting to learn abstract functional concepts.

For learning Cats Effect, the documentation is a great resource.

For learning FS2, use [the documentation](https://fs2.io), or read [this all-in-one article](https://blog.rockthejvm.com/fs2/).

# Guidelines
It is imperative that everyone follows these guidelines whenever feasible. The guidelines aim for high code reusability, simplicity, and testability.

## Write Pure, Simple Functions
When writing code, keep your functions *pure* and with the least possible number of dependencies (parameters). Here are some examples.

### Example 1
When writing a function that gets data from the databse using Doobie, you'll have a `Query0` value. *Do not* run `.to[...]`, `.unique`, `.option` or similar functions that return `ConnectionIO` **unless you really need to**. If you only need to transform the results of the query, use `.map`, and return `Query0` from your function. The reasoning behind this approach is explained later on.

### Example 2
When writing some transformation over some collection, like a list, *do not* write:
```scala
def myFunction(list: List[MyThing]): List[MyOtherThing] = list.map { ... }
```
Instead write:
```scala
def myFunction(thing: MyThing): MyOtherThing = ...
```
And then, where you have a `List[MyThing]`, write:
```scala
val list: List[MyThing] = ...
val newList: List[MyOtherThing] = list.map(myFunction)
```
This makes `myFunction` simpler, and more reusable. The approach isn't only applicable to `List`s, but to any `Functor`.

## Code Against an Interface

It's always a good idea to start writing a feature by designing a `trait` in the form:
```scala
// MyFeatureAlgebra.scala
trait MyFeatureAlgebra[F[_]] {
  
  def getSomething(id: String): F[Something]

  def doSomething(id: String): F[Unit]

  ...
}
```

The trait is parameterized over the effect type `F` that is returned from its methods. This makes it possible to choose the suitable effect type in the implementation, be it a mock implementation for testing, or the production implementation.

```scala
// MyFeatureImpl.scala
import cats.effect.IO

object MyFeatureImpl extends MyFeatureAlgebra[IO] {
  
  def getSomething(id: String): IO[Something] = ...

  def doSomething(id: String): IO[Unit] = ...

  ...
}

// MyFeatureTest.scala
import cats.Id

val myFeatureMock = new MyFeatureAlgebra[Id] {
  def getSomething(id: String): Something =
    Something(42, "dumb value", Greeting.Hi)

  private var somethingWasDone = false

  def doSomething(id: String): Unit = {
    somethingWasDone = true
  }
}
```

Now, whenever any of the functions in your trait is needed, just pass around a value `myFeatureAlgebra: MyFeatureAlgebra[F]`.

```scala
import cats.Monad

def doOtherThings[F[_]: Monad](s: String, myFeatureAlgebra: MyFeatureAlgebra[F]): F[Int] = 
  for {
    got <- myFeatureAlgebra.getSomething(s)
    _ <- myFeatureAlgebra.doSomething(s)
    ...
  } yield got.someFieldOfTypeInt
```

## Check Your SQL Queries in Tests

Doobie (the database library) supports query type-checking at test time. Whenever you write a query/update/delete, make sure to write it in this form:

```scala
// queries.scala
import doobie.*
import doobie.implicits.*

def getThingById(id: Int): Query0[Thing] = 
  sql"SELECT * FROM things WHERE id = \$id".query

def updateThing(id: Int, value: String): Update0 = 
  sql"UPDATE things SET some_field = \$value WHERE id = \$id".update

// query-tests.scala

class MyQueryChecks extends FunSuite with IOChecker {
  ...

  test("getThingById") {
    check {
      getThingById(123)
    }
  }

  test("updateThing") {
    check {
      updateThing(123, "xx")
    }
  }

}
```

These functions return `Query0` and `Update0`, which can be passed to Doobie's `check` function at test time.

Do _NOT_ do this:
```scala
// BAD
def getThingById(id: Int): ConnectionIO[Thing] = 
  sql"SELECT * FROM things WHERE id = \$id".query[Thing].unique

// BADDDDD
def updateThing(id: Int, value: String): ConnectionIO[Int] = 
  sql"UPDATE things SET some_field = \$value WHERE id = \$id".update.run
```

These functions both return `ConnectionIO` directly, so the queries within them cannot be checked at test time.
