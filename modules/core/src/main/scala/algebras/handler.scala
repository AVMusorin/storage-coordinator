package algebras

import cats.effect.ExitCode

trait Handler[F[_]] {
  def execute: F[ExitCode]
  def rollback: F[ExitCode]
}

object RestHandler {
  def make[F[_]](params)
}
