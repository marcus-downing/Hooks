package hooks

/**
 * Features are plugins which are visible at the top level. Other plugins are only available as dependencies.
 *
 * A plugin can list its dependencies with the `require` method.
 * Optionally a plugin can ask to be initialised `before` or `after` another plugin.
 * If you ask for an impossible condition, this will cause an exception!
 *
 * Use the `init` method to register actions, filters and components.
 */

trait Plugin {
	def name: String
	def require: List[Plugin]
	def before: List[Plugin] = Nil
	def after: List[Plugin] = Nil

	def init(implicit builder: PluginContextBuilder)
}

trait Feature extends Plugin

class PluginDependencyException(edges: List[(Plugin, Plugin)]) extends Exception(PluginDependencyException.message(edges))

object PluginDependencyException {
	def message(edges: List[(Plugin, Plugin)]) = {
		val errors = edges.map(e => e._1.name+" and "+e._2.name)
		"Unsatisfiable dependencies between "+errors.mkString(", ")
	}
}