package com.dgodek.tictactoe.domain

import java.time.LocalDateTime

import enumeratum.{CirceEnum, DoobieEnum, Enum, EnumEntry}

case class GameId(value: Int)              extends AnyVal
case class StartTime(value: LocalDateTime) extends AnyVal
case class PlayerId(value: Int)            extends AnyVal
case class SequenceNumber(value: Int) extends AnyVal {
  def next: SequenceNumber = SequenceNumber(value + 1)
}

object SequenceNumber {
  val first: SequenceNumber = SequenceNumber(0)
}

case class Position(value: Int) extends AnyVal

object Position {
  val minPosition = 0
  val maxPosition = 2
  def createPosition(value: Int): Option[Position] =
    Either.cond((0 to maxPosition).contains(value), Position(value), ()).toOption
}

sealed trait GameStatus extends EnumEntry

object GameStatus extends Enum[GameStatus] with DoobieEnum[GameStatus] with CirceEnum[GameStatus] {
  override def values: IndexedSeq[GameStatus] = findValues

  case object InProgress extends GameStatus
  case object Tied       extends GameStatus
  case object Finished   extends GameStatus
}

case class Game(
    id: GameId,
    startTime: StartTime,
    firstPlayerId: PlayerId,
    secondPlayerId: Option[PlayerId],
    status: GameStatus,
    winnerPlayerId: Option[PlayerId]
) {
  def playerParticipates(playerId: PlayerId): Boolean = firstPlayerId == playerId || secondPlayerId.contains(playerId)
}

object Game {
  def emptyGame(id: GameId, startTime: StartTime, firstPlayerId: PlayerId): Game =
    Game(
      id,
      startTime,
      firstPlayerId = firstPlayerId,
      status = GameStatus.InProgress,
      secondPlayerId = None,
      winnerPlayerId = None
    )
}

case class GameQuery(
    playerId: Option[PlayerId] = None,
    gameId: Option[GameId] = None
)

sealed trait MarkType extends EnumEntry

object MarkType extends Enum[MarkType] with DoobieEnum[MarkType] with CirceEnum[MarkType] {
  override def values: IndexedSeq[MarkType] = findValues
  case object Nought extends MarkType
  case object Cross  extends MarkType
}

case class Cords(
    xPosition: Position,
    yPosition: Position
)

case class Mark(
    cords: Cords,
    markType: MarkType
)

case class Move(
    gameId: GameId,
    sequenceNumber: SequenceNumber,
    playerId: PlayerId,
    mark: Mark
)

case class GameWithMoves(
    game: Game,
    moves: List[Move]
) {
  lazy val lastPlayerId: Option[PlayerId] = previousMove.map(_.playerId)

  lazy val occupiedPositions: List[Cords] =
    moves.map(_.mark.cords)

  lazy val previousMove: Option[Move] = moves.maxByOption(_.sequenceNumber.value)

  def withNewMove(move: Move): GameWithMoves = this.copy(moves = this.moves :+ move)
}
