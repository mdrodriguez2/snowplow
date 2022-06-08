package com.snowplow.techtest.http.model

case class Response(action: String, id: String, status: Status.Value, message: Option[String])

object Status extends Enumeration {
  val success: Status.Value = Value("success")
  val error: Status.Value   = Value("error")
}

object Response {
  def schemaUploadedOk(schemaId: String): Response = Response("uploadSchema", schemaId, Status.success, None)

  def jsonValid(schemaId: String): Response = Response("validateDocument", schemaId, Status.success, None)

  def jsonInvalid(schemaId: String, message: String): Response =
    Response("validateDocument", schemaId, Status.error, Some(message))

  def schemaNotFound(schemaId: String, message: String): Response =
    Response("validateDocument", schemaId, Status.error, Some(message))

}
