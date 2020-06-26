package com.dgodek.tictactoe.infrastructure.doobie

import com.dgodek.tictactoe.domain.{Game, GameId, GameQuery, GameRepository, GameStatus, GameWithMoves, Move, PlayerId}
import doobie.free.connection.ConnectionIO
import cats.syntax.functor.toFunctorOps

object DoobieGameRepository extends GameRepository[ConnectionIO] {

  override def findGameWithMoves(gameQuery: GameQuery): ConnectionIO[Option[GameWithMoves]] =
    GameQueries
      .findGameWithMoves(gameQuery)
      .to[List]
      .map(_.groupBy {
        case (game, _) => game
      }.map {
        case (game, gameWithMoves) => GameWithMoves(game, gameWithMoves.flatMap(_._2.toList))
      }.headOption)

  override def findGamesWithMoves(gameQuery: GameQuery): ConnectionIO[List[GameWithMoves]] =
    GameQueries
      .findGameWithMoves(gameQuery)
      .to[List]
      .map(_.groupBy {
        case (game, _) => game
      }.map {
        case (game, gameWithMoves) => GameWithMoves(game, gameWithMoves.flatMap(_._2.toList))
      }.toList)

  override def saveGame(game: Game): ConnectionIO[Unit] =
    GameQueries.insertGame.run(game).as(())

  override def saveMove(move: Move): ConnectionIO[Unit] =
    GameQueries.insertMove.run(move).as(())

  override def updateGameStatus(id: GameId, status: GameStatus): ConnectionIO[Unit] =
    GameQueries.updateGameStatus(id, status, winnerPlayerId = None).run.map(_ => ())

  override def updateSecondPlayerId(gameId: GameId, playerId: PlayerId): ConnectionIO[Unit] =
    GameQueries.updateSecondPlayerId(gameId = gameId, playerId).run.map(_ => ())

  override def updateGameStatusAndWinner(id: GameId, status: GameStatus, winnerId: PlayerId): ConnectionIO[Unit] =
    GameQueries.updateGameStatus(id, status, Some(winnerId)).run.map(_ => ())
}
