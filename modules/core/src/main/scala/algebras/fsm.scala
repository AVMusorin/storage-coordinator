package algebras

import algebras.handler.ActionHandler
import cats.Monad
import cats.data.EitherT
import cats.effect.Ref
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import domain.states.Event
import domain.states.FSMState

/**
  * Finite state machine
  */
trait FSM[F[_]] {
  def currentState: F[FSMState]
  def transition(event: Event): EitherT[F, Throwable, FSMState]
}

class FSMPersistence[F[_]: Monad](private val cmd: RedisCommands[F, String, Int]) extends FSM[F] {
  override def currentState: F[FSMState] = ???

  override def transition(event: Event): EitherT[F, Throwable, FSMState] = ???
}

/**
 * case class to store subtypes of FSMState in [[cats.effect.Ref]]
 */
case class SimpleStateStore(state: FSMState)

/**
 * Simple FSM stores states in memory and will be reset after restart
 * @param init an initial state
 * @param handler handler for processing states
 */
class FSMSimple[F[_]: Monad](
    private val init: Ref[F, SimpleStateStore],
    handler: ActionHandler[F]
) extends FSM[F] {
  override def currentState: F[FSMState] = init.get.map(_.state)

  override def transition(event: Event): EitherT[F, Throwable, FSMState] =
    for {
      cState <- EitherT.right(currentState)
      state  <- EitherT(handler.handle(cState, event))
      _      <- EitherT.right(init.set(SimpleStateStore(state)))
    } yield state
}
