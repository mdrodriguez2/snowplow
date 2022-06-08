package com.snowplow.techtest.adapter.service

import com.snowplow.techtest.domain.model.AppError
import com.snowplow.techtest.domain.port.SchemaRepository
import com.snowplow.techtest.domain.port.SchemaRepository.{SchemaId, SchemaRepositoryEnv}
import io.circe.Json
import zio.{IO, ZLayer}

import scala.collection.mutable

object InMemorySchemaRepository {
  val live: ZLayer[Any, Nothing, SchemaRepositoryEnv] = ZLayer.succeed(new InMemorySchemaRepository)
}

//TODO add logs all over this
class InMemorySchemaRepository extends SchemaRepository.Service {

  val storage = new mutable.HashMap[SchemaId, Json]()

  override def retrieve(id: SchemaId): IO[AppError, Option[Json]] =
    IO.succeed(storage.get(id))

  override def store(schema: Json, id: SchemaId): IO[AppError, SchemaId] = {
    storage.put(id, schema)
    IO.succeed(id)
  }
}
