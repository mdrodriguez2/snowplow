package com.snowplow.techtest.http.model
import com.snowplow.techtest.http.model.Action.{ErrorAction, UploadSchema, ValidateDocument}
import io.circe.{Encoder, Json}

case class Response(action: Action, id: String, status: Status, message: Option[String])

sealed abstract class Status {
  val status: String
  override def toString: String = status
}

object Status {
  case object Success extends Status {
    override val status = "success"
  }
  case object Error extends Status {
    override val status = "error"
  }
}

sealed abstract class Action {
  val action: String
  override def toString: String = action
}

object Action {
  case object UploadSchema extends Action {
    override val action = "uploadSchema"
  }
  case object ValidateDocument extends Action {
    override val action = "validateDocument"
  }
  case object RetrieveSchema extends Action {
    override val action = "retrieveSchema"
  }

  case object ErrorAction extends Action {
    override val action = "error"
  }
}

object Response {
  def schemaUploadedOk(schemaId: String): Response = Response(UploadSchema, schemaId, Status.Success, None)

  def jsonValid(schemaId: String): Response = Response(ValidateDocument, schemaId, Status.Success, None)

  def jsonInvalid(schemaId: String, message: String): Response =
    Response(ValidateDocument, schemaId, Status.Error, Some(message))

  def schemaNotFound(action: Action, schemaId: String, message: String): Response =
    Response(action, schemaId, Status.Error, Some(message))

  def unknownError(errorMessage: String): Response = Response(ErrorAction, "Error", Status.Error, Some(errorMessage))

  def malformedSchema(schemaId: String): Response =
    Response(UploadSchema, schemaId, Status.Error, Some("Invalid JSON"))

  implicit val encodeResponse: Encoder[Response] = new Encoder[Response] {
    final def apply(a: Response): Json =
      Json
        .obj(
          ("action", Json.fromString(a.action.toString)),
          ("id", Json.fromString(a.id)),
          ("status", Json.fromString(a.status.toString)),
          ("message", a.message.fold(Json.Null)(s => Json.fromString(s)))
        )
        .deepDropNullValues
  }

}
