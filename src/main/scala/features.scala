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

trait FeatureLike {
	def name: String
	def depend: List[FeatureLike] = Nil
	def before: List[FeatureLike] = Nil
	def after: List[FeatureLike] = Nil

	def init(implicit builder: ContextBuilder)
}

trait Feature extends FeatureLike

class FeatureDependencyException(edges: List[(FeatureLike, FeatureLike)]) extends Exception(FeatureDependencyException.message(edges))

object FeatureDependencyException {
	def message(edges: List[(FeatureLike, FeatureLike)]) = {
		val errors = edges.map(e => e._1.name+" and "+e._2.name)
		"Unsatisfiable dependencies between "+errors.mkString(", ")
	}
}

trait Plugin {
  def name: String
  def requiredFeatures: List[Feature] = Nil
  def optionalFeatures: List[Feature] = Nil
}
