package config

import algebras.FSM
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port

import scala.concurrent.duration.FiniteDuration

object data {
  case class AppConfig(
      httpClientConfig: HttpClientConfig,
      httpServerConfig: HttpServerConfig
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
