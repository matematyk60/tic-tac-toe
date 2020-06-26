package com.dgodek.tictactoe.api

import java.time.LocalDateTime

import com.dgodek.tictactoe.api.GameRequests.MakeMoveRequest
import com.dgodek.tictactoe.domain.{Cords, Game, GameId, GameStatus, GameWithMoves, Mark, MarkType, Move, PlayerId, Position, SequenceNumber, StartTime}
import io.circe._
import io.circe.parser.decode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameCodecsTest extends AnyFlatSpec with Matchers {

  import GameCodecs._

  "circe" should "deserialize MakeMoveRequest json" in {
    val request = json {
      """
        |{
        |  "x": 1,
        |  "y": 1
        |}
        |""".stripMargin
    }

    makeMoveRequestDecoder.decodeJson(request) shouldBe Right(MakeMoveRequest(1, 1))
  }

  "circe" should "serialize GameWithMoves to proper json" in {
    val game = GameWithMoves(
      Game(
        GameId(1),
        StartTime(LocalDateTime.parse("2020-06-25T20:00:00.000")),
        PlayerId(1),
        Some(PlayerId(2)),
        GameStatus.InProgress,
        winnerPlayerId = None),
      moves = List(
        Move(
          GameId(1),
          SequenceNumber(0),
          PlayerId(1),
          Mark(
            Cords(Position(0), Position(0)),
            MarkType.Cross
          )),
        Move(
          GameId(1),
          SequenceNumber(1),
          PlayerId(2),
          Mark(
            Cords(Position(1), Position(1)),
            MarkType.Nought
          ))
      )
    )

    val gameJson = json {
      """
        |{
        |  "id": 1,
        |  "startTime": "2020-06-25T20:00:00",
        |  "firstPlayerId": 1,
        |  "secondPlayerId": 2,
        |  "status": "InProgress",
        |  "moves": [
        |    {
        |      "gameId": 1,
        |      "sequenceNumber": 0,
        |      "playerId": 1,
        |      "mark": {
        |        "x": 0,
        |        "y": 0,
        |        "markType": "Cross"
        |      }
        |    },
        |    {
        |      "gameId": 1,
        |      "sequenceNumber": 1,
        |      "playerId": 2,
        |      "mark": {
        |        "x": 1,
        |        "y": 1,
        |        "markType": "Nought"
        |      }
        |    }
        |  ]
        |}""".stripMargin
    }

    gameWithMovesEncoder(game).dropNullValues shouldBe gameJson
  }

  private def json(s: String): Json = decode[Json](s).getOrElse(throw new Exception("Not a valid Json."))
}
