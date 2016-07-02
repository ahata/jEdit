class IndexedTableDef {
	
	String path
	
	IndexedTableDef(path) {
		this.path = path
	}
	
	def getTable(name) {
		def file = new File(path).find {
			def begin = it.name.lastIndexOf('(')
			def end = it.name.lastIndexOf(')')
			assert begin >=0 && end >=0, "DB定義ファイル${it}の名前不正。"
			def lName = it.name.substring(0, begin)
			def pName = it.name.substring(begin + 1, end)
			return name == lName || name.equalsIgnoreCase(pName)
		}
		
		if(file != null) {
			def node = new XmlParser().parse(file)
			def cols = node.Column.collect {
				[論理名:it.@LName, 物理名:it.@PName, 主キー:it.@PK, ドメイン:it.@Domain, 属性:it.@Type, 長さ:it.@Length, 精度:it.@Accuracy, ディフォルト値:it.@Default, 非NULL:it.@NotNull]
			}
			def table = [論理名:node.@LName, 物理名:node.@PName, cols:cols]
			return table
		}
		return null
	}
}

