package org.github.nikalaikina.manalyzer.dao

import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{MongoCollection, _}

import scala.concurrent.{ExecutionContext, Future}
import com.mongodb.MongoCredential._
import org.mongodb.scala.connection.ClusterSettings
import scala.collection.JavaConverters._


trait BaseDataAccess[T <: DbModel] {

  import Helpers._

  implicit val executionContext = ExecutionContext.fromExecutorService(
    java.util.concurrent.Executors.newCachedThreadPool()
  )

  val database: MongoDatabase = {
    val user = "admin"
    val database = "admin"
    val password: Array[Char] = "abc123".toCharArray
    val credential: MongoCredential = createScramSha1Credential(user, database, password)
    val clusterSettings = ClusterSettings.builder().hosts(List(new ServerAddress("localhost")).asJava).description("Local Server").build()
    val settings = MongoClientSettings.builder().credential(credential).clusterSettings(clusterSettings).build()
    MongoClient(settings).getDatabase("manalyser")
  }
  val collection: MongoCollection[Document] = database.getCollection(collectionName)
  val mutableCollection: MongoCollection[org.mongodb.scala.bson.collection.mutable.Document] =
    database.getCollection(collectionName)

  def collectionName: String

  def all: Future[Seq[T]] = collection.find().map(fromDoc).toFuture().map(_.flatten)

  def count: Future[Long] = collection.count().toFuture()

  def find(id: String): Option[T] = {
    collection.find(equal("_id", new ObjectId(id))).results().headOption.flatMap(fromDoc)
  }

  def insert(e: T): Option[String] = {
    val doc = org.mongodb.scala.bson.collection.mutable.Document(toDoc(e).toSet)
    mutableCollection.insertOne(doc).results()
    doc.get("_id").map(_.asObjectId().getValue.toString)
  }

  def insertAll(list: Iterable[T]): Unit = {
    if (list.nonEmpty) {
      val docs = list.map(e => org.mongodb.scala.bson.collection.mutable.Document(toDoc(e).toSet)).toSeq
      val res = mutableCollection.insertMany(docs).results()
      println(res)
    }
  }

  def update(e: T): Unit = {
    collection.findOneAndReplace(equal("_id", new ObjectId(e.id.get)), toDoc(e)).results()
  }

  def toDoc(e: T): Document
  def fromDoc(d: Document): Option[T]
}
