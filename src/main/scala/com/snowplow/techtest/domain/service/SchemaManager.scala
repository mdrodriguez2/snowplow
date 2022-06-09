package com.snowplow.techtest.domain.service

import com.snowplow.techtest.domain.model.{AppError, SchemaNotFoundError}
import com.snowplow.techtest.domain.port.SchemaRepository
import com.snowplow.techtest.domain.port.SchemaRepository.{SchemaId, SchemaRepositoryEnv}
import io.circe.Json
import zio.ZIO

object SchemaManager {

  def uploadSchema(schema: Json, id: SchemaId): ZIO[SchemaRepositoryEnv, AppError, SchemaId] =
    SchemaRepository.store(schema, id)

  def retrieveSchema(id: SchemaId): ZIO[SchemaRepositoryEnv, AppError, Json] =
    SchemaRepository
      .retrieve(id)
      .flatMap(
        _.fold[ZIO[SchemaRepositoryEnv, AppError, Json]](ZIO.fail(SchemaNotFoundError(id)))(json => ZIO.succeed(json))
      )

}
