import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Ref
import cats.effect.std.Supervisor
import config.data.HttpServerConfig
import domain.states
import domain.states.Init
import domain.states.Load
import domain.states.On
import domain.states.Start
import domain.states.Transition
import modules.HttpApi
import modules.Services
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  val graph: Map[states.FSMState, Transition] = Map(
    Init -> Transition(Init, On(Start, Load)),
    Load -> Transition(Load, On(Start, Init))
  )

  def showEmberBanner(s: Server): IO[Unit] =
    Logger[IO].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  override def run(args: List[String]): IO[ExitCode] =
    Logger[IO].info(s"Loaded config...") >>
      Supervisor[IO]
        .use { implicit sp =>
          AppResources
            .make[IO]
            .evalMap { _ =>
              Ref[IO].of(Transition(Init, On(Start, Load))).map { init =>
                val services = Services.make[IO](init = init, graph = graph)
                val api      = HttpApi.make[IO](services)
                HttpServerConfig.default -> api
              }
            }
            .flatMap {
              case (cfg, api) =>
                EmberServerBuilder
                  .default[IO]
                  .withHost(cfg.host)
                  .withPort(cfg.port)
                  .withHttpApp(api.httpApp)
                  .build
            }
            .use(showEmberBanner(_) >> IO.never.as(ExitCode.Success))
        }
}
