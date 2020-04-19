object Demo extends App {

  final case class NormalClass(name: String, age: Int)

  final case class User(name: String, age: Int = 42)(val s: String)

  final case class Person(name: String) {
    override def toString: String = "***"
  }

  final case class HasOtherConstructors(s: String) {
    def this(a: Int) = this("42")
  }

  println(NormalClass("JP2", 2137))
  println(User("foo")("oops").toString)
  println(Person("boo").toString)
  println(new HasOtherConstructors(0))
}
