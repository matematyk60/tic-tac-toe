package com.dgodek.tictactoe.infrastructure.doobie

import doobie.syntax.string.toSqlInterpolator

object Schema {


  val createUserSchema: doobie.Update0 = sql"""
       CREATE TABLE IF NOT EXISTS users (
         id INTEGER PRIMARY KEY,
         name VARCHAR NOT NULL
       )
         """.update

  val createGamesSchema: doobie.Update0 = sql"""
       CREATE TABLE IF NOT EXISTS games (
         id INTEGER PRIMARY KEY,
         start_time TIMESTAMP NOT NULL,
         first_player_id INTEGER NOT NULL,
         second_player_id INTEGER,
         status VARCHAR NOT NULL,
         winner_player_id INTEGER,
         FOREIGN KEY (first_player_id) REFERENCES users (id),
         FOREIGN KEY (second_player_id) REFERENCES users (id),
         FOREIGN KEY (winner_player_id) REFERENCES users (id)
       )
    """.update

  val createMovesSchema: doobie.Update0 =
    sql"""
         CREATE TABLE IF NOT EXISTS moves (
           game_id INTEGER NOT NULL,
           seq INTEGER NOT NULL,
           player_id INTEGER NOT NULL,
           x_position INTEGER NOT NULL,
           y_position INTEGER NOT NULL,
           mark_type VARCHAR NOT NULL,
           PRIMARY KEY (game_id, seq),
           FOREIGN KEY (player_id) references users (id)
         )
         """.update

  val insertDefaultUsers: doobie.Update0 =
    sql"""
         INSERT INTO users (id, name) VALUES (1, 'Dave'), (2, 'Britney'),
         (3, 'Peter'), (4, 'Ashley'), (5, 'Audrey'), (6, 'Patrick'),
         (7, 'Mark'), (8, 'Lea'), (9, 'Paul'), (10, 'Gregory')
         ON CONFLICT DO NOTHING""".update

  val createTables = List(
    createUserSchema,
    createGamesSchema,
    createMovesSchema
  )


}
