package org.github.nikalaikina.manalyser.dao

import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{MongoCollection, _}

import scala.concurrent.{ExecutionContext, Future}


trait BaseDataAccess[T <: DbModel] {

  import Helpers._

  implicit val executionContext = ExecutionContext.fromExecutorService(
    java.util.concurrent.Executors.newCachedThreadPool()
  )

  val database: MongoDatabase = MongoClient().getDatabase("manalyser")
  val collection: MongoCollection[Document] = database.getCollection(collectionName)
  val mutableCollection: MongoCollection[org.mongodb.scala.bson.collection.mutable.Document] =
    database.getCollection(collectionName)

  def collectionName: String

  def all: Future[Seq[T]] = collection.find().map(fromDoc).toFuture().map(_.flatten)

  def find(id: String): Option[T] = {
    collection.find(equal("_id", new ObjectId(id))).results().headOption.flatMap(fromDoc)
  }

  def insert(e: T): Option[String] = {
    val doc = org.mongodb.scala.bson.collection.mutable.Document(toDoc(e).toSet)
    mutableCollection.insertOne(doc).results()
    doc.get("_id").map(_.asObjectId().getValue.toString)
  }

  def insertAll(list: Iterable[T]): Unit = {
    val docs = list.map(e => org.mongodb.scala.bson.collection.mutable.Document(toDoc(e).toSet)).toSeq
    mutableCollection.insertMany(docs).results()
  }

  def update(e: T): Unit = {
    collection.findOneAndReplace(equal("_id", new ObjectId(e.id.get)), toDoc(e)).results()
  }

  def toDoc(e: T): Document
  def fromDoc(d: Document): Option[T]
}
