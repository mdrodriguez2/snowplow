package com.snowplow.techtest.adapter.service

import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.fixtures.Fixtures
import zio.ZIO
import zio.test.Assertion.isTrue
import zio.test.environment.TestEnvironment
import zio.test._

object InMemorySchemaRepositoryTest extends DefaultRunnableSpec with Fixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("InMemorySchemaRepository unit test")(
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
    ).provideSomeLayer[TestEnvironment](InMemorySchemaRepository.live)

}
