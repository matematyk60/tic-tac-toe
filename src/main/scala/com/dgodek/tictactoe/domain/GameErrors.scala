package com.dgodek.tictactoe.domain

import enumeratum.{CirceEnum, Enum, EnumEntry}

object GameErrors {

  sealed trait JoinGameError extends EnumEntry

  object JoinGameError extends Enum[JoinGameError] with CirceEnum[JoinGameError] {

    override def values: IndexedSeq[JoinGameError] = findValues

    case object NoSuchGame extends JoinGameError
    case object GameIsFull extends JoinGameError
  }

  sealed trait MakeMoveError extends EnumEntry

  object MakeMoveError extends Enum[MakeMoveError] with CirceEnum[MakeMoveError] {

    override def values: IndexedSeq[MakeMoveError] = findValues

    case object UserNotGamePlayer  extends MakeMoveError
    case object NotPlayersTurn     extends MakeMoveError
    case object NoSuchGame         extends MakeMoveError
    case object GameFinished       extends MakeMoveError
    case object InvalidPosition    extends MakeMoveError
    case object PositionTaken      extends MakeMoveError
  }

}
