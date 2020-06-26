package com.dgodek.tictactoe.domain

import java.time.LocalDateTime

import simulacrum.typeclass

@typeclass
trait TimeProvider[F[_]] {
  def currentTime(): F[LocalDateTime]
}
