package com.snowplow.techtest.domain.model

import com.snowplow.techtest.domain.SchemaId

trait AppError extends Exception {
  val message: String
}

final case class JsonValidationFailed(schemaId: SchemaId) extends AppError {
  override val message = s"Validation failed for schema $schemaId"
}

final case class SchemaNotFoundError(schemaId: SchemaId) extends AppError {
  override val message = s"Schema with ID $schemaId not found"
}

final case class StorageError(schemaId: SchemaId, error: String) extends AppError {
  override val message = s"error retrieving schema $schemaId from disk: $error"
}

final case class FolderCreationError(fullPath: String, error: String) extends AppError {
  override val message = s"error creating folder. FullPath: $fullPath. error: $error"
}
final case class WrongFolderError(path: String) extends AppError {
  override val message = s"incorrect folder $path for schema storage"
}
