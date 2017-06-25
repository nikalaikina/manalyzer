package org.github.nikalaikina.manalyser.dao

import java.time.{Clock, LocalDateTime}
import java.util.concurrent.TimeUnit

import org.bson.types.ObjectId
import org.github.nikalaikina.manalyser.Message
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class MessageDao extends BaseDataAccess[Message] {

  import Helpers._

  override def collectionName: String = "messages"


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
    def headResult() = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))
    def printResults(initial: String = ""): Unit = {
      if (initial.length > 0) print(initial)
      results().foreach(res => println(converter(res)))
    }
    def printHeadResult(initial: String = ""): Unit = println(s"$initial${converter(headResult())}")
  }

}