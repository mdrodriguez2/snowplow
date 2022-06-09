package com.snowplow.techtest.http

import com.snowplow.techtest.adapter.service.InMemorySchemaRepository
import com.snowplow.techtest.fixtures.Fixtures
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object ApiTest extends DefaultRunnableSpec with Fixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("App integration test")(
      testM("return a 404 if the schema does not exist") {
        for {
          storedId        <- SchemaManager.uploadSchema(configSchemaJson, schemaId)
          retrievedSchema <- SchemaManager.retrieveSchema(schemaId)
        } yield assert(retrievedSchema == configSchemaJson)(isTrue) && assert(storedId == schemaId)(isTrue)
      },
      testM("return an error if a schema does not exist") {
        assertM(SchemaManager.retrieveSchema("nonExistingSchema").run)(fails(anything)) //TODO this should be a typed error
      }
    ).provideSomeLayer[TestEnvironment](InMemorySchemaRepository.live)

}
