package com.snowplow.techtest.domain.port

import com.snowplow.techtest.domain.SchemaId
import com.snowplow.techtest.domain.model.AppError
import io.circe.Json
import zio.{Has, IO, ZIO}

object SchemaRepository {

  //type definition
  type SchemaRepositoryEnv = Has[SchemaRepository.Service]

  //service def
  trait Service {
    def retrieve(id: SchemaId):            IO[AppError, Option[Json]]
    def store(schema: Json, id: SchemaId): IO[AppError, SchemaId]
  }

  //front-facing API
  def retrieve(id: SchemaId): ZIO[SchemaRepositoryEnv, AppError, Option[Json]] =
    ZIO.accessM[SchemaRepositoryEnv](_.get.retrieve(id))
  def store(schema: Json, id: SchemaId): ZIO[SchemaRepositoryEnv, AppError, SchemaId] =
    ZIO.accessM[SchemaRepositoryEnv](_.get.store(schema, id))
}
