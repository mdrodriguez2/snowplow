package com.snowplow.techtest.http

import cats.effect.Blocker
import com.snowplow.techtest.Main
import com.snowplow.techtest.adapter.service.InMemorySchemaRepository
import com.snowplow.techtest.configuration.Configuration
import com.snowplow.techtest.fixtures.Fixtures
import io.circe.parser._
import io.circe.{Decoder, Encoder, Json}
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.JavaNetClientBuilder
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import zio.interop.catz._
import zio.{Task, ZIO}

import java.time.Duration.ofSeconds
import scala.concurrent.ExecutionContext

class IntegrationTest extends AnyFlatSpecLike with Matchers with Fixtures {

  val ec: ExecutionContext = ExecutionContext.global

  val testEnvironment = Configuration.live ++ InMemorySchemaRepository.live
  val blocker         = Blocker.liftExecutionContext(ec)

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]

  "healthcheck" should "work as expected" in {
    val action = for {
      _        <- Main.server.fork
      _        <- ZIO.sleep(ofSeconds(3))
      client   <- Task(JavaNetClientBuilder[Task](blocker).create)
      response <- client.statusFromString("http://localhost:8080/healthcheck")
    } yield response

    val result: Either[Throwable, Status] =
      zio.Runtime.default.unsafeRun(action.provideCustomLayer(testEnvironment).either)
    result.right.get.code shouldBe 204
  }

  "/schema" should "accept a schema, store it and return it" in {
    val action = for {
      _      <- Main.server.fork
      _      <- ZIO.sleep(ofSeconds(3))
      client <- Task(JavaNetClientBuilder[Task](blocker).create)
      post <- client.expect[Json](
        Request(
          Method.POST,
          Uri.fromString(s"http://localhost:8080/schema/$schemaId").right.get
        ).withEntity(configSchemaJson)
      )
      get <- client.expect[String](s"http://localhost:8080/schema/$schemaId")
    } yield get

    val result = zio.Runtime.default.unsafeRun(action.provideCustomLayer(testEnvironment).either).right.get
    parse(result).right.get shouldBe configSchemaJson
  }

  "/validate" should "validate a schema" in {
    val action = for {
      _      <- Main.server.fork
      _      <- ZIO.sleep(ofSeconds(3))
      client <- Task(JavaNetClientBuilder[Task](blocker).create)
      post <- client.expect[Json](
        Request(
          Method.POST,
          Uri.fromString(s"http://localhost:8080/schema/$schemaId").right.get
        ).withEntity(configSchemaJson)
      )
      status <- client.status(
        Request(
          Method.POST,
          Uri.fromString(s"http://localhost:8080/validate/$schemaId").right.get
        ).withEntity(jsonToValidate)
      )
    } yield status

    val result = zio.Runtime.default.unsafeRun(action.provideCustomLayer(testEnvironment).either).right.get
    result.code shouldBe 200
  }

}
