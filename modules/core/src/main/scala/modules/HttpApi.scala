package modules

import cats.effect.Async
import http.routes.FSMRoutes
import http.routes.version
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.AutoSlash
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.middleware.ResponseLogger
import org.http4s.server.middleware.Timeout
import org.typelevel.log4cats.Logger

import scala.concurrent.duration._

object HttpApi {
  def make[F[_]: Async: Logger](services: Services[F]): HttpApi[F] = HttpApi[F](services)
}

final case class HttpApi[F[_]: Async: Logger] private (services: Services[F]) {
  private val fsmRoutes = new FSMRoutes[F](services.fsm).routes

  private val openRoutes: HttpRoutes[F] = fsmRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}
