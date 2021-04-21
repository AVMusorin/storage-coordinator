package http.routes

import algebras.FSM
import cats.Monad
import cats.implicits._
import domain.states.ChangeFSMState
import domain.states.FSMState
import domain.states.FSMState.jsonEncoder
import domain.states.FSMStateType
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import io.circe.syntax._

final class FSMRoutes[F[_]: Monad: JsonDecoder](fsm: FSM[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/fsm"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(fsm.currentState.map(FSMState.apply))

    case ar @ POST -> Root =>
      ar.asJsonDecode[ChangeFSMState].flatMap { v =>
        fsm.transition(v.event).value.flatMap {
          case Right(value) => Ok(value.toString)
          case Left(value)  => InternalServerError(value.toString)
        }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
