package com.snowplow.techtest.domain.service

import com.snowplow.techtest.adapter.service.InMemorySchemaRepository
import com.snowplow.techtest.fixtures.Fixtures
import zio.test.Assertion.isTrue
import zio.test.environment.TestEnvironment
import zio.test.{assert, DefaultRunnableSpec, ZSpec}

object JsonValidationServiceTest extends DefaultRunnableSpec with Fixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("JsonValidationService unit test")(
      testM("validate a correct schema without null values ") {
        for {
          _           <- SchemaManager.uploadSchema(configSchemaJson, schemaId)
          validatedId <- JsonValidationService.validateJson(jsonToValidateNoNullValues, schemaId)
        } yield assert(validatedId == schemaId)(isTrue)
      },
      testM("validate a correct schema without non-required fields ") {
        for {
          _           <- SchemaManager.uploadSchema(configSchemaJson, schemaId)
          validatedId <- JsonValidationService.validateJson(jsonToValidateWithoutNonRequiredFields, schemaId)
        } yield assert(validatedId == schemaId)(isTrue)
      },
      testM("validate a correct schema with null fields ") {
        for {
          _           <- SchemaManager.uploadSchema(configSchemaJson, schemaId)
          validatedId <- JsonValidationService.validateJson(jsonToValidateWithNullFields, schemaId)
        } yield assert(validatedId == schemaId)(isTrue)
      },
      testM("validate the input example ") {
        for {
          _           <- SchemaManager.uploadSchema(configSchemaJson, schemaId)
          validatedId <- JsonValidationService.validateJson(jsonToValidate, schemaId)
        } yield assert(validatedId == schemaId)(isTrue)
      }
    ).provideSomeLayer[TestEnvironment](InMemorySchemaRepository.live)

}
