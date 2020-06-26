package com.dgodek.tictactoe.app

import cats.data.EitherT
import cats.data.EitherT.catsDataBifunctorForEitherT
import cats.syntax.bifunctor.toBifunctorOps
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.{~>, Applicative, Functor, Monad}
import com.dgodek.tictactoe.domain.GameErrors._
import com.dgodek.tictactoe.domain._

object GameService {

  def createNewGame[F[_]: Monad: TimeProvider, DBIO[_]: Functor: GameRepository](
      playerId: PlayerId)(implicit gameIdProvider: IdProvider[F, GameId], tx: DBIO ~> F): F[GameId] =
    for {
      currentTime <- TimeProvider[F].currentTime()
      gameId      <- gameIdProvider.generateId()
      game = Game.emptyGame(gameId, StartTime(currentTime), playerId)
      _ <- tx(GameRepository[DBIO].saveGame(game))
    } yield gameId

  def joinGame[F[_], DBIO[_]: Monad: GameRepository](playerId: PlayerId, gameId: GameId)(
      implicit tx: DBIO ~> F): F[Either[JoinGameError, GameId]] = {
    val joinGameAction: EitherT[DBIO, JoinGameError, GameId] = for {
      game <- EitherT
        .fromOptionF(
          GameRepository[DBIO].findGameWithMoves(GameQuery(gameId = Some(gameId))),
          ifNone = JoinGameError.NoSuchGame)
        .leftWiden[JoinGameError]
      _ <- EitherT.cond(game.game.secondPlayerId.isEmpty, (), JoinGameError.GameIsFull).leftWiden[JoinGameError]
      _ <- EitherT.right(GameRepository[DBIO].updateSecondPlayerId(gameId, playerId))
    } yield gameId
    tx(joinGameAction.value)
  }

  def makeMove[F[_], DBIO[_]: Monad: GameRepository](
      playerId: PlayerId,
      gameId: GameId,
      xPosition: Int,
      yPosition: Int)(
      implicit tx: DBIO ~> F,
      gameFinishResolver: GameFinishResolver): F[Either[MakeMoveError, GameId]] = {
    def validateMove(game: GameWithMoves): Either[MakeMoveError, Move] =
      for {
        _         <- Either.cond(game.game.status == GameStatus.InProgress, (), MakeMoveError.GameFinished)
        xPosition <- Position.createPosition(xPosition).toRight(MakeMoveError.InvalidPosition)
        yPosition <- Position.createPosition(yPosition).toRight(MakeMoveError.InvalidPosition)
        _ <- Either
          .cond(game.game.playerParticipates(playerId), (), MakeMoveError.UserNotGamePlayer)
        _ <- game.lastPlayerId match {
          case Some(lastPlayerId) if lastPlayerId == playerId =>
            Left(MakeMoveError.NotPlayersTurn)
          case _ => Right(())
        }
        markType = if (playerId == game.game.firstPlayerId) MarkType.Cross else MarkType.Nought
        mark     = Mark(Cords(xPosition, yPosition), markType)
        _ <- Either.cond(!game.occupiedPositions.contains(mark.cords), (), MakeMoveError.PositionTaken)
        sequence = game.previousMove.map(_.sequenceNumber.next).getOrElse(SequenceNumber.first)
      } yield Move(gameId, sequence, playerId, mark)

    def updateGameStatusIfFinished(game: GameWithMoves, mark: Mark) =
      if (gameFinishResolver.isWinningMove(game, mark))
        GameRepository[DBIO].updateGameStatusAndWinner(game.game.id, GameStatus.Finished, playerId)
      else if (gameFinishResolver.isFull(game)) GameRepository[DBIO].updateGameStatus(game.game.id, GameStatus.Tied)
      else Applicative[DBIO].unit

    val makeMoveAction: EitherT[DBIO, MakeMoveError, GameId] = for {
      game <- EitherT
        .fromOptionF[DBIO, MakeMoveError, GameWithMoves](
          GameRepository[DBIO].findGameWithMoves(GameQuery(gameId = Some(gameId))),
          ifNone = MakeMoveError.NoSuchGame)
      move <- EitherT.fromEither(validateMove(game))
      updatedGame = game.withNewMove(move)
      _ <- EitherT.right(GameRepository[DBIO].saveMove(move))
      _ <- EitherT.right(updateGameStatusIfFinished(updatedGame, move.mark))

    } yield updatedGame.game.id
    tx(makeMoveAction.value)
  }

}