package org.github.nikalaikina.manalyser.bot

import akka.actor.{Actor, ActorRef, Props}
import info.mukel.telegrambot4s.api.{ChatActions, Commands, Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{SendMessage, SendPhoto}
import info.mukel.telegrambot4s.models.InputFile.Contents
import info.mukel.telegrambot4s.models.{InputFile, KeyboardButton, Message, ReplyKeyboardMarkup}

import scala.collection.mutable


class ManalyserBot extends Actor with AbstractBot {

  val chats: mutable.Map[Long, ActorRef] = mutable.Map[Long, ActorRef]()

  override def preStart(): Unit = run()

  override def onMessage(msg: Message): Unit = {
    implicit val m = msg
    getChat() ! msg
  }

  on("/start") { implicit msg => _ =>
    chats += msg.source -> newChat()
  }

  override def receive: Receive = {
    case SendTextAnswer(id, text) =>
      request(SendMessage(Left(id), text))

    case SendPic(id: Long, data: Array[Byte]) =>
      request(SendPhoto(Left(id), Left(InputFile("result.jpg", data))))
  }

  def getChat()(implicit msg: Message): ActorRef = {
    chats.getOrElseUpdate(msg.source, newChat())
  }

  def newChat()(implicit msg: Message): ActorRef = {
    context.actorOf(Props(classOf[ChatFsm], self, msg.source))
  }

/*  def getMarkup(buttons: Seq[KeyboardButton], rowLength: Int): Seq[Seq[KeyboardButton]] = {
    def row(i: Int) = buttons.slice(i * rowLength, i * rowLength + rowLength)
    (0 to buttons.size / rowLength + 1)
      .foldLeft(Seq[Seq[KeyboardButton]]())((seq, i) =>  seq :+ row(i))
  }*/
}

import scala.io.Source

case class SendTextAnswer(id: Long, text: String)
case class SendPic(id: Long, data: Array[Byte])

trait AbstractBot extends TelegramBot with Polling with Commands with ChatActions {
  def token = Source.fromFile("bot.token").getLines().next
}