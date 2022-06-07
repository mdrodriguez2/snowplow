package com.snowplow.techtest

import pureconfig.ConfigSource
import zio._

package object configuration {

  type Configuration = Has[ApiConfig]

  final case class AppConfig(api: ApiConfig)


  final case class ApiConfig(endpoint: String, port: Int)

  val apiConfig: URIO[Has[ApiConfig], ApiConfig] = ZIO.access(_.get)

  object Configuration {

    import pureconfig.generic.auto._

    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api))
    )
  }
}
