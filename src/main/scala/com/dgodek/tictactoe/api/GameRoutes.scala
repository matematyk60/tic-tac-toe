package com.dgodek.tictactoe.api

import cats.effect.Sync
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.{~>, Applicative, Monad}
import com.dgodek.tictactoe.api.GameRequests.MakeMoveRequest
import com.dgodek.tictactoe.app.GameService
import com.dgodek.tictactoe.domain.GameErrors.{JoinGameError, MakeMoveError}
import com.dgodek.tictactoe.domain._
import io.circe.Json
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Headers, HttpRoutes, Response, Status}

object GameRoutes {

  import GameCodecs._

  def routes[F[_]: Sync: TimeProvider, DBIO[_]: Monad: GameRepository](
      implicit idProvider: IdProvider[F, GameId],
      tx: DBIO ~> F,
      gameFinishResolver: GameFinishResolver): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {

      case GET -> Root / "games" =>
        tx(GameRepository[DBIO].findGamesWithMoves(GameQuery())).flatMap(
          games => Ok(Json.obj("games" -> Json.arr(games.map(gameWithMovesEncoder.apply): _*)))
        )

      case GET -> Root / "games" / IntVar(gameIdInPath) =>
        val gameId = GameId(gameIdInPath)
        tx(GameRepository[DBIO].findGameWithMoves(GameQuery(gameId = Some(gameId)))).flatMap {
          case Some(game) => Ok(game)
          case None       => NotFound()
        }

      case req @ POST -> Root / "games" =>
        val playerId = playerIdFromHeaderOrDefault(req.headers)
        GameService
          .createNewGame[F, DBIO](playerId)
          .flatMap(gameId => Created(Json.obj("gameId" -> gameIdEncoder(gameId))))

      case req @ POST -> Root / "games" / IntVar(gameIdInPath) / "moves" =>
        val gameId   = GameId(gameIdInPath)
        val playerId = playerIdFromHeaderOrDefault(req.headers)
        for {
          makeMoveRequest <- req.as[MakeMoveRequest]
          result <- GameService
            .makeMove[F, DBIO](playerId, gameId, xPosition = makeMoveRequest.x, yPosition = makeMoveRequest.y)
          response <- result match {
            case Left(error) =>
              val statusCode = error match {
                case MakeMoveError.NoSuchGame => Status.NotFound
                case _                        => Status.UnprocessableEntity
              }
              Applicative[F].pure(
                Response[F](statusCode).withEntity(Json.obj("error" -> MakeMoveError.circeEncoder(error))))
            case Right(gameId) => Ok(Json.obj("gameId" -> gameIdEncoder(gameId)))
          }
        } yield response

      case req @ POST -> Root / "games" / IntVar(gameIdInPath) / "players" =>
        val gameId   = GameId(gameIdInPath)
        val playerId = playerIdFromHeaderOrDefault(req.headers)
        for {
          result <- GameService.joinGame[F, DBIO](playerId, gameId)
          response <- result match {
            case Left(error) =>
              val statusCode = error match {
                case JoinGameError.NoSuchGame => Status.NotFound
                case _                        => Status.UnprocessableEntity
              }
              Applicative[F].pure(
                Response[F](statusCode).withEntity(Json.obj("error" -> JoinGameError.circeEncoder(error))))
            case Right(gameId) =>
              Ok(Json.obj("gameId" -> gameIdEncoder(gameId)))
          }
        } yield response
    }
  }

  private def playerIdFromHeaderOrDefault(headers: Headers) =
    PlayerId(headers.get(CaseInsensitiveString("player-id")).map(_.value.toInt).getOrElse(1))
}
