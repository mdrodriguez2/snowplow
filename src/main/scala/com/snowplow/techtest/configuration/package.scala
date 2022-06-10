package com.snowplow.techtest

import pureconfig.ConfigSource
import zio._

package object configuration {

  type Configuration = Has[ApiConfig] with Has[StorageConfig]

  val apiConfig: URIO[Has[ApiConfig], ApiConfig]             = ZIO.access(_.get)
  val storageConfig: URIO[Has[StorageConfig], StorageConfig] = ZIO.access(_.get)

  object Configuration {

    import pureconfig.generic.auto._

    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.storage))
    )
  }
}

final case class AppConfig(api: ApiConfig, storage: StorageConfig)

final case class ApiConfig(endpoint: String, port: Int)
final case class StorageConfig(path: String)
