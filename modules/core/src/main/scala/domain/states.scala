package domain

import derevo.cats.eqv
import derevo.cats.show
import derevo.circe.magnolia.decoder
import derevo.circe.magnolia.encoder
import derevo.derive
import io.circe.Decoder
import io.circe.Encoder
import io.estatico.newtype.macros.newtype
import scala.util.control.NoStackTrace

object states {

  @newtype
  @derive(decoder, encoder, eqv, show)
  case class FSMState(state: FSMStateType)

  object FSMState {
    implicit val jsonEncoder: Encoder[FSMState] =
      Encoder.forProduct1("state")(_.state.toString)
  }

  /**
    * All states for finite state machine
    */
  @derive(decoder, encoder, eqv, show)
  sealed trait FSMStateType

  case object Init      extends FSMStateType
  case object None      extends FSMStateType
  case object Load      extends FSMStateType
  case object DataCheck extends FSMStateType
  case object Finish    extends FSMStateType

  @newtype
  case class Event(event: EventType)

  /**
    * Events to change FSM states
    */
  @derive(decoder, encoder, eqv, show)
  sealed trait EventType
  case object Unknown     extends EventType
  case object Reset       extends EventType
  case object Start       extends EventType
  case object DataLoaded  extends EventType
  case object DataChecked extends EventType

  @derive(eqv, show)
  @newtype
  case class ChangeFSMState(event: EventType)
  object ChangeFSMState {
    def fromString(s: String): ChangeFSMState = {
      s.toLowerCase match {
        case "start"       => ChangeFSMState(Start)
        case "reset"       => ChangeFSMState(Reset)
        case "dataloaded"  => ChangeFSMState(DataLoaded)
        case "datachecked" => ChangeFSMState(DataChecked)
        case _             => ChangeFSMState(Unknown)
      }
    }

    implicit val jsonEncoder: Encoder[ChangeFSMState] =
      Encoder.forProduct1("event")(_.event.toString)

    implicit val jsonDecoder: Decoder[ChangeFSMState] =
      Decoder.forProduct1("event")(ChangeFSMState.fromString)
  }

  sealed trait FSMError         extends NoStackTrace
  case object InvalidEventError extends FSMError
}
