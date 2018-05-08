package edu.rylynn.scala.datamining.core.associate

class Eclat(minSupport: Int, minConfidence: Int, data: List[String]) {
  val transactions = data.map(transaction=>transaction.split(","))


}
