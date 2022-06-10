package com.snowplow.techtest.adapter.service

import com.snowplow.techtest.StorageConfig
import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.fixtures.Fixtures
import zio.test.Assertion.{anything, fails, isTrue, succeeds}
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{ZIO, ZLayer}

import java.nio.file.{Files, Path}
import scala.util.Try

object InDiskSchemaRepositoryTest extends DefaultRunnableSpec with Fixtures {

  val testStorageConfig  = ZLayer.succeed(StorageConfig("/tmp"))
  val newFolder = "/tmp/new_folder"
  val newFolderConfig  = ZLayer.succeed(StorageConfig(newFolder))

  def spec: ZSpec[TestEnvironment, Failure] = {
    suite("InDiskSchemaRepository unit test")(
      suite("Existing folder")(
        testM("store and retrieve a schema") {
          for {
            storedId        <- ZIO.accessM[SchemaRepositoryEnv](_.get.store(configSchemaJson, schemaId))
            retrievedSchema <- ZIO.accessM[SchemaRepositoryEnv](_.get.retrieve(schemaId))
          } yield assert(retrievedSchema.get == configSchemaJson)(isTrue) && assert(storedId == schemaId)(isTrue)
        },
        testM("return None if a schema does not exist") {
          for {
            retrievedSchema <- ZIO.accessM[SchemaRepositoryEnv](_.get.retrieve("nonExistingSchema"))
          } yield assert(retrievedSchema.isEmpty)(isTrue)
        }
      ).provideSomeLayer[TestEnvironment](testStorageConfig >+> InDiskSchemaRepository.live),
      suite("Non existing folder")(
        testM("creates the folder and succeeds") {
          Try(Files.delete(Path.of(newFolder)))
          assertM(ZIO.accessM[SchemaRepositoryEnv](_.get.store(configSchemaJson, schemaId)).run)(succeeds(anything))
        }
      ).provideSomeLayer[TestEnvironment](newFolderConfig >+> InDiskSchemaRepository.live)
    )
  }

}
