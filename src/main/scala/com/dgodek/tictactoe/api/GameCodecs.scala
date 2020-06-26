package com.dgodek.tictactoe.api

import com.dgodek.tictactoe.api.GameRequests.MakeMoveRequest
import com.dgodek.tictactoe.domain._
import io.circe.generic.semiauto
import io.circe.{Codec, Decoder, Encoder, Json}

object GameCodecs {
  implicit val gameIdEncoder: Encoder[GameId]         = Encoder.encodeInt.contramap(_.value)
  implicit val startTimeEncoder: Encoder[StartTime]   = Encoder.encodeLocalDateTime.contramap(_.value)
  implicit val playerIdEncoder: Encoder[PlayerId]     = Encoder.encodeInt.contramap(_.value)
  implicit val gameStatusEncoder: Encoder[GameStatus] = GameStatus.circeEncoder
  implicit val gameEncoder: Encoder[Game]             = semiauto.deriveEncoder

  implicit val sequenceNumberEncoder: Encoder[SequenceNumber] = Encoder.encodeInt.contramap(_.value)
  implicit val markTypeEncoder: Encoder[MarkType]             = MarkType.circeEncoder

  implicit val positionCodec: Codec[Position] =
    Codec.from(Decoder.decodeInt.map(Position.apply), Encoder.encodeInt.contramap(_.value))

  implicit val markEncoder: Encoder[Mark] = Encoder.instance(
    mark =>
      Json.obj(
        "x"        -> positionCodec(mark.cords.xPosition),
        "y"        -> positionCodec(mark.cords.yPosition),
        "markType" -> markTypeEncoder(mark.markType)
    ))
  implicit val moveEncoder: Encoder[Move] = semiauto.deriveEncoder

  implicit val gameWithMovesEncoder: Encoder[GameWithMoves] = Encoder.instance { gameWithMoves =>
    val gameJson  = gameEncoder(gameWithMoves.game)
    val movesJson = Json.obj("moves" -> Json.arr(gameWithMoves.moves.map(moveEncoder.apply): _*))

    gameJson.deepMerge(movesJson)
  }

  implicit val makeMoveRequestDecoder: Decoder[MakeMoveRequest] = semiauto.deriveDecoder

}
