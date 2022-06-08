package com.snowplow.techtest.http

import com.snowplow.techtest.domain.model.{JsonValidationFailed, SchemaNotFoundError}
import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.domain.service.{JsonValidationService, SchemaManager}
import com.snowplow.techtest.http.model.Response.{jsonInvalid, jsonValid, schemaNotFound, schemaUploadedOk}
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
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
            case other => InternalServerError(other.getMessage) //TODO MANUEL acabar
          },
          id => Ok(schemaUploadedOk(id).toString) //TODO MANUEL hacer que esto sea un JSON
        )

      case GET -> Root / "schema" / schemaId =>
        for {
          result <- SchemaManager.retrieveSchema(schemaId)
          res    <- Ok(result.toString) //TODO quitar el toString y que rule
        } yield res

      case req @ POST -> Root / "validate" / schemaId =>
        (for {
          json <- req.as[Json]
          _ = println(s"received json $json")
          validatedId <- JsonValidationService.validateJson(json, schemaId)
        } yield validatedId).foldM(
          {
            case error: JsonValidationFailed => BadRequest(jsonInvalid(schemaId, error.message).toString)
            case error: SchemaNotFoundError  => BadRequest(schemaNotFound(schemaId, error.message).toString)
            case other                       => InternalServerError(other.getMessage)
          },
          id => Ok(jsonValid(id).toString)
        )

    }
  }

}
