package bcGen.Arrays

object ArrayTestLib {
  def copy[T](src: Array[T], dst: Array[T]): Unit = {
    var idx = 0
    while (idx < src.length) {
      dst(idx) = src(idx)
      idx += 1
    }
    ()
  }

  def checksum[T](array: Array[T]): Int = {
    var sum = 0
    var i = 0
    while (i < array.length) {
      sum += array(i).##
      i += 1
    }
    sum
  }
}