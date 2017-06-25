package org.github.nikalaikina.manalyzer.util

import scala.annotation.tailrec
import scala.util.Random

object GraphicUtil extends App {

  val seq = (1 to 10).map(_ => Random.nextInt(100)).map(_.toDouble)

  pp(seq)
  pp(smooth(seq.toList))



  def smooth(a: List[Double], count: Int = 4): List[Double] = {
    println(count)
    pp(a)
    if (count == 0) {
      a
    } else {
      val res = for {
        i <- 1 until a.size - 1
      } yield 0.5 * (0.5 * (a(i-1) + a(i+1)) + a(i))
      smooth((a.head :: res.toList) :+ a.last, count - 1)
    }
  }

  def pp(iterable: Iterable[Double]): Unit = {
    println(iterable.map(math.round).mkString(" "))
  }

  def listToChart(line: List[Double]) = {
    val n = 3
    val reduced = line.sliding(n).map(l => l.sum / l.size).toSeq
    val max = reduced.max
    reduced.map(_ / max * 100)
  }

}
