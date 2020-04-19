import cats.tagless.finalAlg
import cats.Applicative

object Demo extends App {

  final case class User(name: String, age: Int)
  final case class MultiParameterList(name: String, age: Int)(val s: String)

  final case class Person(name: String) {
    override def toString: String = "***"
  }

  final case class HasOtherConstructors(s: String) {
    def this(a: Int) = this("42")
  }

  final case class ShouldHaveNormalToString(x: String)

  println(User("Joe", 23).toString)
  println(MultiParameterList("foo", 20)("s"))
  println(Person("boo").toString)
  println(new HasOtherConstructors(0))
  println(ShouldHaveNormalToString("a"))
  println(Foo[cats.Id].foo)
}

@finalAlg
trait Foo[F[_]] {
  def foo: F[Unit]
}

object Foo {

  implicit def applicativeFoo[F[_]: Applicative]: Foo[F] = new Foo[F] {
    def foo: F[Unit] = Applicative[F].unit
  }
}
