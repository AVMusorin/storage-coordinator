package algebras

import algebras.handler.ActionHandler
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Sync
import domain.states
import domain.states.EventType
import domain.states.FSMStateType
import domain.states.Init
import domain.states.Load
import domain.states.Start
import weaver.SimpleIOSuite

object FSMSpec extends SimpleIOSuite {

  test("get current state for simple fsm") {
    val handler = new ActionHandler[IO] {
      override def handle(state: FSMStateType, event: EventType): IO[Either[Throwable, FSMStateType]] =
        Sync[IO].delay(Right(Init))
    }

    for {
      init    <- Ref[IO].of(SimpleStateStore(Init))
      current <- new FSMSimple[IO](init = init, handler = handler).currentState
    } yield expect.same(current, Init)
  }

  test("error in actions during transition") {
    val handler = new ActionHandler[IO] {
      override def handle(state: FSMStateType, event: EventType): IO[Either[Throwable, FSMStateType]] =
        Sync[IO].delay(Left(new Throwable("error")))
    }

    for {
      init <- Ref[IO].of(SimpleStateStore(Init))
      fsm = new FSMSimple[IO](init = init, handler = handler)
      cState1      <- fsm.currentState
      invalidTrans <- fsm.transition(Start).value
      cState2      <- fsm.currentState
    } yield expect.same(Init, cState1) &&
      expect.same("error", invalidTrans.left.getOrElse(new Throwable).getMessage) &&
      expect.same(Init, cState2)
  }

  test("valid transition") {
    val handler = new ActionHandler[IO] {
      override def handle(state: FSMStateType, event: EventType): IO[Either[Throwable, FSMStateType]] = {
        state match {
          case Init =>
            event match {
              case Start =>
                IO(println("some side efffects")) *>
                  IO(Right(Load))
              case _ => IO(Left(new Throwable))
            }
          case _ => IO(Left(new Throwable))
        }
      }
    }

    for {
      init <- Ref[IO].of(SimpleStateStore(Init))
      fsm = new FSMSimple[IO](init = init, handler = handler)
      cState1      <- fsm.currentState
      trans <- fsm.transition(Start).value
      cState2      <- fsm.currentState
    } yield expect.same(Init, cState1) &&
      expect.same(Load, trans.getOrElse(states.None)) &&
      expect.same(Load, cState2)
  }
}
