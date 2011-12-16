package hooks

import java.lang.InheritableThreadLocal
import scala.util.DynamicVariable

object PimpDynamicVariable {
  //implicit def pimpDynamicVariable[T](dv: DynamicVariable[T]) = new PimpDynamicVariable[T](dv)
  //implicit def dymamicVariable2Option[T](dv: DynamicVariable[T]): Option[T] = Option(dv.value)
  //implicit def dymamicVariable2List[T](dv: DynamicVariable[T]): List[T] = Option(dv.value).toList
  
}

final class PimpDynamicVariable[T](dv: DynamicVariable[T]) {
  def toOption: Option[T] = Option(dv.value)
  def toList: List[T] = Option(dv.value).toList
  def apply[R](f: (T) => R): R = f(dv.value)
  def transfer = new DynamicVariableTransfer(dv, dv.value)
}


final class DynamicVariableTransfer[T](dv: DynamicVariable[T], value: T) {
  def apply[R](f: => R): R = dv.withValue(value)(f)
}