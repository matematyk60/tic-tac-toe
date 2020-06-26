package com.dgodek.tictactoe.domain

trait GameFinishResolver {

  val firstDiagonal =
    List(Cords(Position(0), Position(0)), Cords(Position(1), Position(1)), Cords(Position(2), Position(2)))

  val secondDiagonal = List(
    Cords(Position(2), Position(0)),
    Cords(Position(1), Position(1)),
    Cords(Position(0), Position(2))
  )

  def isWinningMove(game: GameWithMoves, mark: Mark): Boolean = {
    def horizontally = {
      val row      = mark.cords.yPosition
      val rowMoves = game.moves.filter(_.mark.cords.yPosition == row)
      rowMoves.forall(_.mark.markType == mark.markType) && rowMoves.size == 3
    }
    def vertically = {
      val column      = mark.cords.xPosition
      val columnMoves = game.moves.filter(_.mark.cords.xPosition == column)
      columnMoves.forall(_.mark.markType == mark.markType) && columnMoves.size == 3
    }
    def diagonally = {
      val markTypeCords = game.moves.filter(_.mark.markType == mark.markType).map(_.mark.cords)
      firstDiagonal.forall(markTypeCords.contains) || secondDiagonal.forall(markTypeCords.contains)
    }

    diagonally || horizontally || vertically
  }

  def isFull(game: GameWithMoves): Boolean = game.moves.size == 9

}

object GameFinishResolver extends GameFinishResolver
