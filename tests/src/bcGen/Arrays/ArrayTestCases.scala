package bcGen.Arrays

import bcGen.Arrays.ArrayTestLib

class Foo(val id: Int) {}
object arrayTestCases{
  val size = 1000
  val maxInt = 10000
  val rng = new scala.util.Random(1234)
  val intArr : Array[Int] = Array.fill[Int](size)(rng.nextInt(maxInt))
  val objArr : Array[Any] = Array.fill[Any](size)(new Foo(rng.nextInt(maxInt)))
  val doubleArr : Array[Double] = Array.fill[Double](size)(rng.nextDouble())
  val intDoubleArr : Array[Any] = Array.tabulate(size * 2) { i =>
    if (rng.nextInt % 2 == 0) rng.nextInt(maxInt) else rng.nextDouble()
  }
  val shortArrInt = Array.fill[Int](10)(rng.nextInt(maxInt))
  val shortArrDouble = Array.fill[Double](10)(rng.nextDouble())
  val shortArrObj = Array.fill[Any](10)(new Foo(rng.nextInt(maxInt)))
  val shorArrIntCp = new Array[Int](10)
  val shorArrDoubleCp = new Array[Double](10)
  val shorArrObjCp = new Array[Any](10)
  val intArrCp = new Array[Int](size)
  val anyArrCp = new Array[Any](size)
  val doubleArrCp = new Array[Double](size)
  val intDoubleArrCp = new Array[Any](size * 2)

  var hashState: Long = 0
  val hashMod: Long = 998244353
  @main def mainArr1(): Unit =
    ArrayTestLib.copy[Int](intArr, intArrCp)
    for (e <- intArrCp) {
      hashState = (hashState * 23 + e) % hashMod
    }

  @main def mainArr2(): Unit =
    ArrayTestLib.copy[Any](objArr, anyArrCp)
    for (e <- anyArrCp) {
      hashState = (hashState * 23 + e.asInstanceOf[Foo].id) % hashMod
    }

  @main def mainArr3(): Unit =
    ArrayTestLib.copy[Double](doubleArr, doubleArrCp)
    for (e <- doubleArrCp) {
      hashState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(e) % hashMod) % hashMod
    }
  
  @main def mainArr4(): Unit =
    ArrayTestLib.copy[Any](intDoubleArr, intDoubleArrCp)
    for (e <- intDoubleArrCp) {
      e match {
        case i: Int => hashState = (hashState * 23 + i) % hashMod
        case d: Double => hashState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(d) % hashMod) % hashMod
        case _ => {}
      }
    }

  def genericCopy[T](src: Array[T], dst: Array[T]): Unit = {
    ArrayTestLib.copy(src, dst)
  }

  @main def mainArr5(): Unit =
    genericCopy[Int](intArr, intArrCp)
    for (e <- intArrCp) {
      hashState = (hashState * 23 + e) % hashMod
    }

  @main def mainArr6(): Unit =
    genericCopy[Any](objArr, anyArrCp)
    for (e <- anyArrCp) {
      hashState = (hashState * 23 + e.asInstanceOf[Foo].id) % hashMod
    }

  @main def mainArr7(): Unit =
    genericCopy[Double](doubleArr, doubleArrCp)
    for (e <- doubleArrCp) {
      hashState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(e) % hashMod) % hashMod
    }

  @main def mainArr8(): Unit =
    genericCopy[Any](intDoubleArr, intDoubleArrCp)
    for (e <- intDoubleArrCp) {
      e match {
        case i: Int => hashState = (hashState * 23 + i) % hashMod
        case d: Double => hashState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(d) % hashMod) % hashMod
        case _ => {}
      }
    }

  @main def runChecksumInt(): Unit = 
    val repeat = 10000
    var i = 1
    var sum: Long = 0
    while i <= repeat do
      sum += ArrayTestLib.checksum[Int](intArr)
      i += 1
    println(sum)

  @main def runChecksumInt2(): Unit =
    var i = 0
    while (i < shortArrInt.length) do {
      println(shortArrInt(i) + " : " + shortArrInt(i).##)
      i += 1
    }
    val sum = ArrayTestLib.checksum[Int](shortArrInt)
    println(sum)
    val sum2 = ArrayTestLib.checksum[Int](shortArrInt)
    println(sum2)
  
  @main def runAllArr(): Unit =
    val repeat = 1000
    var i = 1
    while i <= repeat do{
      mainArr1()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr2()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr3()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr4()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr5()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr6()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr7()
      i += 1
    }
    i = 1
    while i <= repeat do {
      mainArr8()
      i += 1
    }
    println(hashState)

  @main def runInt(): Unit =
    val repeat = 10000
    var i = 1
    while i <= repeat do
      ArrayTestLib.copy[Int](intArr, intArrCp)
      i += 1

  @main def runMixed(): Unit = 
    val repeat = 20000
    var i = 1
    var is = 0
    var anys = 0
    var ds = 0
    var elses = 0
    while i <= repeat do
      val n = rng.nextInt(4)
      if (n == 0) {
        ArrayTestLib.copy[Int](intArr, intArrCp)
        is += 1
      } else if (n == 1) {
        ArrayTestLib.copy[Any](objArr, anyArrCp)
        anys += 1
      } else if (n == 2) {
        ArrayTestLib.copy[Double](doubleArr, doubleArrCp)
        ds += 1
      } else {
        ArrayTestLib.copy[Any](intDoubleArr, intDoubleArrCp)
        elses +=1
      }
      i += 1
    println(System.nanoTime())
    i = 1
    is = 0
    anys = 0
    ds = 0
    elses = 0
    while i <= repeat do
      val n = rng.nextInt(4)
      if (n == 0) {
        ArrayTestLib.copy[Int](intArr, intArrCp)
        is += 1
      } else if (n == 1) {
        ArrayTestLib.copy[Any](objArr, anyArrCp)
        anys += 1
      } else if (n == 2) {
        ArrayTestLib.copy[Double](doubleArr, doubleArrCp)
        ds += 1
      } else {
        ArrayTestLib.copy[Any](intDoubleArr, intDoubleArrCp)
        elses +=1
      }
      i += 1
    println(is)
    println(anys)
    println(ds)
    println(elses)
    println(System.nanoTime())
}