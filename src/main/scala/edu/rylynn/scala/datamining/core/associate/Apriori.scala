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

  def generateSuperItemSet(cni: Set[String], cnj: Set[String]): Set[String] = {
    val superSet = cni.intersect(cnj)
    if (superSet.size == cni.size - 1)
      superSet
    else
      null
  }

  def string2Set(s: String): Set[String] = Set(s)

  def generateSubItemSet(itemSet: Set[String]): List[Set[String]] = {
    itemSet.map(item => itemSet.diff(string2Set(item))).toList
  }

  def generateFrequentItemSet: List[(Set[String], Double)] = {
    //val items: List[Set[String]] = transactions.reduceLeft((x1, x2)=>x1.++(x2))

    //val lastFrequentSet = items.map(item=>(item.toSet, countSupport(string2Set(item)))).filter(_._2>=minSupport).toList

    //frequentItemSet = frequentItemSet ++ lastFrequentSet
    print(frequentItemSet)

    null

  }
}

object Test {
  def main(args: Array[String]): Unit = {
    val list = List("a,b,c,d", "c,d,e,f", "c,d,e,f", "c,d,e,f", "c,d,e,f")
    val set = Set("a", "c")
    new Apriori(0.2, 0.8, list).generateFrequentItemSet
  }
}
