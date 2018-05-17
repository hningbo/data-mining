package edu.rylynn.scala.datamining.core.cluster

import scala.math.{pow, sqrt}

class KMeans(clusterNum: Int, data: Array[Array[Double]]) {

  var centers: List[Int] = Nil
  val points: Array[Point] = data.map(p => new Point(p))
  var error: Double = -1

  class Point(_attribute: Array[Double]) {
    var classIndex: Int = -1

    val attribute: Array[Double] = _attribute

    def distance(point: Point): Double = {
      sqrt(this.attribute.zip(point.attribute).map(t => pow(t._1 - t._2, 2)).sum)
    }


    override def toString: String = attribute.toString
  }

  def kmeans: List[(Int, Array[Double])] = {
    def nearestIndex(point: Point) = centers.map(center => (center, points(center).distance(point)))
      .min(Ordering.by[(Int, Double), Double] { case (index, distane) => distane })


    while (centers.length < clusterNum) {
      val randomNum: Int = new (util.Random).nextInt(data.length)
      if (!centers.contains(randomNum)) {
        centers = centers.::(randomNum)
      }
    }

    var centerPoints: List[(Int, Array[Double])] = centers.zipWithIndex.map(centerIndex => (centerIndex._2, points(centerIndex._1).attribute)).toList
    var lasterror: Double = 0
    var delta: Double = 99999
    var k = 3
    while (k!=0) {
      k = k-1
      error = 0
      points.foreach(p => {
        val t = nearestIndex(p)
        p.classIndex = t._1
        error = error + t._2
      })
      val deltaCenters = points.groupBy(p => p.classIndex).
        map(t => (t._1, t._2.
          map(point => point.attribute).
          reduce((a1, a2) => a1.zip(a2).
            map(at => at._1 + at._2)).map(at=>at/t._2.size.toDouble)))

      centerPoints.foreach { centerPoint =>
        deltaCenters.foreach(deltaCenter => if (centerPoint._1 == deltaCenter._1) {
          Array.copy(deltaCenter._2, 0, centerPoint._2, 0, deltaCenter._2.length)
        })
      }
      centerPoints.foreach(p=>println(p._2.foreach(print(_,""))))

      println()
    }
    centerPoints
  }

}

object test {
  def main(args: Array[String]): Unit = {
    var data: Array[Array[Double]] = Array(Array(1, 2, 3, 4, 5), Array(2, 3, 4, 5, 6), Array(5, 6, 7, 8, 9), Array(9, 10, 11, 12, 13), Array(9, 10, 11, 12, 13))

    new KMeans(3, data).kmeans.foreach(println(_))
  }
}
