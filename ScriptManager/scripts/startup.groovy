import org.codehaus.groovy.control.CompilerConfiguration
import groovy.transform.*

@Field config = getConfig()

def getConfig() {
	def config = new CompilerConfiguration()
	config.setSourceEncoding("utf-8")
	return config
}

try {
	if(ScriptEngine.ENGINE == null) {
		ScriptEngine.ENGINE = _engine_
	}
	def shell = new GroovyShell(this.getBinding(), config).parse(_src_)
	def pre = shell.metaClass.methodMissing
	shell.metaClass.methodMissing = {String name, args->
		if(name.startsWith('_')) {
			def binding = new Binding(this.binding.variables)
			binding.setVariable("args", args)
			_engine_.run(name, binding)
		} else {
			pre.invoke(name, args)
		}
	}
	use(FileCategory, StringCategory, MapCategory) {
		shell.run()
	}
} catch(Throwable e) {
	if(e.getMessage()) {
		println "${e.toString()}"
	} else {
		def cause = e
		while(cause.getCause() != null) {
			cause = cause.getCause()
		}
		def trace = new StringWriter()
		cause.printStackTrace(new PrintWriter(trace))
		def list = trace.toString().split("\n")
		if(list.size() >= 10) {
			list = list[0..9]
		}
		println list.join("\n")
	}
}

