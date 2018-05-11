package edu.rylynn.scala.datamining.core.associate

class Eclat(minSupport: Double, minConfidence: Double, data: List[String]) {
  val transactions: List[(Set[String], Int)] = data.zipWithIndex.map(t => (t._1.split(",").toSet, t._2))
  val tSize = transactions.size
  var tidSets: Map[Set[String], Set[Int]] = Map[Set[String], Set[Int]]()

  var frequentItemSet: List[(Set[String], Double)] = List[(Set[String], Double)]()

  def string2Set(s: String): Set[String] = Set(s)

  def generateFrequentItemSet: List[(Set[String], Double)] = {
    def addIntoTidSet(itemSet: Set[String], index: Int): Unit = {
      if (tidSets.getOrElse(itemSet, -1) == -1) {
        tidSets += (itemSet -> Set(index))
      }
      else {
        var value = tidSets.apply(itemSet)
        value += index
        tidSets = tidSets.updated(itemSet, value)
      }
    }

    def countSupport(tid: Set[Int]): Double = tid.size.toDouble / tSize

    def generateSuperItemSet(cni: (Set[String], Set[Int]), cnj: (Set[String], Set[Int])): (Set[String], Set[Int]) = {
      val superSet = cni._1.++(cnj._1)
      if (superSet.size == cni._1.size + 1) {
        val superSetTid: Set[Int] = cni._2.intersect(cnj._2)
        (superSet, superSetTid)
      }

      else
        null
    }

    transactions.foreach(t => t._1.foreach(i => addIntoTidSet(string2Set(i), t._2)))

    var lastFrequentSet: List[(Set[String], Double)] = tidSets.toList.map(s => (s._1, countSupport(s._2))).filter(_._2 >= minSupport)

    frequentItemSet ++= lastFrequentSet

    var flag = 1
    while (flag == 1) {
      var newItemSet: Set[(Set[String], Set[Int])] = Set[(Set[String], Set[Int])]()
      val cni: List[(Set[String], Set[Int])] = lastFrequentSet.map(s => (s._1, tidSets.apply(s._1)))
      val cnj: List[(Set[String], Set[Int])] = lastFrequentSet.map(s => (s._1, tidSets.apply(s._1)))
      cni.foreach(cnii => newItemSet = newItemSet ++ cnj.map(cnjj => generateSuperItemSet(cnii, cnjj)).filter(items => items != null))
      tidSets ++= newItemSet
      val newFrequentItemSet: List[(Set[String], Double)] = newItemSet.toList.map(s => (s._1, countSupport(s._2))).filter(_._2 >= minSupport)

      if (newFrequentItemSet.isEmpty) {
        flag = 0
      }
      else {
        frequentItemSet ++= newFrequentItemSet
        lastFrequentSet = newFrequentItemSet
      }
    }
    frequentItemSet
  }

  def generateRules: List[(Set[String], Set[String], Double)] = {
    generateFrequentItemSet

    def subItemSet(itemSet: Set[String]): List[Set[String]] = itemSet.subsets().toList.filter(s => s.nonEmpty && s.size != itemSet.size)

    def strongRules(A: Set[String], B: Set[String], supportAB: Double): (Set[String], Set[String], Double) = {
      if (A.isEmpty || B.isEmpty)
        null

      val supportA = frequentItemSet.filter(s => s._1.equals(A)).head._2
      val confidence: Double = supportAB / supportA
      if (confidence > minConfidence)
        (A, B, confidence)
      else
        null
    }

    frequentItemSet.flatMap(frequentSet => subItemSet(frequentSet._1).map(
      subSet => strongRules(subSet, frequentSet._1.diff(subSet), frequentSet._2)).filter(r => r != null))
  }

}

object test {
  def main(args: Array[String]): Unit = {
    val list = List("a,b,c,d", "c,d,e,f", "c,d,e,f", "c,d,e,f", "c,d,e,f")
    //new Eclat(0.3, 0.8, list).generateFrequentItemSet.foreach(println(_))
    new Eclat(0.3, 0.8, list).generateRules.foreach(r=>println(r._1,"=>",r._2," ",r._3))
  }

}
