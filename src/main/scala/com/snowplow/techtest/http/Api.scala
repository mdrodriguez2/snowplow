package com.snowplow.techtest.http

import com.snowplow.techtest.domain.model.{JsonValidationFailed, SchemaNotFoundError}
import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.domain.service.{JsonValidationService, SchemaManager}
import com.snowplow.techtest.http.model.Action.{RetrieveSchema, ValidateDocument}
import com.snowplow.techtest.http.model.Response._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, MalformedMessageBodyFailure}
import zio._
import zio.interop.catz._

final case class Api[R <: SchemaRepositoryEnv](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]

  import dsl._

  val route: HttpRoutes[UserTask] = {
    HttpRoutes.of[UserTask] {
      case GET -> Root / "healthcheck" => NoContent()

      case req @ POST -> Root / "schema" / schemaId =>
        (for {
          schema   <- req.as[Json]
          storedId <- SchemaManager.uploadSchema(schema, schemaId)
        } yield storedId).foldM(
          {
            case _: MalformedMessageBodyFailure => BadRequest(malformedSchema(schemaId).asJson)
            case other                          => InternalServerError(unknownError(other.getMessage).asJson)
          },
          id => Created(schemaUploadedOk(id).asJson)
        )

      case GET -> Root / "schema" / schemaId =>
        SchemaManager
          .retrieveSchema(schemaId)
          .foldM(
            {
              case err: SchemaNotFoundError => NotFound(schemaNotFound(RetrieveSchema, schemaId, err.message).asJson)
              case other                    => InternalServerError(unknownError(other.getMessage).asJson)
            },
            schema => Ok(schema.asJson)
          )

      case req @ POST -> Root / "validate" / schemaId =>
        (for {
          json        <- req.as[Json]
          validatedId <- JsonValidationService.validateJson(json, schemaId)
        } yield validatedId).foldM(
          {
            case _: MalformedMessageBodyFailure => BadRequest(jsonInvalid(schemaId, "Invalid JSON").asJson)
            case error: JsonValidationFailed    => BadRequest(jsonInvalid(schemaId, error.message).asJson)
            case error: SchemaNotFoundError =>
              BadRequest(schemaNotFound(ValidateDocument, schemaId, error.message).asJson)
            case other => InternalServerError(unknownError(other.getMessage).asJson)
          },
          id => Ok(jsonValid(id).asJson)
        )
    }
  }

}
