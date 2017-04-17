package org.github.nikalaikina.manalyser

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util.TimeZone

import org.apache.commons.math3.stat.regression.SimpleRegression
import org.github.nikalaikina.manalyser.dao.MessageDao
import org.github.nikalaikina.manalyser.util.Artist

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext
import scalaz.syntax.std.option._

object Charter extends App {

  implicit val executionContext = ExecutionContext.fromExecutorService(
    java.util.concurrent.Executors.newCachedThreadPool()
  )

  val stats: List[((Iterable[Message] => Double), String)] = List(
    (msgs => msgs.size, "count"),
    (msgs => msgs.flatMap(_.text).map(_.size).sum, "text sum"),
    (msgs => {
      val sum = msgs.flatMap(_.text).map(_.size).sum
      if (msgs.isEmpty) 0 else sum / msgs.size
    }, "average"),
    (msgs => msgs.flatMap(_.text).map(_.size).some.filter(_.nonEmpty).map(_.max.toDouble).getOrElse(0d), "max msg size"),
    (msgs => {
      val mine = msgs.filter(_.fromMe).flatMap(_.text).map(_.size).sum
      val notMine = msgs.filterNot(_.fromMe).flatMap(_.text).map(_.size).sum
      if (mine == 0) 1 else notMine / mine
    }, "not mine to mine")
  )

  val dao = new MessageDao()
  dao.all.map { msgs =>
    val from: LocalDateTime = msgs.map(_.time).minBy(x => x.getYear * 10000 + x.getMonthValue * 100 + x.getDayOfMonth)

    val now = LocalDate.now()
    def processDays(date: LocalDate = from.toLocalDate, ans: List[List[Double]] = stats.map(_ => List[Double]())): List[List[Double]] = {
      if (date == now) return ans

      val ms = msgs.filter(_.time.toLocalDate == date)
      val res = stats.zip(ans).map {
        case (counter, list) =>
          try {
            list :+ counter._1(ms)
          } catch {
            case x: Throwable =>
              println(":" * 100)
              println(x)
              println(":" * 100)
              list :+ 0d
          }
      }
      processDays(date.plusDays(1), res)
    }

    val result = processDays()

    val r2: Seq[(List[Double], String)] = result.map { line =>
      val reduced = reduceList(line)
      val max = reduced.max
      reduced.map(_ / max * 100)
    }.zip(stats.map(_._2))

    val url = Artist.draw(r2)

    println("!" * 100)
    println(url)
    println("!" * 100)
  }

  Thread.sleep(1000 * 60 * 60 * 24)

  def reduceList(list: List[Double], res: List[Double] = List.empty[Double]): List[Double] = {
    val n = 3
    if (list.isEmpty) {
      res
    } else {
      val take = list.take(n)
      reduceList(list.drop(n), res :+ take.sum / take.size)
    }
  }

}

trait Metric {
  val name: String
  def calc(msgs: Iterable[Message]): Double
}

