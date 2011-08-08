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
	def before: List[Plugin] = List()
	def after: List[Plugin] = List()

	def init(implicit context: PluginContext)
}

trait Feature extends Plugin