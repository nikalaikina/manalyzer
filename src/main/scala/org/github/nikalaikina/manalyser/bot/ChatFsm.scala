package org.github.nikalaikina.manalyser.bot

import akka.actor.{ActorRef, FSM}
import org.github.nikalaikina.manalyser.{MessageProcessor, Metrics, Setup}
import org.github.nikalaikina.manalyser.bot.ChatFsm._
import info.mukel.telegrambot4s.models.{Message => BotMessage}
import org.github.nikalaikina.manalyser.chart.JFreeCharter
import org.github.nikalaikina.manalyser.tg.{ApiFactory, Service}

import scala.language.postfixOps


case class ChatFsm(botApi: ActorRef, chatId: Long) extends FSM[ChatFsm.State, ChatFsm.Data] {

  val tgApi = new Service(ApiFactory.api)

  val phoneNumberRx = "^[0-9\\-\\+]{9,15}$".r
  val codeRx = "^[0-9]{5}$".r

  startWith(GettingNumber, Collecting())

  override def preStart(): Unit = {
    super.preStart()
    txt("Send your phone number")
  }

  when(GettingNumber) {
    case Event(msg: BotMessage, _) =>
      val number = msg.text.filter(txt => phoneNumberRx.findAllIn(txt).nonEmpty)
      number match {
        case Some(phoneNumber) =>
          val sendCode = tgApi.sendCode(phoneNumber)
          val timeout = sendCode.getTimeout
          println(s"!!! TIMEOUT $timeout")
          val hash = sendCode.getPhoneCodeHash
          txt("Send your log in code")
          goto(LogIn) using Number(phoneNumber, hash)
        case None =>
          txt("Invalid phone number")
          stay()
      }

  }

  when(LogIn) {
    case Event(msg: BotMessage, Number(phoneNumber, hash)) =>
      val code = msg.text.filter(txt => codeRx.findAllIn(txt).nonEmpty)
      code match {
        case Some(code) =>
          tgApi.signIn(phoneNumber, code.split(" ").mkString, hash)
          txt("Forward any message from friend you want to analyse")
          goto(ChoosingUser)
        case None =>
          txt("Invalid code")
          stay()
      }
  }

  when(ChoosingUser) {
    case Event(msg: BotMessage, Number(phoneNumber, hash)) =>
      msg.forwardFrom.foreach { user =>
        txt("Analysing ...")
        val messages = tgApi.getHistory(user.id)
        val r2 = MessageProcessor(messages, Metrics.stats).exec
        val data = JFreeCharter.draw(r2, s"${chatId}_result.jpg")
        pic(data)
      }
      stay()
  }

  def txt(text: String) = botApi ! SendTextAnswer(chatId, text)
  def pic(data: Array[Byte]) = botApi ! SendPic(chatId, data)

}

object ChatFsm {
  sealed trait State
  case object GettingNumber extends State
  case object LogIn extends State

  case object ChoosingUser extends State
  case object ProcessingMessages extends State

  sealed trait Data
  case class Collecting() extends Data
  case class Number(number: String, hash: String) extends Data
}
