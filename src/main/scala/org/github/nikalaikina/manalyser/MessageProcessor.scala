package org.github.nikalaikina.manalyser

import java.time.{LocalDate, LocalDateTime}

import scala.util.Try

case class MessageProcessor(
                           allMessages: Iterable[Message],
                           metrics: List[Metric]
                           ) {

  import org.github.nikalaikina.manalyser.util.GraphicUtil._

   val from: LocalDateTime = allMessages.map(_.time).minBy(x => x.getYear * 10000 + x.getMonthValue * 100 + x.getDayOfMonth)
   val now = LocalDate.now()

   def exec = processDays().map(listToChart).map(l => smooth(l.toList, 100)).zip(metrics.map(_.name))

   private def processDays(
                            date: LocalDate = from.toLocalDate,
                            ans: List[List[Double]] = metrics.map(_ => List[Double]())
                          ): List[List[Double]] = {
     if (date == now) return ans

     val todayMessages = allMessages.filter(_.time.toLocalDate == date)
     val res = metrics.zip(ans).map {
       case (Metric(f, _), list) =>
         list :+ Try(f(todayMessages)).toOption.getOrElse(0d)
     }
     processDays(date.plusDays(1), res)
   }

}
