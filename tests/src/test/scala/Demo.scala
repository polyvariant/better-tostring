import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Tests extends AnyWordSpec with Matchers {

  "Simple case class" should {
    "stringify nicely" in {
      SimpleCaseClass(
        "Joe",
        23
      ).toString shouldBe "SimpleCaseClass(name = Joe, age = 23)"

    }
  }

  "Case class with multiple parameter lists" should {
    "only have the first list included" in {
      MultiParameterList("foo", 20)(
        "s"
      ).toString shouldBe "MultiParameterList(name = foo, age = 20)"
    }
  }

  "Case class with custom toString" should {
    "use it" in {
      CustomTostring("Joe").toString shouldBe "***"
    }
  }

  "Method with alternate constructors" should {
    "stringify based on primary constructor" in {
      new HasOtherConstructors(
        10
      ).toString shouldBe "HasOtherConstructors(s = 10 beers)"
    }
  }

  "Class nested in an object" should {
    "include enclosing object's name" in {
      ObjectNestedParent.ObjectNestedClass("Joe").toString shouldBe "ObjectNestedParent.ObjectNestedClass(name = Joe)"
    }
  }

  "Class nested in a package object" should {
    "not include package's name" in {
      pack.InPackageObject("Joe").toString shouldBe "InPackageObject(name = Joe)"
    }
  }

  "Class nested in another class" should {
    "stringify normally" in {
      new NestedParent().NestedChild("a").toString shouldBe "NestedChild(a)"
    }
  }

  "Class nested in an object itself nested in a class" should {
    "stringify normally" in {
      new DeeplyNestedInClassGrandparent().DeeplyNestedInClassParent.DeeplyNestedInClassClass("a").toString shouldBe "DeeplyNestedInClassClass(a)"
    }
  }

  "Method-local class" should {
    "stringify normally" in {
      MethodLocalWrapper.methodLocalClassStringify shouldBe "LocalClass(a)"
    }
  }
}

final case class SimpleCaseClass(name: String, age: Int)
final case class MultiParameterList(name: String, age: Int)(val s: String)

final case class CustomTostring(name: String) {
  override def toString: String = "***"
}

final case class HasOtherConstructors(s: String) {
  def this(a: Int) = this(a.toString + " beers")
}

final class NestedParent() {
  case class NestedChild(name: String)
}

object ObjectNestedParent {
  case class ObjectNestedClass(name: String)
}

final class DeeplyNestedInClassGrandparent {
  object DeeplyNestedInClassParent {
    case class DeeplyNestedInClassClass(name: String)
  }
}

object MethodLocalWrapper {

  def methodLocalClassStringify: String = {
    final case class LocalClass(name: String)

    LocalClass("a").toString()
  }

}
