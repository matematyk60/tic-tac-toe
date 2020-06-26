package com.dgodek.tictactoe.domain

trait IdProvider[F[_], ID] {
  def generateId(): F[ID]
}
