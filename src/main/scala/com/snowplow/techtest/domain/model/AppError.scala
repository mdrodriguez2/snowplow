package com.snowplow.techtest.domain.model

trait AppError extends Exception {
  val message: String
}

final case class SchemaValidationFailed(schemaId: String) extends AppError {
  override val message = s"Validation failed for schema $schemaId"
}

final case class SchemaNotFoundError(schemaId: String) extends AppError {
  override val message = s"Schema with ID $schemaId not found"
}


