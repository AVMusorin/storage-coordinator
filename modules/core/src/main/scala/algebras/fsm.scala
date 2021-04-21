package algebras

import algebras.handler.ActionHandler
import cats.Monad
import cats.data.EitherT
import cats.effect.Ref
import cats.implicits._
import domain.states.EventType
import domain.states.FSMStateType

/**
  * Finite state machine
  */
trait FSM[F[_]] {
  def currentState: F[FSMStateType]
  def transition(event: EventType): EitherT[F, Throwable, FSMStateType]
}

/**
  * FSM based on Redis and persisting state
  */
class FSMPersistence[F[_]: Monad] extends FSM[F] {
  override def currentState: F[FSMStateType] = ???

  override def transition(event: EventType): EitherT[F, Throwable, FSMStateType] = ???
}

/**
  * case class to store subtypes of FSMState in [[cats.effect.Ref]]
  */
case class SimpleStateStore(state: FSMStateType)

/**
  * Simple FSM stores states in memory and will be reset after restart
  * @param init an initial state
  * @param handler handler for processing states
  */
class FSMSimple[F[_]: Monad](
    private val init: Ref[F, SimpleStateStore],
    handler: ActionHandler[F]
) extends FSM[F] {
  override def currentState: F[FSMStateType] = init.get.map(_.state)

  override def transition(event: EventType): EitherT[F, Throwable, FSMStateType] =
    for {
      cState <- EitherT.right(currentState)
      state  <- EitherT(handler.handle(cState, event))
      _      <- EitherT.right(init.set(SimpleStateStore(state)))
    } yield state
}
