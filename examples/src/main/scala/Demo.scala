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

  final case class NestedParent() {
    final case class NestedChild(name: String)
  }

  def fun() = {
    final case class LocalClass(name: String)

    LocalClass("a").toString()
  }

  println(User("Joe", 23).toString)
  println(MultiParameterList("foo", 20)("s"))
  println(Person("boo").toString)
  println(new HasOtherConstructors(0))
  println(Foo[cats.Id].foo)
  println(NestedParent().NestedChild("a"))
  println(fun())
}

trait Foo[F[_]] {
  def foo: F[Unit]
}

object Foo {

  def apply[F[_]](implicit F: Foo[F]): Foo[F] = F

  implicit def applicativeFoo[F[_]: Applicative]: Foo[F] = new Foo[F] {
    def foo: F[Unit] = Applicative[F].unit
  }
}
