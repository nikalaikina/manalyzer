package org.github.nikalaikina.manalyser

import scalaz.syntax.std.option._

object Metrics {

  val stats: List[Metric] = List(
    Metric(_.size, "count"),
    Metric(_.textLength, "text sum"),
    Metric(msgs => {
      if (msgs.isEmpty) 0 else msgs.textLength / msgs.size
    }, "average"),
    Metric(
      _.toMsgSizes.some
        .filter(_.nonEmpty)
        .map(_.max.toDouble)
        .getOrElse(0d),
      "max msg size"
    ),
    Metric(msgs => {
      val mine = msgs.mine.textLength
      val notMine = msgs.notMine.textLength
      if (mine == 0) 1 else notMine / mine
    }, "not mine to mine")
  )

  implicit class MessagesExt(msgs: Iterable[Message]) {
    def textLength = toMsgSizes.sum
    def toMsgSizes = msgs.flatMap(_.text).map(_.length)
    def toOption = msgs.some.filter(_.nonEmpty)
    def mine = msgs.filter(_.fromMe)
    def notMine = msgs.filterNot(_.fromMe)
  }
}


case class Metric(
                   calc: Iterable[Message] => Double,
                   name: String
                 )
