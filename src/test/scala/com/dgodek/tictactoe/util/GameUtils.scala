package com.dgodek.tictactoe.util

import java.time.LocalDateTime

import com.dgodek.tictactoe.domain.{Game, GameId, GameStatus, GameWithMoves, PlayerId, StartTime}

object GameUtils {

  val gameId: GameId = GameId(1)

  val firstPlayerId: PlayerId  = PlayerId(1)
  val secondPlayerId: PlayerId = PlayerId(2)

  val finishedGame: Game = Game(
    gameId,
    StartTime(LocalDateTime.now()),
    firstPlayerId,
    Some(secondPlayerId),
    GameStatus.Finished,
    Some(firstPlayerId)
  )

  val game: Game = Game(
    gameId,
    StartTime(LocalDateTime.now()),
    firstPlayerId,
    secondPlayerId = None,
    GameStatus.InProgress,
    None
  )

  val emptyGame: GameWithMoves = GameWithMoves(
    game,
    moves = List.empty
  )
}
