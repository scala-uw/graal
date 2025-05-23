package bcGen

class GenericMethod {
  // non-generic method, no type hint
  def passInt(x: Int): Int = x
  // non-generic method, no type hint
  def passRef(x: ClassRef): ClassRef = x
  // @MethodTypeParameterCount: 1
  // @MethodParameterType: M0
  // @MethodReturnType: M0
  def identity[T](value: T): T = value
  // non-generic method, no type hint
  def identity2(value: Any): Any = value
  // @MethodTypeParameterCount: 1
  // @MethodParameterType: M0, M1
  // @MethodReturnType: M0
  def first[A, B](fst: A, snd: B): A = fst
  override def toString: String = "Printing GenericMethod"
}

object testGenericMethod {
  @main def testGenericMethodMain(): Unit = {
    println("test1:")
    /* prints:
       23
       2.2
       c
       Printing ClassRef 1
       Printing ClassRef 2
       8
       87
       88
    */
    test1()
    println("test2[Int, Char, Double]:")
    /* prints:
       9.9
       7
       7
       v
       7
     */
    test2[Int, Char, Double](7, 'v', 9.9)
    println("test2[ClassRef, ClassRef, ClassRef")
    /* prints:
       Printing ClassRef -3
       Printing ClassRef -1
       Printing ClassRef -1
       Printing ClassRef -2
       Printing ClassRef -1
     */
    test2[ClassRef, ClassRef, ClassRef](new ClassRef(-1), new ClassRef(-2), new ClassRef(-3))
  }
  //if (methodParameterTpye is generic (has generic methodParameterType type hint) &&
  //    reified type is primitive &&
  //    argument type before invoke is non-generic) {eunbox (before invoke)}
  // if (methodReturnType is generic (has methodReturnType type hint) &&
  //     reified type is primitive &&
  //     return type at call site is non-generic, InvokeReturnType is 'L', not 'M0', 'K0') {eBox (in big loop)}
  def test1() : Unit = {
    val gm = new GenericMethod
    // passing a primitive type as
    // type argument to a generic method
    // @InstructionTypeArguments: offset, I
    // @InvokeReturnType: offset, L (?TBD)
    val v1 = gm.identity[Int](23) //need InstructionTypeArg: I, InvokeReturnType: L (boxing needed in case INVOKE)
    println(v1)
    // passing to a non-generic method
    // with java.lang.Object as parameter
    // no hints needed
    val v2 = gm.identity2(2.2)
    println(v2)
    // passing two different primitive types 
    // as type arguments to a generic method
    // @InstructionTypeArguments: offset, C D
    // @InvokeReturnType: offset, L (?TBD)
    val v3 = gm.first[Char, Double]('c', 8.8) //same as v1, need InstructionTypeArg: CD, InvokeReturnType: L (boxing needed)
    println(v3)
    // passing a scala class as
    // type argument to a generic method
    // @InstructionTypeArguments: offset, L
    // @InvokeReturnType: offset, L
    val v4 = gm.identity[ClassRef](new ClassRef(1)) //need InstructionTypeArg: L, InvokeReturnType: L (no boxing needed)
    println(v4)
    // passing a scala class to a method
    // that takes ClassRef as parameter
    // no hints needed
    val v5 = gm.passRef(new ClassRef(2))
    println(v5)
    // passing a java class as
    // type argument to a generic method
    // @InstructionTypeArguments: offset, L
    // @InvokeReturnType: offset, L
    val v6 = gm.identity[java.lang.Integer](java.lang.Integer.valueOf(8))
    println(v6)
    // passing a java class to a 
    // non-generic method
    // with java.lang.Object as parameter
    // no hints needed
    val v7 = gm.identity2(java.lang.Integer.valueOf(87))
    println(v7)
    // passing a scala int to a
    // non-generic method
    // no hints needed
    val v8 = gm.passInt(88)
    println(v8)
  }
  //def test2$[U,X,Y](u:U,x:X,y:Y) = test2[U,X,Y](u,x,y)
  def test2[U, X, Y](value: U, fst: X, snd: Y): Unit = {
    val gm = new GenericMethod
    // passing a type argument of current method
    // and a value
    // of that type to a generic method
    // @InstructionTypeArguments: offset, M2
    // @InvokeReturnType: offset, M2
    val v1 = gm.identity[Y](snd) //need InstructionTypeArg: M2, InvokeReturnType: M2?
    println(v1)
    // passing a value of generic type
    // to a non-generic method
    // no hints needed
    val v2 = gm.identity2(value)
    println(v2)
    // passing a value of generic type
    // to a generic method with
    // type parameter Any(java.lang.Object)
    // @InstructionTypeArguments: offset, L
    // @InvokeReturnType: offset, L
    val v3 = gm.identity[Any](value)
    println(v3)
    // passing two values of generic types
    // to a generic method
    // @InstructionTypeArguments: offset, M1 M2
    // @InvokeReturnType: offset, M1
    val v4 = gm.first[X, Y](fst, snd)
    println(v4)
    // passing two values, one a scala class
    // and the other a generic type
    // to a generic method
    // @InstructionTypeArguments: offset, L M2
    // @InvokeReturnType: offset, L
    val v5 = gm.first[Any, Y](value, snd)
    println(v5)
  }
}