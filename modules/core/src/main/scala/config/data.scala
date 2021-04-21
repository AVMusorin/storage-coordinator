package config

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

  case class HttpClientConfig(
      timeout: FiniteDuration,
      idleTimeInPool: FiniteDuration
  )
}
