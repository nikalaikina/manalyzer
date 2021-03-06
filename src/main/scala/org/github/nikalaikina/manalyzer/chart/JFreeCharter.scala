package org.github.nikalaikina.manalyzer.chart

import java.io.{File, FileReader}
import java.nio.file.{Files, Paths}

import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFactory, ChartUtilities, JFreeChart}
import org.jfree.data.category.DefaultCategoryDataset


object JFreeCharter {

  def draw(array: Seq[(Seq[Double], String)], fileName: String): Array[Byte] = {

    val dataset = new DefaultCategoryDataset( )
    for {
      (line, name) <- array
      (value, i) <- line.zipWithIndex
    } {
      dataset.addValue(value, name, i)
    }

    val lineChart: JFreeChart = ChartFactory.createLineChart(
      "Private chat chart",
      "Days",
      "Interest rate",
      dataset,
      PlotOrientation.VERTICAL,
      true, true, false
    )

    val width = 1300   /* Width of the image */
    val height = 400
    ChartUtilities.saveChartAsPNG(new File(fileName), lineChart, width, height)
    val path = Paths.get(fileName)
    Files.readAllBytes(path)
  }
}
