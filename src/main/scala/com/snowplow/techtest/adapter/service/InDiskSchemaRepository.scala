package com.snowplow.techtest.adapter.service

import com.snowplow.techtest.StorageConfig
import com.snowplow.techtest.domain.model.{AppError, FolderCreationError, StorageError}
import com.snowplow.techtest.domain.port.SchemaRepository
import com.snowplow.techtest.domain.port.SchemaRepository.{SchemaId, SchemaRepositoryEnv}
import io.circe.Json
import io.circe.parser._
import zio.{IO, _}

import java.nio.file.{Files, Path, Paths}

object InDiskSchemaRepository {
  val live: ZLayer[Has[StorageConfig], Nothing, SchemaRepositoryEnv] = ZLayer.fromService(new InDiskSchemaRepository(_))
}

//TODO add logs all over this
class InDiskSchemaRepository(config: StorageConfig) extends SchemaRepository.Service {

  private def buildPath(path: String, id: SchemaId): String = s"$path/$id"

  override def retrieve(id: SchemaId): IO[AppError, Option[Json]] = {
    if (Files.exists(Paths.get(buildPath(config.path, id)))) {
      val str = Files.readString(Paths.get(buildPath(config.path, id)))
      parse(str).fold(err => IO.fail(StorageError(id, err.getMessage())), json => IO.succeed(Some(json)))
    } else IO.succeed(None)
  }

  override def store(schema: Json, id: SchemaId): IO[AppError, SchemaId] =
    for {
      _ <- createDirectoriesIfRequired(config.path)
      _ <- IO.succeed(Files.writeString(Paths.get(buildPath(config.path, id)), schema.toString))
    } yield id

  private def createDirectoriesIfRequired(path: String): IO[AppError, Unit] =
    if (Files.exists(Path.of(path))) IO.succeed((): Unit)
    else IO[Unit](Files.createDirectories(Path.of(path))).mapError(err => FolderCreationError(path, err.getMessage))
}
