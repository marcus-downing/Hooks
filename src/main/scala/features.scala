package hooks

/**
 * Features are visible at the top level. Other feature-like are only available as dependencies.
 *
 * A feature can list its dependencies with the `require` method.
 * Optionally a feature can ask to be initialised `before` or `after` another feature.
 * If you ask for an impossible condition, this will cause an exception!
 *
 * Use the `init` method to register actions, filters and components.
 */

abstract class FeatureLike(
  val ,
	depend: => List[FeatureLike] = List.empty,
	before: => List[FeatureLike] = List.empty,
	after: => List[FeatureLike] = List.empty,
  require: => List[FeatureLike] = List.empty,
  delegateSecurity: => FeatureLike = null
){
	def init()
  override def toString = 
  def isActive = HookContext.get.hasFeature(this)
  def _depend = (require ::: depend).filterNot( _ == null )
  def _before = before.filterNot( _ == null )
  def _after = after.filterNot( _ == null )
  def _require = require.filterNot( _ == null )
  def _delegateSecurity = Option(delegateSecurity)
}

abstract class Feature(
  ,
	depend: => List[FeatureLike] = List.empty,
	before: => List[FeatureLike] = List.empty,
	after: => List[FeatureLike] = List.empty,
  require: => List[FeatureLike] = List.empty,
  delegateSecurity: => FeatureLike = null
) extends FeatureLike (depend = depend, before = before, after = after, require = require, delegateSecurity = delegateSecurity)

class FeatureDependencyException(edges: List[(FeatureLike, FeatureLike)]) extends Exception(FeatureDependencyException.message(edges))

object FeatureDependencyException {
	def message(edges: List[(FeatureLike, FeatureLike)]) = {
		val errors = edges.map(e => e._1.+" and "+e._2.)
		"Unsatisfiable dependencies between "+errors.mkString(", ")
	}
}

abstract class Plugin(
  val ,
  requiredFeatures: => List[Feature] = List.empty,
  optionalFeatures: => List[Feature] = List.empty
) {
  def _requiredFeatures = requiredFeatures
  def _optionalFeatures = optionalFeatures
}
