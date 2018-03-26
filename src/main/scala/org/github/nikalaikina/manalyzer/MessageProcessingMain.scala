package org.github.nikalaikina.manalyzer

import org.github.nikalaikina.manalyzer.chart.JFreeCharter
import org.github.nikalaikina.manalyzer.dao.MessageDao

import scala.util.Random


object MessageProcessingMain extends App {
  val dao = new MessageDao()

  try {
    val messages = dao.find(35720273, 138017261)
    val r2 = MessageProcessor(messages, Metrics.stats).exec
    JFreeCharter.draw(r2, s"${Random.nextLong()}_result.png")
    println("DONE")
  } catch {
    case x: Throwable =>
      println(x)
      throw x
  }

}
