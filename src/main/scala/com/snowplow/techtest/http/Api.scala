package com.snowplow.techtest.http

import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import zio._
import zio.interop.catz._

final case class Api[R](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] = jsonEncoderOf[UserTask, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]

  import dsl._

  val route: HttpRoutes[UserTask] = {
    HttpRoutes.of[UserTask] {
      case GET -> Root / "healthcheck" => NoContent()

      //TODO remove this, quick test for JSON decoding
      case req@POST -> Root / "test" =>
        for {
          a <- req.as[Json]
          _ = println(s"received string $a")
          res <- Ok()
        } yield res

    }
  }

}
