object Demo extends App {

  final case class User(name: String, var age: Int)
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
  
  enum Tree[T] {
    case Node(l: Tree[T], r: Tree[T])
    case Leaf(v: T)
  }

  println(User("Joe", 23).toString)
  println(MultiParameterList("foo", 20)("s"))
  println(Person("boo").toString)
  println(new HasOtherConstructors(0))
  println(NestedParent().NestedChild("a"))
  println(fun())
  println(Tree.Node(Tree.Leaf(69), Tree.Node(Tree.Leaf(4), Tree.Leaf(2))))
}
