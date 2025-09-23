package bcGen.Methods

object MethodTestCases {
  val rng = new scala.util.Random(1234)

  //def genericIdentity[T](value: T): T = MethodTestLib.identity[T](value)
  @main def runIdentityInt(): Unit = {
    val repeat = 10000
    val input = 42
    var sum = 0
    var i = 0
    while (i < repeat) {
      sum += MethodTestLib.identity[Int](input)
      i += 1
    }
    println(s"Sum of $repeat identities of $input is $sum")
  }

  @main def runFirstMixed(): Unit = {
    val repeat = 10000
    var i = 0
    var sumInt = 0
    var sumDouble = 0.0
    while (i < repeat) {
      val n = rng.nextInt(2)
      if (n == 0) {
        sumInt += MethodTestLib.first[Int, Double](i, i.toDouble)
      } else {
        sumDouble += MethodTestLib.first[Double, Int](i.toDouble, i)
      }
      i += 1
    }
    println(s"Sum of $repeat firsts is int: $sumInt, double: $sumDouble")
  }
}
