package config

import com.comcast.ip4s.{ Host, Port }
import domain.states.OnEvent
import domain.states.Transition

import scala.concurrent.duration.FiniteDuration

object data {
  case class AppConfig(
      fsmConfig: FSMConfig,
      httpClientConfig: HttpClientConfig,
      httpServerConfig: HttpServerConfig
  )

  case class FSMConfig(
      transitions: List[Transition],
      onEvents: List[OnEvent]
  )

  case class HttpServerConfig(
      host: Host,
      port: Port
  )
  object HttpServerConfig {
    def default: HttpServerConfig = {
      val conf = for {
        host <- Host.fromString("localhost")
        port <- Port.fromInt(8080)
      } yield HttpServerConfig(host, port)
      conf.getOrElse(throw new RuntimeException("Invalid host:port"))
    }
  }

  case class HttpClientConfig(
      timeout: FiniteDuration,
      idleTimeInPool: FiniteDuration
  )
}
