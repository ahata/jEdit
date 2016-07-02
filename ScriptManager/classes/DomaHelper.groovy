class DomaHelper {
	
	def Map _cache = [:]
	
	def getMethods(String src) {
		def methods = []
		def mch = src =~ /(@.+?)\s+(\S+)\s+(\w+)\(([^)]*)\);/
		mch.each {method, anno, retType, methodName, paramList->
			def list = paramList.split(",").findAll {it.trim()}.collect {
				def (a,b) = it.trim().split("\\s+")
				[type:a, name:b]
			}
			methods << [anno:anno, retType:retType, name:methodName, params:list]
		}
		return methods
	}
	
	def getEntity(File sqlFile, String entityName) {
		def parent = new File(sqlFile.parentFile.path.replaceAll('\\\\META-INF', "") + ".java").parentFile.parentFile
		def entityFile = new File(parent, /entity\${entityName}.java/)
		if(!entityFile.isFile()) {
			return [:]
		}
		def mch = entityFile.text =~ 'private\\s+(\\S+)\\s+(\\w+) ='
		def entity = _cache.get("${sqlFile}/${entityName}" as String)
		if(entity != null) {
			return entity
		}
		def rs = [:]
		mch.each {g0, g1, g2->
			rs.put(g2, [type:g1, name:g2])
		}
		_cache.put("${sqlFile}/${entityName}" as String, rs)
		rs
	}
	
	def getDaoFile(File sqlFile) {
		new File(sqlFile.parentFile.path.replaceAll('\\\\META-INF', "") + ".java")
	}
	
	def getDomaLines(String sql) {
		def mch = sql =~ '/\\*(.+?)\\*/'
		mch.collect {g0, g1->
			sql.line(mch.start()).trim()
		}
	}
}
