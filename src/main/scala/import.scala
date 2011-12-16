//package hooks

import scala.util.DynamicVariable

/**
 * Helper functions
 * These implicit conversions are loaded with the hooks package
 */

package object hooks {
  //import ApplicableVariable.dynamicVariable2applicable
  implicit def pimpDynamicVariable[T](dv: DynamicVariable[T]) = new PimpDynamicVariable[T](dv)
  //implicit def dymamicVariable2Option[T](dv: DynamicVariable[T]): Option[T] = Option(dv.value)
  //implicit def dymamicVariable2Option[T](dv: DynamicVariable[T]): List[T] = Option(dv.value).toList

  /**
   * Helper imports
   * These implicits are used in the hooks themselves to disambiguate
   */

  object Imports {
    //  dummy implicits 0-F
    object D0 { implicit val d = this }; type D0 = D0.type;
    object D1 { implicit val d = this }; type D1 = D1.type;
    object D2 { implicit val d = this }; type D2 = D2.type;
    object D3 { implicit val d = this }; type D3 = D3.type;
    object D4 { implicit val d = this }; type D4 = D4.type;
    object D5 { implicit val d = this }; type D5 = D5.type;
    object D6 { implicit val d = this }; type D6 = D6.type;
    object D7 { implicit val d = this }; type D7 = D7.type;
    object D8 { implicit val d = this }; type D8 = D8.type;
    object D9 { implicit val d = this }; type D9 = D9.type;
    object DA { implicit val d = this }; type DA = DA.type;
    object DB { implicit val d = this }; type DB = DB.type;
    object DC { implicit val d = this }; type DC = DC.type;
    object DD { implicit val d = this }; type DD = DD.type;
    object DE { implicit val d = this }; type DE = DE.type;
    object DF { implicit val d = this }; type DF = DF.type;
  }
}
