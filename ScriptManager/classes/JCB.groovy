import tools.excel.*
import tools.excel.jacob.*
import tools.excel.util.*

class JCB {

    static Sheet[] sheets(Workbook wb) {
        return wb.getAllSheets()
    }
    static Sheet sheet(Workbook wb, Object index) {
        return wb.getSheet(index)
    }
    static Row row(Sheet sheet, Object index) {
        return sheet.getRow(index)
    }
    static Cell cell(Row row, Object index) {
        return row.getCell(index)
    }
    static int rowIndex(Sheet sheet, index) {
        if(index instanceof CharSequence) {
            ExcelUtil.getRowIndex(index)
        } else {
            index
        }
    }
    static int colIndex(Sheet sheet, index) {
        if(index instanceof CharSequence) {
            ExcelUtil.getColumnIndex(index)
        } else {
            index
        }
    }
    static void rows(Sheet sheet, def from, final int maxInvalid, Closure isValid, Closure cl) {
        int start
        if(from instanceof CharSequence) {
            start = ExcelUtil.getRowIndex(from.toString())
        } else {
            start = from
        }
        int cnt = 0
        for(def i = start; cnt <= maxInvalid; i++) {
            def row = sheet.getRow(i)
            if(isValid.doCall(row)) {
                cnt = 0
                cl.doCall(row)
            } else {
                cnt++
                continue
            }
        }
    }

    static void excel(File file, Closure cl) {
        def app = Application.start()
        try {
            cl.doCall(app.open(file))
        } finally {
            app.end()
        }
    }

    static void rows(Sheet sheet, def from, def to, Closure cl) {
        int start
        if(from instanceof CharSequence) {
            start = ExcelUtil.getRowIndex(from.toString())
        } else {
            start = from
        }
        int end
        if(to instanceof CharSequence) {
            end = ExcelUtil.getRowIndex(to.toString())
        } else {
            end = to
        }
        for(i in (start..end)) {
            def row = sheet.getRow(i)
            cl.doCall(row)
        }
    }
    static void cells(Row row, from='A', to='IV', int step=1, Closure cl) {
        if(from instanceof CharSequence) {
            from = ExcelUtil.getColumnIndex(from.toString())
        }
        if(to instanceof CharSequence) {
            to = ExcelUtil.getColumnIndex(to.toString())
        }
    if(step < 1) {
        throw new IllegalArgumentException("step < 1")
    }
    if(from <= to) {
            for(int i = from; i <= to; i += step) {
                cl.doCall(row.cell(i))
            }
    } else {
            for(int i = from; i >= to; i -= step) {
                cl.doCall(row.cell(i))
            }
    }
    }
    static Cell cell(Sheet sheet, String coord) {
        return sheet.getCellAt(coord)
    }
    static Object map(Sheet sheet, Map map, boolean useText=true) {
        def item = new Object()
        def cache = [:]
        item.metaClass.propertyMissing = {String name, Object value->
            // setter
            String coord = map.get(name)
            if(coord) {
                sheet.getCellAt(coord).setValue(value)
                cache.put(name, value)
            } else {
                throw new MissingPropertyException(name)
            }
        }
        item.metaClass.propertyMissing = {String name->
            // getter
            String coord = map.get(name)
            if(coord) {
                if(cache.containsKey(name)) {
                    return cache.get(name)
                } else {
                    def val = useText? sheet.cell(coord).getText() : sheet.cell(coord).getValue()
                    cache.put(name, val)
                    return val
                }
            } else {
                throw new MissingPropertyException(name)
            }
        }
        item.metaClass.toMap = {
            map.each {k,v->
                item."${k}"
            }
            cache
        }
        item.metaClass.mapping = {
            map
        }
        return item
    }
    static Object map(Row row, Map map, boolean useText=true) {
        def item = new Object()
        def cache = [:]
        item.metaClass.propertyMissing = {String name, Object value->
            // setter
            String col = map.get(name)
            if(col) {
                row.cell(col).setValue(value)
                cache.put(name, value)
            } else {
                throw new MissingPropertyException(name)
            }
        }
        item.metaClass.propertyMissing = {String name->
            // getter
            String col = map.get(name)
            if(col) {
                if(cache.containsKey(name)) {
                    return cache.get(name)
                } else {
                    def val = useText? row.cell(col).getText(): row.cell(col).getValue()
                    cache.put(name, val)
                    return val
                }
            } else {
                throw new MissingPropertyException(name)
            }
        }
        item.metaClass.toMap = {
            map.each {k,v->
                item."${k}"
            }
            cache
        }
        item.metaClass.mapping = {
            map
        }
        return item
    }
    static Cell setValue(Cell cell, Object o) {
        if(o == null) {
            return cell.setValue("")
        }
        if(o instanceof String || o instanceof Date) {
            return cell.setValue(o)
        }
        return cell.setValue(String.valueOf(o))
    }
    
