package com.dgodek.tictactoe.domain

import simulacrum.typeclass

@typeclass
trait GameRepository[DBIO[_]] {
  def findGameWithMoves(gameQuery: GameQuery): DBIO[Option[GameWithMoves]]
  def findGamesWithMoves(gameQuery: GameQuery): DBIO[List[GameWithMoves]]
  def saveGame(game: Game): DBIO[Unit]
  def saveMove(move: Move): DBIO[Unit]
  def updateGameStatus(id: GameId, status: GameStatus): DBIO[Unit]
  def updateGameStatusAndWinner(id: GameId, status: GameStatus, winnerId: PlayerId): DBIO[Unit]
  def updateSecondPlayerId(gameId: GameId, playerId: PlayerId): DBIO[Unit]
}
