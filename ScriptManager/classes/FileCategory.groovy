class FileCategory {
	
	static File find(File file, Closure cl) {
		def list = findAll(file, cl)
		if(list) {
			return list[0]
		}
		return null
	}
	
	static List findAll(File file, Closure cl) {
		def list = []
		file.eachFile {
			if(cl.doCall(it)) {
				list << it
			}
		}
		return list
	}
	
	static File findRecurse(File file, Closure cl) {
		def list = findAllRecurse(file, cl)
		if(list) {
			return list[0]
		}
		return null
	}
	
	static List findAllRecurse(File file, Closure cl) {
		def list = []
		file.eachFileRecurse {
			if(cl.doCall(it)) {
				list << it
			}
		}
		return list
	}
	
	static void copyTo(File from, File to) {
		if(from.directory) {
			assert to.directory || to.mkdir(), "fail to create dir: ${to}"
			from.eachFile {sub->
				copyTo(sub, new File(to, sub.name))
			}
		} else if(from.file) {
			to.withOutputStream {output->
				from.withInputStream() {input->
					output << input
				}
			}
		}
	}
	
	static boolean rename(File from, String name) {
		return from.renameTo(new File(from.parentFile, name))
	}
	
	static void walk(File root, filter=null, Closure cl) {
		if(!root.isDirectory()) {
			throw new IllegalArgumentException("${root} is not a directory.")
		}
		def clFilter
		if(filter == null) {
			clFilter = {false}
		} else if(filter instanceof String) {
			clFilter = {it.name =~ filter}
		} else if(filter instanceof Closure){
			if(filter.getMaximumNumberOfParameters() == 1) {
				clFilter = filter
			} else {
				clFilter = filter.curry(root)
			}
		} else {
			throw new IllegalArgumentException("filter")
		}
		
		if(cl.getMaximumNumberOfParameters() == 1) {
			_walk(root, clFilter, cl)
		} else {
			_walk(root, clFilter, cl.curry(root))
		}
	}
	
	private static void _walk(File dir, Closure filter, Closure cl) {
		if(filter(dir)) {
			return
		}
		dir.eachFile {
			if(it.isDirectory()) {
				_walk(it, filter, cl)
			}
			cl.doCall(it)
		}
	}
	
	static void addBom(File file) {
		def temp = null
		file.withInputStream() {fin->
			byte[] buf = new byte[3]
			byte[] sig = [0xEF, 0xBB, 0xBF]
			fin.mark(buf.length)
			fin.read(buf)
			fin.reset()
			if(buf != sig) {
				temp = File.createTempFile(file.name, "", file.parentFile)
				temp.withOutputStream {os->
					os.write(sig)
					os << fin
				}
			}
		}
		if(temp?.isFile()) {
			file.delete()
			temp.renameTo(file)
		}
	}
	
	static boolean removeBom(File file) {
		if(!hasBom(file)) {
			return false
		}
		def temp = null
		file.withInputStream() {fin->
			byte[] buf = new byte[3]
			// ignore bom
			fin.read(buf)
			temp = File.createTempFile(file.name, "", file.parentFile)
			temp.withOutputStream {os->
				// write bytes that after bom
				os << fin
			}
		}
		if(temp?.isFile()) {
			file.delete()
			temp.renameTo(file)
		}
		return true
	}
	
	static boolean hasBom(File file) {
		def result
		file.withInputStream() {fin->
			byte[] buf = new byte[3]
			byte[] sig = [0xEF, 0xBB, 0xBF]
			fin.read(buf)
			result = buf == sig
		}
		return result
	}
	
	static String extname(File file) {
		def index = file.name.lastIndexOf('.')
		if(index >= 0) {
			return file.name.substring(index + 1)
		} else {
			return ""
		}
	}
	
	static void edit(File file, String encode = System.getProperty("file.encoding"), Closure cl) {
		if(file.hasBom()) {
			file.setText(cl(file.getText()), "UTF-8")
			file.addBom()
		} else {
			file.setText(cl(file.getText(encode)), encode)
		}
	}
	
	static String md5(File file) {
		tools.md5.MD5Utils.getMD5(file)
	}
	
	static def cache(File file, String key, Closure objGen) {
		def dir = new File("c:/_objcache_")
		if(!dir.isDirectory()) {
			dir.mkdir()
		}
		def objFile = new File(dir, key)
		def cachedObj = null
		if(objFile.isFile()) {
			try {
				objFile.withObjectInputStream {
					cachedObj = it.readObject()
				}
			} catch(Exception e) {
			}
		}
		def needUpdate = {obj->
			if(obj == null) {
				return true
			}
			def (modifyTime, o) = obj
			if(modifyTime < file.lastModified()) {
				true
			} else {
				false
			}
		}
		if(needUpdate.doCall(cachedObj)) {
			def obj = objGen.doCall()
			new File(dir, key).withObjectOutputStream {
				it.writeObject([file.lastModified(), objGen.doCall()])
			}
			return obj
		} else {
			return cachedObj[1]
		}
	}
	
	static def run(File script, Map binding=[:]) {
		ScriptEngine.run(script, new Binding(binding))
	}
	static def run(File script, Object...args) {
		ScriptEngine.run(script, *args)
	}
	static boolean remove(File file) {
		if(file.isFile()) {
			return file.delete()
		} else if(file.isDirectory()) {
			def rs = []
			file.eachFile {
				rs << remove(it)
			}
			return rs.every {it} && file.delete()
		}
		return false
	}
}