    static def digits = ('A'..'Z')
    static String index2col(Integer index) {
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
    
    static int col2index(String col) {
        col = col.toUpperCase()
        if(col ==~ '[A-Z]+') {
            return col.collect {
                char ch = it
                char a = 'A'
                ch - a + 1
            }.inject(0) {s,i->
                s = s * 26 + i
                s
            } - 1
        } else if(col ==~ '[0-9]+') {
            return Integer.parseInt(col) - 1
        }
        throw new Exception("format error:${col}")
    }
    
    static void table(Sheet sheet, startRow, startCol, endCol, int skip=0, Closure cl) {
        if(startRow instanceof String) {
            startRow = tools.excel.util.ExcelUtil.getRowIndex(startRow)
        }
        if(startCol instanceof String) {
            startCol = tools.excel.util.ExcelUtil.getColumnIndex(startCol)
        }
        if(endCol instanceof String) {
            endCol = tools.excel.util.ExcelUtil.getColumnIndex(endCol)
        }
        def head = sheet.row(startRow)
        def colNames = (startCol .. endCol).inject([:]) {map,it->
            map.put(head.getCell(it).text, it.index2col())
            map
        }
        for(int i=++startRow + skip; ;i++) {
            def rowObj = sheet.row(i).map(colNames)
            if(cl.doCall(rowObj) != true) {
                break
            }
        }
    }
    
    // シートをテーブルとして編集
	static void editTable(Sheet sheet, startRow, startCol, endCol, Closure cl) {
		if(startRow instanceof String) {
			startRow = tools.excel.util.ExcelUtil.getRowIndex(startRow)
		}
		if(startCol instanceof String) {
			startCol = tools.excel.util.ExcelUtil.getColumnIndex(startCol)
		}
		if(endCol instanceof String) {
			endCol = tools.excel.util.ExcelUtil.getColumnIndex(endCol)
		}
		def head = sheet.row(startRow)
		def colNames = (startCol .. endCol).inject([:]) {map,it->
			map.put(head.getCell(it).text, it.index2col())
			map
		}
		def table = [row: {int index->
			def row = sheet.row(startRow + index + 1)
			row.map(colNames)
		}]
		cl.doCall(table)
	}
	static void editTable(Sheet sheet, startRow, Map colMap, Closure cl) {
		if(startRow instanceof String) {
			startRow = tools.excel.util.ExcelUtil.getRowIndex(startRow)
		}
		def table = [row: {int index->
			def row = sheet.row(startRow + index)
			row.map(colMap)
		}]
		cl.doCall(table)
	}
	static void editTable(Sheet sheet, startRow, Closure rowMapper, Closure cl) {
		if(startRow instanceof String) {
			startRow = tools.excel.util.ExcelUtil.getRowIndex(startRow)
		}
		def table = [row: {int index->
			def row = sheet.row(startRow + index)
			rowMapper.doCall(row)
		}]
		cl.doCall(table)
	}
}
