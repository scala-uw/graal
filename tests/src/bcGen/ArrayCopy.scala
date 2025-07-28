package bcGen

object ArrayCopy {
  def copy[T](src: Array[T], dst: Array[T]): Unit = {
    var idx = 0
    while (idx < src.length) {
      dst(idx) = src(idx)
      idx += 1
    }
    ()
  }
}