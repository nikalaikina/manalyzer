package org.github.nikalaikina.manalyzer.chart

import com.googlecode.charts4j.{Color, Data, GCharts, Plots}

import scala.util.Random

object Artist extends Charter {

  override def draw(array: Seq[(Seq[Double], String)]): String = {
    val plots = array.map {
      case (line, name) =>
        val plot = Plots.newPlot(Data.newData(line:_*))
        plot.setColor(Random.shuffle(colors).head)
        plot.setLegend(name)
        plot
    }
    val newLineChart = GCharts.newLineChart(plots: _*)
    newLineChart.setSize(1000, 300)
    newLineChart.toURLString
  }

  val colors = List(
    Color.CHOCOLATE,
    Color.CORAL,
    Color.CYAN,
    Color.DARKCYAN,
    Color.RED,
    Color.GOLD,
    Color.GREEN,
    Color.DARKGREEN,
    Color.GRAY
  )
}
