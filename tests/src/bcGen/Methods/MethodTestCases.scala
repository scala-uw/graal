package bcGen.Methods

object MethodTestCases {
  val rng = new scala.util.Random(1234)

  def genericIdentity[T](value: T): T = MethodTestLib.identity[T](value)
  
  @main def runCorrectness1(): Unit = {
    val v: Int = MethodTestLib.identity[Int](42)
    println(v)
  }

  @main def runCorrectness2(): Unit = 
    val v: Any = MethodTestLib.identity[Int](42)
    println(v)

  def idAny(x: Any): Any = x
  def foo3[T](x: T): Any = idAny(x)

  @main def runCorrectness3(): Unit = 
    val v: Any = foo3[Int](42)
    println(v)

  def foo4[T](x: T): Any = MethodTestLib.identity[Any](x)

  @main def runCorrectness4(): Unit = 
    val v: Any = foo4[Int](42)
    println(v)

  @main def runCorrectness5(): Unit = {
      // Call the method fully with all arguments
      MethodTestLib.A[Int](10)
        [String, Double]("hello", 3.14, 2.0)
          [Boolean, Char, Long](true, 'c', 100L)
            (1.toByte, 42, 2.71, "ref")
    }

  @main def runCorrectness6(): Unit = {
      MethodTestLib.B[Double](3.14, "test", 2.71)
    }

  @main def runIdentitySimple(): Unit = {
    val repeat = 1000000
    val input = 42
    var sum = 0
    var i = 0
    while (i < repeat) {
      sum += MethodTestLib.identity[Int](input)
      i += 1
    }
    println(s"Sum of $repeat identities of $input is $sum")
  }
  
  @main def runIdentityInt(): Unit = {
    val repeat = 1000000
    val input = 42
    var sum = 0
    var anySum: Any = 0
    var i = 0
    while (i < repeat) {
      sum += MethodTestLib.identity[Int](input)
      anySum = MethodTestLib.identity[Any](input)
      i += 1
    }
    println(s"Sum of $repeat identities of $input is $sum and Any sum is $anySum")
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

  @main def runIdentity2(): Unit = {
    val repeat = 1000000
    val input = 42
    var sum = 0
    var i = 0
    while (i < repeat) {
      sum += genericIdentity[Int](input)
      i += 1
    }
    println(s"Sum of $repeat generic identities of $input is $sum")
  }
}
