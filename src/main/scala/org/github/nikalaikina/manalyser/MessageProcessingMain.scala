package org.github.nikalaikina.manalyser

import org.github.nikalaikina.manalyser.chart.JFreeCharter
import org.github.nikalaikina.manalyser.tg.{ApiFactory, Service}
import org.telegram.api.contact.TLContact
import org.telegram.api.contacts.TLAbsContacts
import org.telegram.api.message.TLMessage

import scala.util.Random


object MessageProcessingMain extends App {

  try {
    val service = new Service(ApiFactory.api)

    val messages = service.getHistory(35720273, 138017261)
    val r2 = MessageProcessor(messages, Metrics.stats).exec
    JFreeCharter.draw(r2, s"${Random.nextLong()}_result.png")
    println("DONE")
  } catch {
    case x: Throwable =>
      println(x)
      throw x
  }

}
