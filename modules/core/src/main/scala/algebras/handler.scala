package algebras

import cats.effect.Concurrent
import cats.effect.kernel.Async
import cats.implicits._
import domain.states.Event
import domain.states.FSMState
import domain.states._
import org.typelevel.log4cats.Logger

object handler {

  trait ActionHandler[F[_]] {
    def handle(state: FSMState, event: Event): F[Either[Throwable, FSMState]]
  }

  object SimpleStorageHandler {
    def make[F[_]: Concurrent: Logger: Async]: ActionHandler[F] =
      (state: FSMState, event: Event) => {
        state match {
          case s@Init =>
            event match {
              case e@Start =>
                info(s, e) *>
                  Async[F].delay(Right(Load))
              case e@_ => invalidEvent(s, e)
            }
          case s@Load =>
            event match {
              case e@DataLoaded =>
                info(s, e) *>
                  Async[F].delay(Right(Finish))
              case e@DataFailed =>
                info(s, e) *>
                  Logger[F].warn("Failed loading data") *>
                  Async[F].delay(Right(Load))
              case e@_ => invalidEvent(s, e)
            }
          case s@Finish =>
            event match {
              case e@Reset =>
                info(s, e) *>
                  Async[F].delay(Right(Init))
              case e@_ => invalidEvent(s, e)
            }
          case s@_ => Logger[F].error(s"Invalid state '$s'") *>
            Async[F].delay(Left(new Throwable))
        }
      }

    private def info[F[_]: Logger](s: FSMState, e: Event): F[Unit] =
      Logger[F].info(s"Execute event '$e' on state '$s'")

    private def error[F[_]: Logger](s: FSMState, e: Event): F[Unit] =
      Logger[F].error(s"Invalid event '$e' for state '$s'")

    private def invalidEvent[F[_]: Logger: Async](s: FSMState, e: Event): F[Either[Throwable, FSMState]] =
      error(s, e) *> Async[F].delay(Left(new Throwable))
  }
}
