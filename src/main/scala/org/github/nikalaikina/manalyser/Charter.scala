package org.github.nikalaikina.manalyser

import java.time.{LocalDate, LocalDateTime}

import org.github.nikalaikina.manalyser.dao.MessageDao
import org.github.nikalaikina.manalyser.util.Artist

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object Charter extends App {

  implicit val executionContext = ExecutionContext.fromExecutorService(
    java.util.concurrent.Executors.newCachedThreadPool()
  )

  val stats: List[Metric] = Metrics.stats

  val dao = new MessageDao()
  dao.all.map { msgs =>
    val r2 = MessageProcessor(msgs, stats).exec
    val url = Artist.draw(r2)

    println("!" * 100)
    println(url)
    println("!" * 100)
  }

  Thread.sleep(1000 * 60 * 60 * 24)

}


case class MessageProcessor(
                           allMessages: Iterable[Message],
                           metrics: List[Metric]
                           ) {

  val from: LocalDateTime = allMessages.map(_.time).minBy(x => x.getYear * 10000 + x.getMonthValue * 100 + x.getDayOfMonth)
  val now = LocalDate.now()

  def exec = processDays().map(listToChart).zip(metrics.map(_.name))

  private def processDays(
                           date: LocalDate = from.toLocalDate,
                           ans: List[List[Double]] = metrics.map(_ => List[Double]())
                         ): List[List[Double]] = {
    if (date == now) return ans

    val todayMessages = allMessages.filter(_.time.toLocalDate == date)
    val res = metrics.zip(ans).map {
      case (Metric(f, _), list) =>
        val value = Try(f(todayMessages)) match {
          case Success(x) =>
            x
          case Failure(ex) =>
            println(ex)
            0d
        }
        list :+ value
    }
    processDays(date.plusDays(1), res)
  }

  private def listToChart(line: List[Double]) = {
    val n = 3
    val reduced = line.sliding(n).map(l => l.sum / l.size).toSeq
    val max = reduced.max
    reduced.map(_ / max * 100)
  }
}
