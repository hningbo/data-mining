package edu.rylynn.scala.datamining.core

import scala.math.abs

object Test {
  def main(args: Array[String]): Unit = {
    val list = List(36.89, -80, 45, -70,51.13)
    var w1 = 0.0
    var w2 = -180.0

    def train(): Unit = list.foreach {
      x =>
        if (abs(w1 - x) < abs(w2 - x)) {
          val delta = 0.8 * (x - w1)
          w1 = w1 + delta
          println(1, "\t", delta, "\t", w1, "\t", w2)
        }
        else {
          val delta = 0.8 * (x - w2)
          w2 = w2 + delta
          println(2, "\t", delta, "\t", w1, "\t", w2)
        }
    }

    train
    train
  }

}
