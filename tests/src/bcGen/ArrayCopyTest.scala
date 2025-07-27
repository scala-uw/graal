package bcGen

import org.openjdk.jmh.annotations.*
import benchmark.Foo

object testArray{
  val size = 10
  val maxInt = 100
  val intArr : Array[Int] = Array.fill[Int](size)(scala.util.Random.nextInt(maxInt))
  val objArr : Array[Any] = Array.fill[Any](size)(new bcGen.Foo(scala.util.Random.nextInt(maxInt)))
  val doubleArr : Array[Double] = Array.fill[Double](size)(scala.util.Random.nextDouble())
  val intDoubleArr : Array[Any] = Array.tabulate(size * 2) { i =>
    if (scala.util.Random.nextInt % 2 == 0) scala.util.Random.nextInt(maxInt) else scala.util.Random.nextDouble()
  }
  @main def mainArr1(): Unit =
    val intArrCp = new Array[Int](size)
    ArrayCopy.copy[Int](intArr, intArrCp)
    println("intArr: " + intArr.mkString(", "))
    println("intArrCp: " + intArrCp.mkString(", "))

  @main def mainArr2(): Unit =
    val anyArrCp = new Array[Any](size)
    ArrayCopy.copy[Any](objArr, anyArrCp)
    println("objArr: " + objArr.mkString(", "))
    println("arrCp: " + anyArrCp.mkString(", "))

  @main def mainArr3(): Unit =
    val doubleArrCp = new Array[Double](size)
    ArrayCopy.copy[Double](doubleArr, doubleArrCp)
    println("doubleArr: " + doubleArr.mkString(", "))
    println("doubleArrCp: " + doubleArrCp.mkString(", "))
  
  @main def mainArr4(): Unit =
    val intDoubleArrCp = new Array[Any](size * 2)
    ArrayCopy.copy[Any](intDoubleArr, intDoubleArrCp)
    println("intDoubleArr: " + intDoubleArr.mkString(", "))
    println("intDoubleArrCp: " + intDoubleArrCp.mkString(", "))

  def genericCopy[T](src: Array[T], dst: Array[T]): Unit = {
    ArrayCopy.copy(src, dst)
    println("src: " + src.mkString(", "))
    println("dst: " + dst.mkString(", "))
  }

  @main def mainArr5(): Unit =
    val intArrCp = new Array[Int](size)
    genericCopy[Int](intArr, intArrCp)

  @main def mainArr6(): Unit =
    val anyArrCp = new Array[Any](size)
    genericCopy[Any](objArr, anyArrCp)

  @main def mainArr7(): Unit =
    val doubleArrCp = new Array[Double](size)
    genericCopy[Double](doubleArr, doubleArrCp)

  @main def mainArr8(): Unit =
    val intDoubleArrCp = new Array[Any](size * 2)
    genericCopy[Any](intDoubleArr, intDoubleArrCp)
}
