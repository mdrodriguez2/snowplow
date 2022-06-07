package com.snowplow.techtest.http

import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.domain.service.{JsonValidationService, SchemaManager}
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
        for {
          schema <- req.as[Json]
          _ = println(s"received schema $schema")
          result <- SchemaManager.uploadSchema(schema, schemaId)
          res    <- Ok() //TODO hay que devolver el error correcto
        } yield res

      case GET -> Root / "schema" / schemaId =>
        for {
          result <- SchemaManager.retrieveSchema(schemaId)
          res    <- Ok(result.toString) //TODO hay que devolver el error correcto
        } yield res

      case req @ POST -> Root / "validate" / schemaId =>
        for {
          json <- req.as[Json]
          _ = println(s"received json $json")
          result <- JsonValidationService.validateJson(json, schemaId)
          res    <- Ok() //TODO hay que devolver el error correcto
        } yield res

    }
  }

}
