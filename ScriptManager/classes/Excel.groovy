class Excel {

	static final def digits = ('A'..'Z')

	static int getIndex(CharSequence str) {
		def index = str.toString().toUpperCase()
		if(index ==~ '[A-Z]+') {
			index.collect {
				char ch = it
				char a = 'A'
				ch - a + 1
			}.inject(0) {s,i->
				s = s * 26 + i
				s
			} - 1
		} else if(index ==~ '[0-9]+') {
			Integer.parseInt(index) - 1
		} else {
			throw new Exception("format error:${index}")
		}
	}

	static int getIndex(Integer value) {
		return value
	}
	
	static String getCol(Integer index) {
		def rs = []
		int val = index + 1
		while(true) {
			rs << digits[(val - 1) % 26]
			val = (val - 1) / 26
			if(val == 0) {
				break
			}
		}
		rs.reverse().join()
	}

	private static def _parse(File excel, sheetName, startRow, startCol, endCol, Closure rowMapper=null, int step=1) {
		if(!excel.isFile()) {
			throw new IllegalArgumentException("${excel.path} is not file.")
		}
		if(step < 1) {
			throw new IllegalArgumentException("step < 1.")
		}
		startRow = startRow.index
		startCol = startCol.index
		endCol = endCol.index
		
		def rows = []
		excel.excel {wb->
			def sheet = wb.sheet(sheetName)
			if(rowMapper == null) {
				def head = sheet.row(startRow)
				def colNames = (startCol .. endCol).inject([:]) {map,it->
					map.put(head.cell(it).getText(), it.col)
					map
				}
				rowMapper = {rowObj->
					def obj = rowObj.map(colNames).toMap()
					if(obj.every {k,v-> v == "" || v == null}) {
						return null
					}
					return obj
				}
				startRow++
			}
			
			for(int i=startRow; ;i += step) {
				def rowObjs = (i .. (i + step - 1)).collect {
					sheet.row(it)
				}
				def obj
				if(step > 1) {
					obj = rowMapper.doCall(rowObjs)
				} else {
					obj = rowMapper.doCall(rowObjs[0])
				}
				if(!obj) {
					break
				}
				rows << obj
			}
		}
		return rows
	}

	static def exceltable(File excel, sheetName, startRow, startCol, endCol, Closure rowMapper, int step=1) {
		def key = "exceltable" + "${excel.path}/${sheetName}/${startRow}/${startCol}/${endCol}".md5()
		def objGen = {
			_parse(excel, sheetName, startRow, startCol, endCol, rowMapper, step)
		}
		excel.cache(key, objGen)
	}

	static def exceltable(File excel, sheetName, startRow, Closure rowMapper, int step=1) {
		def key = "exceltable" + "${excel.path}/${sheetName}/${startRow}/A/Z".md5()
		def objGen = {
			_parse(excel, sheetName, startRow, "A", "Z", rowMapper, step)
		}
		excel.cache(key, objGen)
	}

	static def exceltable(File excel, sheetName, startRow, startCol, endCol) {
		def key = "exceltable" + "${excel.path}/${sheetName}/${startRow}/${startCol}/${endCol}".md5()
		def objGen = {
			_parse(excel, sheetName, startRow, startCol, endCol)
		}
		excel.cache(key, objGen)
	}

	static def exceltable(File excel, sheetName, startRow, Map colMap) {
		def rowMapper = {row->
			def obj = row.map(colMap).toMap()
			if(obj.every {k,v-> v == "" || v == null}) {
				return null
			}
			return obj
		}
		exceltable(excel, sheetName, startRow, "A", "Z", rowMapper, 1)
	}

	static def excelobj(File workbook, sheet, Map mapping) {
		def key = "excelobj" + "${workbook.path}/${sheet}/${mapping.collect {k,v-> v}.sort().join('/')}".md5()
		def objGen = {
			def rs = null
			workbook.excel {wb->
				rs = wb.sheet(sheet).map(mapping).toMap()
			}
			return rs
		}
		workbook.cache(key, objGen)
	}
}
