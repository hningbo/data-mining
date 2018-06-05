package edu.rylynn.scala.datamining.core.cluster

import scala.math.{pow, sqrt}

class KMeans(clusterNum: Int, data: Array[Array[Double]]) {

  var centers: List[(Int, Array[Double])] = List((0, Array(2, 10)), (1, Array(5, 8)), (2, Array(1, 2)))
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
    def nearestIndex(point: Point) = centers.map(center => {
      //println(new Point(center._2).distance(point))
      (center._1, new Point(center._2).distance(point))
    }).minBy(x => x._2)._1


    //    while (centers.length < clusterNum) {
    //      val randomNum: Int = new (util.Random).nextInt(data.length)
    //      if (!centers.contains(randomNum)) {
    //        centers = centers.::(randomNum)
    //      }
    //    }

    var lasterror: Double = 0
    var delta: Double = 99999
    var maxIteration = 4
    while (maxIteration != 0) {
      maxIteration = maxIteration - 1

      val group = points.groupBy(nearestIndex)

      val deltaCenters: List[(Int, Array[Double])] = group.map(g => (g._1, g._2.map(_.attribute).reduce((a1, a2)=>a1.zip(a2).map(z=>z._1+z._2)).map(y=>y/g._2.size.toDouble))).toList



      deltaCenters.foreach(p=>{
        print(p._1+":")
        p._2.foreach(print(_,""))
        println()
      })
      centers = deltaCenters.map(x=>(x._1, x._2))
      //centers.foreach(p => println(p._2.foreach(print(_,""))))

      println()
    }
    centers
  }

}

object test {
  def main(args: Array[String]): Unit = {
    var data: Array[Array[Double]] = Array(Array(2, 10), Array(2, 5), Array(8, 4), Array(5, 8), Array(7, 5), Array(6, 4), Array(1, 2), Array(4, 9))

    new KMeans(3, data).kmeans.foreach(println(_))
  }
}
