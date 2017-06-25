package org.github.nikalaikina.manalyzer

import akka.actor._
import org.github.nikalaikina.manalyzer.bot.ManalyserBot
import org.github.nikalaikina.manalyzer.tg.{ApiFactory, Service}
import org.telegram.api.message.TLMessage

object Main extends App {

  implicit val system = ActorSystem("routes-service")
  implicit val executionContext = system.dispatcher

  system.actorOf(Props(classOf[ManalyserBot]), "bot")

}
