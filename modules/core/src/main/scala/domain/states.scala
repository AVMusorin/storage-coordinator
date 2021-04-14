package domain

import cats.effect.ExitCode
import derevo.cats.eqv
import derevo.cats.show
import derevo.circe.magnolia.decoder
import derevo.circe.magnolia.encoder
import derevo.derive

object states {

  /**
    * Rule model for transitions
    * @param event event received
    * @param dest destination state
    */
  @derive(decoder, encoder, eqv, show)
  case class On[E, S](event: E, dest: S)

  /**
    * Actions witch should be executed on event to change state
    * @param event event
    * @param actions array of actions
    */
  case class OnEvent(event: Event, actions: Action*)

  /**
    * State model
    * @param state actual state
    * @param transitions list of available transitions
    */
  @derive(decoder, encoder, eqv, show)
  case class Transition(state: FSMState, transitions: On[Event, FSMState]*) {
    def getStateByEvent(event: Event): Option[FSMState] = {
      transitions.find {
        case On(e, _) if e == event => true
        case _                      => false
      } map (_.dest)
    }
  }

  @derive(decoder, encoder, eqv, show)
  sealed trait FSMState

  case object Init extends FSMState

  case object Load        extends FSMState
  case object LoadFailure extends FSMState
  case object LoadSuccess extends FSMState
  case object LoadApprove extends FSMState

  case object DataCheck        extends FSMState
  case object DataCheckFailure extends FSMState
  case object DataCheckSuccess extends FSMState
  case object DataCheckApprove extends FSMState

  case object Finish extends FSMState

  @derive(decoder, encoder, eqv, show)
  sealed trait Event
  case object Start       extends Event
  case object DataLoaded  extends Event
  case object DataFailed  extends Event
  case object DataChecked extends Event

  sealed trait Action {
    def execute[A, B](a: A => B): B = ???
    def rollback[A](a: A => ExitCode): ExitCode
  }

//  TODO: move somewhere
  case class ChangeFSMState(event: Event)
}
