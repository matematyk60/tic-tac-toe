package com.dgodek.tictactoe.infrastructure.doobie

import java.time.LocalDateTime

import com.dgodek.tictactoe.domain._
import doobie.implicits.javatime.JavaTimeLocalDateTimeMeta
import doobie.util.meta.Meta

object GameMeta {

  implicit val gameIdMeta: Meta[GameId]         = Meta[Int].timap(GameId)(_.value)
  implicit val startTimeMeta: Meta[StartTime]   = Meta[LocalDateTime].timap(StartTime)(_.value)
  implicit val playerIdMeta: Meta[PlayerId]     = Meta[Int].timap(PlayerId)(_.value)
  implicit val gameStatusMeta: Meta[GameStatus] = GameStatus.enumMeta

}
