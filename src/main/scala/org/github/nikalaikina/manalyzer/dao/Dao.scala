package org.github.nikalaikina.manalyzer.dao

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import info.mukel.telegrambot4s.models.{Message => BotMessage}
import org.github.nikalaikina.manalyzer.Message
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MessageDao extends BaseDataAccess[Message] {

  import Helpers._

  override def collectionName: String = "messages"

  def getHistory(msg: BotMessage): Iterable[Message] = {
    val userId = msg.forwardFrom.get.id
    val me = msg.from.get.id
    find(me, userId)
  }

  def find(userId: Long, withId: Long): Iterable[Message] = {
    collection
      .find(
        and(equal("userIdWith", withId), equal("userIdProvider", userId))
      ).results().flatMap(fromDoc)
  }

  def count(userId: Long, withId: Long): Long = {
    collection.count(
      and(equal("userIdWith", withId), equal("userIdProvider", userId))
    ).results().head
  }

  override def toDoc(e: Message): Document = {
    import e._
    val b = Document.builder
    b ++= Document(
      "fromProvider" -> fromProvider,
      "userIdWith" -> userIdWith,
      "userIdProvider" -> userIdProvider,
      "time" -> time.toString
    )

    text.foreach(x => b ++= Document("text" -> x))
    b.result()
  }

  override def fromDoc(d: Document): Option[Message] = {
    for {
      id <- d.get("_id").map(_.asObjectId().getValue.toString)
      fromProvider <- d.get("fromProvider").map(_.asBoolean().getValue)
      userIdWith <- d.get("userIdWith").map(_.asInt64().longValue())
      userIdProvider <- d.get("userIdProvider").map(_.asInt64().longValue())
      text = d.get("text").map(_.asString().getValue)
      time <- d.get("time").map(_.asString().getValue).map(LocalDateTime.parse)
    } yield Message(fromProvider, userIdWith, userIdProvider, text, time)
  }
}

trait DbModel { val id: Option[String] }

object Helpers {

  implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {
    override val converter: (Document) => String = (doc) => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
    override val converter: (C) => String = (doc) => doc.toString
  }

  trait ImplicitObservable[C] {
    val observable: Observable[C]
    val converter: (C) => String

    def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))
  }

}