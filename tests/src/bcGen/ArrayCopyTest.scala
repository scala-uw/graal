package bcGen

import org.openjdk.jmh.annotations.*
import benchmark.Foo

object testArray{
  val size = 1000
  val maxInt = 100
  val rng = new scala.util.Random(1234)
  val intArr : Array[Int] = Array.fill[Int](size)(rng.nextInt(maxInt))
  val objArr : Array[Any] = Array.fill[Any](size)(new bcGen.Foo(rng.nextInt(maxInt)))
  val doubleArr : Array[Double] = Array.fill[Double](size)(rng.nextDouble())
  val intDoubleArr : Array[Any] = Array.tabulate(size * 2) { i =>
    if (rng.nextInt % 2 == 0) rng.nextInt(maxInt) else rng.nextDouble()
  }
  val intArrCp = new Array[Int](size)
  val anyArrCp = new Array[Any](size)
  val doubleArrCp = new Array[Double](size)
  val intDoubleArrCp = new Array[Any](size * 2)

  var hashState: Long = 0
  val hashMod: Long = 998244353
  @main def mainArr1(): Unit =
    ArrayCopy.copy[Int](intArr, intArrCp)
    for (e <- intArrCp) {
      hashState = (hashState * 23 + e) % hashMod
    }

  @main def mainArr2(): Unit =
    ArrayCopy.copy[Any](objArr, anyArrCp)
    for (e <- anyArrCp) {
      hashState = (hashState * 23 + e.id) % hashMod
    }

  @main def mainArr3(): Unit =
    ArrayCopy.copy[Double](doubleArr, doubleArrCp)
    for (e <- doubleArrCp) {
      hasState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(e) % hashMod) % hashMod
    }
  
  @main def mainArr4(): Unit =
    ArrayCopy.copy[Any](intDoubleArr, intDoubleArrCp)
    for (e <- intDoubleArrCp) {
      e match {
        case i: Int => hashState = (hashState * 23 + i) % hashMod
        case d: Double => hashState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(d) % hashMod) % hashMod
        case _ => {}
      }
    }

  def genericCopy[T](src: Array[T], dst: Array[T]): Unit = {
    ArrayCopy.copy(src, dst)
  }

  @main def mainArr5(): Unit =
    genericCopy[Int](intArr, intArrCp)
    for (e <- intArrCp) {
      hashState = (hashState * 23 + e) % hashMod
    }

  @main def mainArr6(): Unit =
    genericCopy[Any](objArr, anyArrCp)
    for (e <- anyArrCp) {
      hashState = (hashState * 23 + e.id) % hashMod
    }

  @main def mainArr7(): Unit =
    genericCopy[Double](doubleArr, doubleArrCp)
    for (e <- doubleArrCp) {
      hasState = (hashState * 23 + java.lang.Double.doubleToRawLongBits(e) % hashMod) % hashMod
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
  
  @main def runAllArr: Unit =
    val repeat = 10000
    for (_ <- 1 to repeat) {
      mainArr1()
    }
    for (_ <- 1 to repeat) {
      mainArr2()
    }
    for (_ <- 1 to repeat) {
      mainArr3()
    }
    for (_ <- 1 to repeat) {
      mainArr4()
    }
    for (_ <- 1 to repeat) {
      mainArr5()
    }
    for (_ <- 1 to repeat) {
      mainArr6()
    }
    for (_ <- 1 to repeat) {
      mainArr7()
    }
    for (_ <- 1 to repeat) {
      mainArr8()
    }
    println(hashState)
    
}
