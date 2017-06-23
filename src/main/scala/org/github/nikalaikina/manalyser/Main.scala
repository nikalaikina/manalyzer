package org.github.nikalaikina.manalyser

import akka.actor._
import org.github.nikalaikina.manalyser.bot.ManalyserBot
import org.github.nikalaikina.manalyser.tg.{ApiFactory, Service}
import org.telegram.api.message.TLMessage

object Main extends App {

  implicit val system = ActorSystem("routes-service")
  implicit val executionContext = system.dispatcher

  system.actorOf(Props(classOf[ManalyserBot]), "bot")

}
