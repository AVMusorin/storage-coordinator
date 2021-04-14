package algebras

import cats.effect.IO
import cats.effect.Ref
import domain.states
import domain.states.Init
import domain.states.Load
import domain.states.On
import domain.states.Start
import domain.states.Transition
import weaver.SimpleIOSuite

object FSMSpec extends SimpleIOSuite {
  val graph: Map[states.FSMState, Transition] = Map(
    Init -> Transition(Init, On(Start, Load)),
    Load -> Transition(Load, On(Start, Init))
  )
  test("get state for simple fsm") {
    for {
      init <- Ref[IO].of(Transition(Init, On(Start, Load)))
      current <- new FSMSimple[IO](init = init, graph = graph).currentState
    } yield expect.same(current, Transition(Init, On(Start, Load)))
  }

  test("change state for simple fsm") {
    for {
      init <- Ref[IO].of(Transition(Init, On(Start, Load)))
      fsm = new FSMSimple[IO](init = init, graph = graph)
      _ <- fsm.transition(Start)
      newState <- fsm.currentState
    } yield expect.same(newState, Transition(Load, On(Start, Init)))
  }
}
