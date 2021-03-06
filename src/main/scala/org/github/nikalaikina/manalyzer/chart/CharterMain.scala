package org.github.nikalaikina.manalyzer.chart

import java.time.{LocalDate, LocalDateTime}

import org.github.nikalaikina.manalyzer.dao.MessageDao
import org.github.nikalaikina.manalyzer.{Message, MessageProcessor, Metric, Metrics}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object CharterMain extends App {

  implicit val executionContext = ExecutionContext.fromExecutorService(
    java.util.concurrent.Executors.newCachedThreadPool()
  )

  val stats: List[Metric] = Metrics.stats

  val dao = new MessageDao()
  dao.all.map { msgs =>
    val r2 = MessageProcessor(msgs, stats).exec
    val url = JFreeCharter.draw(r2, "chart.png")

    println("!" * 100)
    println(url)
    println("!" * 100)
  }

  Thread.sleep(1000 * 60 * 60 * 24)

}



