package bcGen.Methods

object MethodTestLib {
  class Foo(val id: Int){
    override def toString: String = "Printing Foo " + id
  }
  
  def identity[T](value: T): T = value
  
  def first[U, V](fst: U, snd: V): U = fst
}
