package com.snowplow.techtest

import cats.effect.{ExitCode => CatsExitCode}
import com.snowplow.techtest.adapter.service.InMemorySchemaRepository
import com.snowplow.techtest.configuration.Configuration
import com.snowplow.techtest.domain.port.SchemaRepository.SchemaRepositoryEnv
import com.snowplow.techtest.http.Api
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {
  val ec: ExecutionContextExecutor = ExecutionContext.global
  type AppEnvironment = Configuration with Clock with SchemaRepositoryEnv

  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment = Configuration.live ++ InMemorySchemaRepository.live

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        api <- configuration.apiConfig
        httpApp = Router[AppTask](
          "/" -> Api(s"${api.endpoint}/").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask](ec)
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, CatsExitCode]
            .drain
        }
      } yield server

    val a: ZIO[zio.ZEnv, Throwable, Unit] = program
      .provideSomeLayer[ZEnv](appEnvironment)

    a.tapError(err => putStrLn(s"Execution failed with: $err")).exitCode
  }
}
