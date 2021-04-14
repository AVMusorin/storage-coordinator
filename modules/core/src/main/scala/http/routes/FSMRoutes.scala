package http.routes

import algebras.FSM
import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityEncoder._

final class FSMRoutes[F[_]: Monad](fsm: FSM[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/fsm"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(fsm.currentState)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
