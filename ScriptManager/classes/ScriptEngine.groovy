class ScriptEngine {
	public static def ENGINE
	public static def run(File file, Binding bd) {
		ENGINE.run(file.path, bd)
	}
	public static def run(File file, Map binding=[:]) {
		ENGINE.run(file.path, new Binding(binding))
	}
	public static def run(File file, Object...args) {
		ENGINE.run(file.path, new Binding([args:args]))
	}
}

