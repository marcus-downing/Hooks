package hooks

trait Plugin(val name: String) {
	def require: List[Plugin]
	def before: List[Plugin]
	def after: List[Plugin]

	def init(implicit context: PluginContext)
}

trait Feature(name: String) extends Plugin(name)