package info.golushkov.eve.tool.akka.utils

import scala.concurrent._
import scala.concurrent.duration._

trait UberFuture {
  implicit class UberUberFuture[T](f: Future[T]) {
    def await: T = Await.result(f, 1.minute)
  }
}
