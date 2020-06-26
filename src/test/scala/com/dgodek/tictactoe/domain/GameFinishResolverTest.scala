package com.dgodek.tictactoe.domain

import com.dgodek.tictactoe.util.GameUtils._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameFinishResolverTest extends AnyFlatSpec with Matchers {

  "GameFinishResolver.isWinningMove" should "return false for first move" in {
    val move =
      Move(
        gameId,
        SequenceNumber(0),
        PlayerId(1),
        Mark(Cords(xPosition = Position(1), yPosition = Position(1)), MarkType.Cross))
    val game = emptyGame.withNewMove(move)

    GameFinishResolver.isWinningMove(game, move.mark) shouldBe false
  }

  it should "return true for winning in rows" in {
    (0 to 2).foreach { rowNumber =>
      val firstMove =
        Move(
          gameId,
          SequenceNumber(0),
          PlayerId(1),
          Mark(Cords(xPosition = Position(0), yPosition = Position(rowNumber)), MarkType.Cross))

      val secondMove =
        Move(
          gameId,
          SequenceNumber(1),
          PlayerId(1),
          Mark(Cords(xPosition = Position(1), yPosition = Position(rowNumber)), MarkType.Cross))

      val thirdMove =
        Move(
          gameId,
          SequenceNumber(2),
          PlayerId(1),
          Mark(Cords(xPosition = Position(2), yPosition = Position(rowNumber)), MarkType.Cross))

      val game = emptyGame.withNewMove(firstMove).withNewMove(secondMove).withNewMove(thirdMove)

      GameFinishResolver.isWinningMove(game, thirdMove.mark) shouldBe true
    }
  }

  it should "return true for winning in columns" in {
    (0 to 2).foreach { columnNumber =>
      val firstMove =
        Move(
          gameId,
          SequenceNumber(0),
          PlayerId(1),
          Mark(Cords(xPosition = Position(columnNumber), yPosition = Position(0)), MarkType.Cross))

      val secondMove =
        Move(
          gameId,
          SequenceNumber(1),
          PlayerId(1),
          Mark(Cords(xPosition = Position(columnNumber), yPosition = Position(1)), MarkType.Cross))

      val thirdMove =
        Move(
          gameId,
          SequenceNumber(2),
          PlayerId(1),
          Mark(Cords(xPosition = Position(columnNumber), yPosition = Position(2)), MarkType.Cross))

      val game = emptyGame.withNewMove(firstMove).withNewMove(secondMove).withNewMove(thirdMove)

      GameFinishResolver.isWinningMove(game, thirdMove.mark) shouldBe true
    }
  }

  it should "return true for winning diagonally /" in {
    val firstMove =
      Move(
        gameId,
        SequenceNumber(0),
        PlayerId(1),
        Mark(Cords(xPosition = Position(0), yPosition = Position(0)), MarkType.Cross))

    val secondMove =
      Move(
        gameId,
        SequenceNumber(1),
        PlayerId(1),
        Mark(Cords(xPosition = Position(1), yPosition = Position(1)), MarkType.Cross))

    val thirdMove =
      Move(
        gameId,
        SequenceNumber(2),
        PlayerId(1),
        Mark(Cords(xPosition = Position(2), yPosition = Position(2)), MarkType.Cross))

    val game = emptyGame.withNewMove(firstMove).withNewMove(secondMove).withNewMove(thirdMove)

    GameFinishResolver.isWinningMove(game, thirdMove.mark) shouldBe true
  }

  it should "return true for winning diagonally \\" in {
    val firstMove =
      Move(
        gameId,
        SequenceNumber(0),
        PlayerId(1),
        Mark(Cords(xPosition = Position(2), yPosition = Position(0)), MarkType.Cross))

    val secondMove =
      Move(
        gameId,
        SequenceNumber(1),
        PlayerId(1),
        Mark(Cords(xPosition = Position(1), yPosition = Position(1)), MarkType.Cross))

    val thirdMove =
      Move(
        gameId,
        SequenceNumber(2),
        PlayerId(1),
        Mark(Cords(xPosition = Position(0), yPosition = Position(2)), MarkType.Cross))

    val game = emptyGame.withNewMove(firstMove).withNewMove(secondMove).withNewMove(thirdMove)

    GameFinishResolver.isWinningMove(game, thirdMove.mark) shouldBe true
  }

  it should "return false if mark types in row are not identical" in {
    val firstMove =
      Move(
        gameId,
        SequenceNumber(0),
        PlayerId(1),
        Mark(Cords(xPosition = Position(0), yPosition = Position(0)), MarkType.Cross))

    val secondMove =
      Move(
        gameId,
        SequenceNumber(1),
        PlayerId(1),
        Mark(Cords(xPosition = Position(1), yPosition = Position(0)), MarkType.Nought))

    val thirdMove =
      Move(
        gameId,
        SequenceNumber(2),
        PlayerId(1),
        Mark(Cords(xPosition = Position(2), yPosition = Position(0)), MarkType.Cross))

    val game = emptyGame.withNewMove(firstMove).withNewMove(secondMove).withNewMove(thirdMove)

    GameFinishResolver.isWinningMove(game, thirdMove.mark) shouldBe false
  }

  "GameFinishResolver.isFull" should "return false for empty game" in {
    GameFinishResolver.isFull(emptyGame) shouldBe false
  }

  it should "return true for full game" in {
    val fullGame = (0 to 2).foldLeft(emptyGame) {
      case (game, row) => (0 to 2).foldLeft(game) {
        case (game, column) => game.withNewMove(Move(
          gameId,
          SequenceNumber(row + column),
          PlayerId(1),
          Mark(Cords(xPosition = Position(column), yPosition = Position(row)), MarkType.Cross)))
      }
    }

    GameFinishResolver.isFull(fullGame) shouldBe true
  }


}
