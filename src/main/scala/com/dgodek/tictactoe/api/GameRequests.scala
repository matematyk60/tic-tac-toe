package com.dgodek.tictactoe.api

object GameRequests {

  case class MakeMoveRequest(
      x: Int,
      y: Int
  )

}
