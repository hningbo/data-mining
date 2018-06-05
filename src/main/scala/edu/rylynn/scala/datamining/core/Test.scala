package edu.rylynn.scala.datamining.core

object Test {
  def main(args: Array[String]): Unit = {
    val orders = List(List(1, 2, 3), List(1, 3, 2), List(2, 3, 1), List(2, 1, 3), List(3, 2, 1), List(3, 1, 2))
    val w = Array(Array(0, 0.3, 0.3), Array(0.3, 0, 0.3), Array(0.3, 0.3, 0))

    val x = Array(1, 1, 0)

    def train(): Unit =
      orders.foreach {
        order =>
          var y = x.map(xi=>xi).toArray
          order.foreach {
            i =>
              y(i - 1) = if (w(i - 1)(0) * y(0) + w(i - 1)(1) * y(2) + w(i - 1)(2) * y(2) - 0.5> 0) 1
              else 0

          }
          y.foreach(print(_))
          println()
      }


    train()
  }

}
