package com.snowplow.techtest.http.model
import io.circe.{Encoder, Json}

case class Response(action: String, id: String, status: Status, message: Option[String])

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

object Response {
  def schemaUploadedOk(schemaId: String): Response = Response("uploadSchema", schemaId, Status.Success, None)

  def jsonValid(schemaId: String): Response = Response("validateDocument", schemaId, Status.Success, None)

  def jsonInvalid(schemaId: String, message: String): Response =
    Response("validateDocument", schemaId, Status.Error, Some(message))

  def schemaNotFound(schemaId: String, message: String): Response =
    Response("validateDocument", schemaId, Status.Error, Some(message))

  def unknownError(errorMessage: String): Response = Response("Error", "Error", Status.Error, Some(errorMessage))

  def malformedSchema(schemaId: String): Response =
    Response("uploadSchema", schemaId, Status.Error, Some("Invalid JSON"))

  implicit val encodeResponse: Encoder[Response] = new Encoder[Response] {
    final def apply(a: Response): Json =
      Json
        .obj(
          ("action", Json.fromString(a.action)),
          ("id", Json.fromString(a.id)),
          ("status", Json.fromString(a.status.toString)),
          ("message", a.message.fold(Json.Null)(s => Json.fromString(s)))
        )
        .deepDropNullValues
  }

}
