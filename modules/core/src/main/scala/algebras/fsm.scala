package algebras

import cats.Monad
import cats.effect.Ref
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import domain.states.Event
import domain.states.FSMState
import domain.states.Transition

/**
  * Finite state machine
  */
trait FSM[F[_]] {
  def currentState: F[Transition]
  def transition(event: Event): F[Transition]
}

class FSMPersistence[F[_]](private val cmd: RedisCommands[F, String, Int]) extends FSM[F] {
  override def currentState: F[Transition] = ???

  override def transition(event: Event): F[Transition] = ???
}

class FSMSimple[F[_]: Monad](private val init: Ref[F, Transition], graph: Map[FSMState, Transition]) extends FSM[F] {
  override def currentState: F[Transition] = init.get

  override def transition(event: Event): F[Transition] = {
    val newT = currentState.fmap { curState: Transition =>
      val curTrans = graph.getOrElse(curState.state, throw new Exception("Invalid state")) // TODO: better exception
      // TODO: add logging about invalid events
      val newTransition = curTrans
        .getStateByEvent(event)
        .map { newState =>
          graph.getOrElse(newState, curState)
        }
        .getOrElse(curTrans)
      newTransition
    }
    for {
      n <- newT
      _ <- init.set(n)
    } yield n
  }
}
