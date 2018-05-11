package edu.rylynn.scala.datamining.core.associate

class Apriori(minSupport: Double, minConfidence: Double, data: List[String]) {

  val transactions: List[Set[String]] = data.map(transaction => transaction.split(",").toSet)


  var rules: List[(Set[String], Set[String], Double)] = List[(Set[String], Set[String], Double)]()
  var frequentItemSet: List[(Set[String], Double)] = List[(Set[String], Double)]()

  def countSupport(itemSet: Set[String]): Double = {
    def inTransaction(transaction: Set[String]): Boolean = {
      itemSet.map(item => transaction.contains(item)).reduceRight((x1, x2) => x1 & x2)
    }

    val count = transactions.count(transaction => inTransaction(transaction))
    count.toDouble / transactions.size.toDouble
  }


  def string2Set(s: String): Set[String] = Set(s)

  def generateSubItemSet(itemSet: Set[String]): List[Set[String]] = {
    itemSet.map(item => itemSet.diff(string2Set(item))).toList
  }

  def generateFrequentItemSet: List[(Set[String], Double)] = {
    def generateSuperItemSet(cni: Set[String], cnj: Set[String]): Set[String] = {

      val superSet = cni.++(cnj)
      if (superSet.size == cni.size + 1)
        superSet
      else
        null
    }

    val items: Set[String] = transactions.reduce((x1, x2) => x1 ++ x2)

    var lastFrequentSet: List[(Set[String], Double)] = items.map(item => (string2Set(item), countSupport(string2Set(item)))).filter(_._2 >= minSupport).toList
    frequentItemSet = frequentItemSet ++ lastFrequentSet
    var flag: Int = 1
    while (flag == 1) {
      var newItemSet: Set[Set[String]] = Set[Set[String]]()

      val cni: List[Set[String]] = lastFrequentSet.map(_._1)
      val cnj: List[Set[String]] = lastFrequentSet.map(_._1)

      cni.foreach(cnii => newItemSet = newItemSet ++ cnj.map(cnjj => generateSuperItemSet(cnii, cnjj)).filter(items => items != null))


      val newFrequentSet: List[(Set[String], Double)] = newItemSet.toList.map(newItem => (newItem, countSupport(newItem))).filter(_._2 >= minSupport)
      if (newFrequentSet.isEmpty) {
        flag = 0
      }
      else {
        frequentItemSet ++= newFrequentSet
      }
      lastFrequentSet = newFrequentSet
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

object Test {
  def main(args: Array[String]): Unit = {
    val list = List("a,b,c,d", "c,d,e,f", "c,d,e,f", "c,d,e,f", "c,d,e,f")
    //new Apriori(0.3, 0.8, list).generateFrequentItemSet.foreach(s=>println(s._1, s._2))
    new Apriori(0.3, 0.8, list).generateRules.foreach(r=>println(r._1,"=>",r._2," ",r._3))
  }
}
