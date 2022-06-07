package com.snowplow.techtest.adapter.service

import com.snowplow.techtest.domain.model.{AppError, SchemaNotFoundError}
import com.snowplow.techtest.domain.port.SchemaRepository
import com.snowplow.techtest.domain.port.SchemaRepository.{SchemaId, SchemaRepositoryEnv}
import io.circe.Json
import zio.{IO, ZLayer}

import scala.collection.mutable

object InMemorySchemaRepositoryService {
  val live: ZLayer[Any, AppError, SchemaRepositoryEnv] = ZLayer.succeed(new InMemorySchemaRepositoryService)
}

//TODO add logs all over this
class InMemorySchemaRepositoryService extends SchemaRepository.Service {

  val storage = new mutable.HashMap[SchemaId, Json]()

  //TODO creo que esto debería devolver Option y el SchemaManager pasarlo a AppError
  override def retrieve(id: SchemaId): IO[AppError, Json] =
    storage.get(id).fold(IO.fail(SchemaNotFoundError(id)))(IO.succeed(_))

  override def store(schema: Json, id: SchemaId): IO[AppError, SchemaId] = {
    storage.put(id, schema)
    IO.succeed(id)
  }
}
