package com.dgodek.tictactoe.infrastructure.doobie

import com.dgodek.tictactoe.domain.{Game, GameId, GameQuery, GameStatus, Move, PlayerId}
import doobie.syntax.string.toSqlInterpolator
import doobie.util.fragment.Fragment
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import doobie.Fragments

object GameQueries {

  import GameMeta._

  val insertGame: Update[Game] =
    Update[Game]("""
      INSERT INTO games (id, start_time, first_player_id, second_player_id, status, winner_player_id)
      VALUES (?, ?,  ?, ?, ?, ?)""")

  val insertMove: Update[Move] =
    Update[Move]("""
        INSERT INTO moves (game_id, seq, player_id, x_position, y_position, mark_type)
        VALUES (?, ?, ?, ?, ?, ?)""")

  def updateGameStatus(gameId: GameId, gameStatus: GameStatus, winnerPlayerId: Option[PlayerId]): Update0 = {
    val set = Fragments.set(
      winnerPlayerId
        .map(winnerPlayerId => fr"winner_player_id = $winnerPlayerId")
        .toList :+ fr"status = $gameStatus": _*)

    (fr"UPDATE games" ++ set ++ fr"WHERE id = $gameId").update
  }

  def updateSecondPlayerId(gameId: GameId, playerId: PlayerId): doobie.Update0 =
    sql"""
         UPDATE games SET second_player_id = $playerId where id = $gameId""".update

  def findGame(gameQuery: GameQuery): Query0[Game] = {
    val fragment = fr"SELECT * FROM games g"

    addQueryFragments(gameQuery, fragment).query[Game]
  }

  def findGameWithMoves(gameQuery: GameQuery): Query0[(Game, Option[Move])] = {
    val fragment = fr"""SELECT g.id, g.start_time, g.first_player_id, g.second_player_id, g.status, g.winner_player_id,
                    m.game_id, m.seq, m.player_id, m.x_position, m.y_position, m.mark_type
                    FROM games g LEFT JOIN moves m ON g.id = m.game_id
                     """

    (addQueryFragments(gameQuery, fragment) ++ fr"ORDER BY m.seq")
      .query[(Game, Option[Move])]
  }

  private def addQueryFragments(gameQuery: GameQuery, fragment: Fragment) = {
    val byPlayerId =
      gameQuery.playerId.map(playerId => fr"g.first_player_id = $playerId OR g.second_player_id = $playerId")
    val byGameId = gameQuery.gameId.map(gameId => fr"g.id = $gameId")

    fragment ++ Fragments.whereAndOpt(byPlayerId, byGameId)
  }

}
