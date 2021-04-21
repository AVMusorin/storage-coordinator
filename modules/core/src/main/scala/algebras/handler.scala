package algebras

import cats.effect.Concurrent
import cats.effect.kernel.Async
import cats.implicits._
import domain.states.EventType
import domain.states.FSMStateType
import domain.states._
import org.typelevel.log4cats.Logger

object handler {

  /**
    * Handle events and execute actions
    */
  trait ActionHandler[F[_]] {
    def handle(state: FSMStateType, event: EventType): F[Either[Throwable, FSMStateType]]
  }

  object SimpleStorageHandler {
    def make[F[_]: Concurrent: Logger: Async]: ActionHandler[F] =
      (state: FSMStateType, event: EventType) => {
        state match {
          case s @ Init =>
            event match {
              case e @ Start =>
                info(s, e) *>
                  Async[F].delay(Right(Load))
              case e @ _ => invalidEvent(s, e)
            }
          case s @ Load =>
            event match {
              case e @ DataLoaded =>
                info(s, e) *>
                  Async[F].delay(Right(Finish))
              case e @ _ => invalidEvent(s, e)
            }
          case s @ Finish =>
            event match {
              case e @ Reset =>
                info(s, e) *>
                  Async[F].delay(Right(Init))
              case e @ _ => invalidEvent(s, e)
            }
          case s @ _ =>
            Logger[F].error(s"Invalid state '$s'") *>
              Async[F].delay(Left(new Throwable))
        }
      }

    private def info[F[_]: Logger](s: FSMStateType, e: EventType): F[Unit] =
      Logger[F].info(s"Execute event '$e' on state '$s'")

    private def error[F[_]: Logger](s: FSMStateType, e: EventType): F[Unit] =
      Logger[F].error(s"Invalid event '$e' for state '$s'")

    private def invalidEvent[F[_]: Logger: Async](s: FSMStateType, e: EventType): F[Either[Throwable, FSMStateType]] =
      error(s, e) *> Async[F].delay(Left(new Throwable))
  }
}
