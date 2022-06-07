package com.snowplow.techtest.domain.service

import com.snowplow.techtest.domain.model.{AppError, SchemaValidationFailed}
import com.snowplow.techtest.domain.port.SchemaRepository.{SchemaId, SchemaRepositoryEnv}
import io.circe.Json
import io.restassured.module.jsv.JsonSchemaValidator
import zio.{IO, ZIO}

object JsonValidationService {

  def validateJson(json: Json, schemaId: SchemaId): ZIO[SchemaRepositoryEnv, AppError, SchemaId] =
    for {
      schema <- SchemaManager.retrieveSchema(schemaId)
      _      <- validate(schema, json, schemaId)
    } yield schemaId

  //TODO this is just a stub
  def validate(schema: Json, json: Json, schemaId: SchemaId): IO[AppError, Unit] = {
    val validator: JsonSchemaValidator = JsonSchemaValidator.matchesJsonSchema(schema.toString)
    val res                            = validator.matches(json.toString)
    if (res) IO.succeed((): Unit)
    else IO.fail(SchemaValidationFailed(schemaId))

  }

}