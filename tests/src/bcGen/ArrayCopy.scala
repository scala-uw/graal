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

//  def copy2[T](src: Array[T], dst: Array[T]) : Unit = {
//    var idx = 0
//    (src, dst) match {
//      case (src_i: Array[Int], dst_i: Array[Int]) => {
//        while (idx < src_i.length) {
//          dst_i(idx) = src_i(idx)
//          idx += 1
//        }
//        ()
//      }
//      case (src_d: Array[Double], dst_d: Array[Double]) => {
//        while (idx < src_d.length) {
//          dst_d(idx) = src_d(idx)
//          idx += 1
//        }
//        ()
//      }
//      case (src_d: Array[Char], dst_d: Array[Char]) => {
//        while (idx < src_d.length) {
//          dst_d(idx) = src_d(idx)
//          idx += 1
//        }
//        ()
//      }
//      case _ => copy(src, dst)
//    }
//  }
}