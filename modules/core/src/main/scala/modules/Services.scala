package modules

import algebras.FSM
import algebras.FSMSimple
import cats.effect.Ref
import cats.effect.Temporal
import domain.states.FSMState
import domain.states.Transition

object Services {
  def make[F[_]: Temporal](init: Ref[F, Transition], graph: Map[FSMState, Transition]): Services[F] = {
    Services[F](fsm = new FSMSimple[F](init = init, graph = graph))
  }
}

final case class Services[F[_]] private (val fsm: FSM[F])
