package config

import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import config.data.AppConfig
import config.data.FSMConfig
import config.data.HttpClientConfig
import config.data.HttpServerConfig
import cats.instances._
import domain.states.DataFailed
import domain.states.DataLoaded
import domain.states.Finish
import domain.states.Init
import domain.states.Load
import domain.states.LoadFailure
import domain.states.On
import domain.states.Transition

import scala.concurrent.duration._
import scala.reflect.internal.util.ChromeTrace.EventType.Start
import scala.util.control.NoStackTrace

object loader {

  case object InvalidHostOrPort extends NoStackTrace

  def apply[F[_]: Async]: F[AppConfig] = {
    fsm.flatMap { fsmConfig =>
      (Host.fromString("0.0.0.0"), Port.fromInt(8080)).tupled
        .liftTo[F](InvalidHostOrPort)
        .map { case (h, p) =>
          AppConfig(
            fsmConfig,
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

  private def fsm[F[_]: Sync]: F[FSMConfig] = {
    Sync[F].delay {
      val transitions = List(
        Transition(
          state = Init,
          On(Start, Load)),
        Transition(
          state = Load,
          On(DataLoaded, Finish),
          On(DataFailed, LoadFailure)
        ),
        Transition(
          state = LoadFailure,
          On()
        )
      )
    }
  }

}
