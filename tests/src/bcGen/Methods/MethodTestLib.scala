package bcGen.Methods

object MethodTestLib {

  def identity[T](value: T): T = value
  
  def first[U, V](fst: U, snd: V): U = fst

  def A[T](t: T)[S, V](s : S, v: V, v2: V)
    [W, X, Y](w: W, x: X, y: Y)(byte: Byte, int: Int, double: Double, ref: Any) = {
    println(s"A called with t: $t, s: $s, v: $v, v2: $v2, w: $w, x: $x, y: $y" +
      s", byte: $byte, int: $int, double: $double, ref: $ref")
  }

  def B[T](t: T, s: String, t2: T): Unit = {
    println(s"B called with t: $t, s: $s, t2: $t2")
  }

  // def A1[T, S](x: T, s: S)[R] = {}

  // def genericIdentity[T](value: T): T = identity[T](value)
}
