import algebras.SimpleStateStore
import algebras.handler.SimpleStorageHandler
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Ref
import cats.effect.std.Supervisor
import domain.states.Init
import modules.HttpApi
import modules.Services
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def showEmberBanner(s: Server): IO[Unit] =
    Logger[IO].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  override def run(args: List[String]): IO[ExitCode] =
    config.loader[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") >>
        Supervisor[IO]
          .use { implicit sp =>
            AppResources
              .make[IO](cfg)
              .evalMap { _ =>
                val httpApi: IO[HttpApi[IO]] = for {
                  initState <- Ref[IO].of(SimpleStateStore(Init))
                  services = Services.make[IO](init = initState, fsmHandler = SimpleStorageHandler.make[IO])
                } yield HttpApi.make[IO](services)
                httpApi.map(cfg.httpServerConfig -> _)
              }
              .map {
                case (cfg, api) =>
//                  EmberServerBuilder
//                    .default[IO]
//                    .withHost(cfg.host)
//                    .withPort(cfg.port)
//                    .withHttpApp(api.httpApp)
//                    .build
                  BlazeServerBuilder[IO](global)
                    .bindHttp(cfg.port.value, cfg.host.toString)
                    .withHttpApp(api.httpApp)
                    .serve
                    .compile
              }
              .use(_.drain.as(ExitCode.Success))
//              .use(showEmberBanner(_) >> IO.never.as(ExitCode.Success))
          }
    }
}
