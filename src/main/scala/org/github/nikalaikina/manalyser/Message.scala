package org.github.nikalaikina.manalyser

import java.time.{Instant, LocalDateTime}
import java.util.TimeZone

import org.github.nikalaikina.manalyser.dao.DbModel
import org.telegram.api.message.{TLAbsMessage, TLMessage, TLMessageEmpty, TLMessageService}
import org.telegram.api.messages.{TLAbsMessages, TLMessagesSlice}
import org.telegram.tl.TLVector

import scala.collection.immutable.Seq
import scalaz.syntax.std.option._

case class Message(
                    fromProvider: Boolean,
                    userIdWith: Long,
                    userIdProvider: Long,
                    text: Option[String],
                    time: LocalDateTime,
                    id: Option[String] = None
                  ) extends DbModel

object Message {
  def fromTl(source: TLAbsMessages, myId: Int): Seq[Message] = {
    source match {
      case slice: TLMessagesSlice =>
        convert(slice.getMessages, myId)
    }
  }

  def convert(msgs: TLVector[TLAbsMessage], myId: Int) = {
    msgs.toArray.toVector.flatMap {
      case msg: TLMessage =>
        val from = msg.getFromId
        val to = msg.getToId.getId
        Message(
          fromProvider = from == myId,
          userIdWith = if (from == myId) to else from,
          userIdProvider = myId,
          text = msg.getMessage.some.filter(_.nonEmpty),
          time = LocalDateTime.ofInstant(Instant.ofEpochSecond(msg.getDate()), TimeZone.getDefault.toZoneId)
        ).some
      case msg: TLMessageEmpty =>
        None
      case msg: TLMessageService =>
        None
    }
  }
}
