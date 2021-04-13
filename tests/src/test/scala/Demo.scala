import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait IO[A] {
  def flatMap[B](f: A => IO[B]): IO[B]
  def map[B](f: A => B): IO[B]
}

object Demo {

  def foo[A](ioa: IO[A]): IO[Int] = com.kubukoz.DebugUtils.withDesugar {
    for {
      a <- ioa
      x <- foo(ioa)
    } yield x
  }

}
