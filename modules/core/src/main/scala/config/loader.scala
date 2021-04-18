package config

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import config.data.AppConfig
import config.data.HttpClientConfig
import config.data.HttpServerConfig

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

object loader {

  case object InvalidHostOrPort extends NoStackTrace

  def apply[F[_]: Async]: F[AppConfig] = {
    (Host.fromString("0.0.0.0"), Port.fromInt(8080)).tupled
      .liftTo[F](InvalidHostOrPort)
      .map {
        case (h, p) =>
          AppConfig(
            HttpClientConfig(
              timeout = 30.seconds,
              idleTimeInPool = 60.seconds
            ),
            HttpServerConfig(
              host = h,
              port = p
            )
          )
      }
  }
}
