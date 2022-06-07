package com.snowplow.techtest.domain.service

import io.circe.Json
import zio.ZIO

object SchemaManager {
  def uploadSchema(json: Json): ZIO[SchemaStorageEnv, AppError, ]

}
