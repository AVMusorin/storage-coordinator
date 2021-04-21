import cats.effect.Async
import cats.effect.Concurrent
import cats.effect.Resource
import config.data.AppConfig
import config.data.HttpClientConfig
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger

final case class AppResources[F[_]](
    client: Client[F]
)

object AppResources {
  def make[F[_]: Concurrent: Logger: MkHttpClient: Network](cfg: AppConfig): Resource[F, AppResources[F]] = {
    val client: Resource[F, Client[F]] = MkHttpClient[F].newEmber(cfg.httpClientConfig)
    client.map(AppResources.apply)
  }
}

trait MkHttpClient[F[_]] {
  def newEmber(c: HttpClientConfig): Resource[F, Client[F]]
}

object MkHttpClient {
  def apply[F[_]: MkHttpClient]: MkHttpClient[F] = implicitly

  implicit def forAsync[F[_]: Async]: MkHttpClient[F] =
    new MkHttpClient[F] {
      def newEmber(c: HttpClientConfig): Resource[F, Client[F]] =
        EmberClientBuilder
          .default[F]
          .withTimeout(c.timeout)
          .withIdleTimeInPool(c.idleTimeInPool)
          .build
    }
}
