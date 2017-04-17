package org.github.nikalaikina.manalyser

import org.apache.commons.math3.stat.regression.SimpleRegression

object Lalala extends App {

  val sr = new SimpleRegression()
  private val ints = Array(1, 3, 8, 45, 2, 1)

  var k = 1

  ints.foreach { i =>
    sr.addData(k, i)
    k += 1
  }
  val res = sr.regress()
  res.getParameterEstimates.foreach { x => print(x); print(", ") }
  println(res)

}
