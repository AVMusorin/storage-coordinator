package modules

import algebras.FSM
import algebras.FSMSimple
import algebras.SimpleStateStore
import algebras.handler.ActionHandler
import cats.effect.Ref
import cats.effect.Temporal

object Services {
  def make[F[_]: Temporal](
      init: Ref[F, SimpleStateStore],
      fsmHandler: ActionHandler[F]
  ): Services[F] = {
    Services[F](fsm = new FSMSimple[F](init = init, handler = fsmHandler))
  }
}

final case class Services[F[_]] private (val fsm: FSM[F])
