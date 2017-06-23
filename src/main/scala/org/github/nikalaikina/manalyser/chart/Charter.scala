package org.github.nikalaikina.manalyser.chart

trait Charter {

  def draw(array: Seq[(Seq[Double], String)]): String

}
