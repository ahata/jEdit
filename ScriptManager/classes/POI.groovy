import java.text.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.hssf.util.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.*

class SheetWrapper {
	def sheet
	def cache = [:]
	def useText
	def Map map
	
	SheetWrapper(sheet, map, useText) {
		this.sheet = sheet
		this.map = map
		this.useText = useText
	}
	
	def propertyMissing(String name, Object value) {
		// setter
		String coord = map.get(name)
		if(coord) {
			sheet.cell(coord).setValue(value)
			cache.put(name, value)
		} else {
			throw new MissingPropertyException(name)
		}
	}
	
	def propertyMissing(String name) {
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
	
	def toMap() {
		map.each {k,v->
			this."${k}"
		}
		cache
	}
	
	def mapping() {
		map
	}
}

class RowWrapper {
	def row
	def useText
	def Map map
	def cache = [:]
	
	RowWrapper(row, map, useText) {
		this.row = row
		this.map = map
		this.useText = useText
	}
	
	def propertyMissing(String name, Object value) {
		// setter
		String col = map.get(name)
		if(col) {
			row.cell(col).setValue(value)
			cache.put(name, value)
		} else {
			throw new MissingPropertyException(name)
		}
	}
	
	def propertyMissing(String name) {
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
	
	def toMap() {
		map.each {k,v->
			this."${k}"
		}
		cache
	}
	
	def mapping() {
		map
	}	
}

class ExtDateFormat extends java.text.Format {
	String format
	
	ExtDateFormat(format) {
		this.format = format
	}
	def sdf = new SimpleDateFormat(format)
	
	StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if(obj instanceof Date) {
			toAppendTo.append(sdf.format(obj))
		} else {
			toAppendTo.append(sdf.format(DateUtil.getJavaDate(obj)))
		}
	}
	
	Object parseObject(String source, ParsePosition pos) {
	}
	
	Date parse(String str) {
		try {
			sdf.parse(str)
		} catch(e) {
			null
		}
	}
}

enum Type {
	hssf,
	xssf
}

class MyDateUtil {
	
	static final PTNS = [
		'm/d/yy':'yyyy/M/d',
		'[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy':'yyyy年M月d日',
		'[$-411]ge\\.m\\.d;@':'yyyy/M/d',
		'[$-411]ggge"年"m"月"d"日";@':'yyyy/M/d',
		'yyyy"年"m"月"d"日";@':'yyyy年M月d日',
		'yyyy"年"m"月";@':'yyyy年M月',
		'm"月"d"日";@':'M月d日',
		'yyyy/m/d;@':'yyyy/M/d',
		'[$-409]yyyy/m/d\\ h:mm\\ AM/PM;@':'yyyy/M/d h:m a',
		'yyyy/m/d\\ h:mm;@':'yyyy/M/d H:m',
		'm/d;@':'M/d',
		'm/d/yy;@':'M/d/yy',
		'mm/dd/yy;@':'MM/dd/yy',
	].inject([:]) {s,v->
		if(v.value) {
			s.put(v.key, new ExtDateFormat(v.value))
		} else {
			s.put(v.key, null)
		}
		s
	}
}

class MyWorkbook {
	
	@Delegate Workbook _workbook
	@Lazy _evaluator = getCreationHelper().createFormulaEvaluator()
	Type _type
	File _file
	
	MyWorkbook(File file) {
		_file = file
		if(file.name =~ '(?i)xls$') {
			_type = Type.hssf
		} else if(file.name =~ '(?i)xlsx$') {
			_type = Type.xssf
		} else {
			throw new RuntimeException("Only xls and xlsx format are supported.")
		}
		if(file.isFile()) {
			file.withInputStream {
				_workbook = WorkbookFactory.create(it)
			}
		} else {
			if(_type == Type.hssf) {
				_workbook = new HSSFWorkbook()
			} else {
				_workbook = new XSSFWorkbook()
			}
		}
		_workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK)
	}
	
	def getEvaluator() {
		_evaluator
	}
	
	MyWorkbook getWorkbook() {
		return this
	}

	Sheet sheet(String name) {
		def s = _workbook.getSheet(name)
		return s == null ? null : new MySheet(s)
	}

	Sheet sheet(int index) {
		def s = _workbook.getSheetAt(index)
		return s == null ? null : new MySheet(s)
	}

	Sheet createSheet() {
		new MySheet(_workbook.createSheet())
	}

	Sheet createSheet(String name) {
		new MySheet(_workbook.createSheet(name))
	}
	
	void save() {
		_file.withOutputStream {
			_workbook.write(it)
		}
	}
	
	void saveAs(File path) {
		path.withOutputStream {
			_workbook.write(it)
		}
	}
	
	void saveAs(String path) {
		saveAs(new File(path))
	}
	
	def getSheets() {
		(0 ..< _workbook.getNumberOfSheets()).collect {
			new MySheet(_workbook.getSheetAt(it))
		}
	}
	
	def getSheetNum() {
		_workbook.getNumberOfSheets()
	}
	
	Font cloneFont(Font font) {
		def f2 = createFont()
		f2.setBoldweight(font.getBoldweight())
		f2.setCharSet(font.getCharSet())
		f2.setColor(font.getColor())
		f2.setFontHeight(font.getFontHeight())
		f2.setFontHeightInPoints(font.getFontHeightInPoints())
		f2.setFontName(font.getFontName())
		f2.setItalic(font.getItalic())
		f2.setStrikeout(font.getStrikeout())
		f2.setTypeOffset(font.getTypeOffset())
		f2.setUnderline(font.getUnderline())
		return f2
	}
	
	class MySheet {
		@Delegate Sheet _sheet
		
		MySheet(Sheet sheet) {
			_sheet = sheet
		}
		
		String getName() {
			_sheet.getSheetName()
		}
		
		Row row(String row) {
			new MyRow(_sheet.getRow(row.index)?:_sheet.createRow(row.index))
		}
		
		Row row(int index) {
			new MyRow(_sheet.getRow(index)?:_sheet.createRow(index))
		}
		
		Cell cell(String coord) {
			def mch = coord =~ '(?i)^([A-Z]+)([0-9]+)$'
			if(!mch) {
				throw new IllegalArgumentException(coord)
			}
			def (col, row) = [mch[0][1], mch[0][2]].collect {
				it.index
			}
			this.row(row).cell(col)
		}
		
		void eachRow(Closure cl) {
			for(Row row : _sheet) {
				cl.doCall(new MyRow(row))
			}
		}

		def readCol2End(x, y) {
			def row = row(x.index)
			def result = []
			for(int i=y.index;;i++) {
				def cell = row.cell(i)
				def val = cell.value
				if(!val) {
					break
				}
				result.add(val)
			}
			return result
		}
		
		def readRow2End(x, y) {
			def result = []
			def colInd = y.index
			for(int i=x.index;;i++) {
				def row = row(i)
				def cell = row.cell(colInd)
				def val = cell.value
				if(!val) {
					break
				}
				result.add(val)
			}
			return result
		}

		def matrix(x, y) {
			def startX = x.index
			def startY = y.index
			def cols = readCol2End(x, y)
			def rows = readRow2End(x, y)
			def result = []
			rows.eachWithIndex {r,i->
				def xrow = sheet.row(startX + i)
				def _cols = []
				cols.eachWithIndex {c,j->
					def xcell = xrow.cell(startY + j)
					_cols.add(xcell.value)
				}
				result.add(_cols)
			}
			return result
		}

		MySheet getSheet() {
			return this
		}
		
		def select() {
			getWorkbook().setActiveSheet(getIndex())
		}
		
		def getIndex() {
			getWorkbook().getSheetIndex(_sheet)
		}

		class MyRow {
			@Delegate Row _row
			
			MyRow(Row row) {
				_row = row
			}
			
			MyRow getRow() {
				return this
			}
			
			void eachCell(Closure cl) {
				for(Cell c: _row) {
					cl.doCall(new MyCell(c))
				}
			}
			
			Cell cell(int index) {
				new MyCell(_row.getCell(index))
			}
			
			Cell cell(String index) {
				new MyCell(_row.getCell(index.index))
			}
			
			class MyCell {
				static final short[] _colors = [
					8,60,59,58,56,18,62,63,
					16,53,19,17,21,12,54,23,
					10,52,50,57,49,48,20,55,
					14,51,13,11,15,40,61,22,
					45,47,43,42,41,44,46,9
				]
				static _formatter = {
					def ssf = new HSSFDataFormatter()
					MyDateUtil.PTNS.each {k,v->
						if(v) {
							ssf.addFormat(k, v)
						}
					}
					ssf
				}()
				
				@Delegate Cell _cell
				
				MyCell(Cell cell) {
					_cell = cell
				}
				
				String getText() {
					if(_cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
						def value = getEvaluator().evaluate(_cell)
						if(value.getCellType() == Cell.CELL_TYPE_ERROR) {
							ErrorConstants.getText(value.getErrorValue())
						} else {
							_formatter.formatCellValue(_cell, getEvaluator())
						}
					} else {
						_formatter.formatCellValue(_cell)
					}
				}
				
				private Object evaluate() {
					def cellValue = getEvaluator().evaluate(_cell)
					def value = null
					switch (cellValue.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						value = null
						break
					case Cell.CELL_TYPE_BOOLEAN:
						value = cellValue.getBooleanValue()
						break
					case Cell.CELL_TYPE_ERROR:
						value = ErrorConstants.getText(cellValue.getErrorValue())
						break
					case Cell.CELL_TYPE_NUMERIC:
						if(isDate()) {
							value = _cell.getDateCellValue()
						} else {
							value = _cell.getNumericCellValue()
						}
						break
					case Cell.CELL_TYPE_STRING:
						value = cellValue.getStringValue()
						break
					default:
						throw new RuntimeException("UnexpedtedCellType")
					}
					return value
				}
				
				Object getValue() {
					Object value = null
					switch (_cell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						value = null
						break
					case Cell.CELL_TYPE_BOOLEAN:
						value = _cell.getBooleanCellValue()
						break
					case Cell.CELL_TYPE_ERROR:
						value = ErrorConstants.getText(_cell.getErrorCellValue())
						break
					case Cell.CELL_TYPE_FORMULA:
						value = evaluate()
						break
					case Cell.CELL_TYPE_NUMERIC:
						if(isDate()) {
							value = _cell.getDateCellValue()
						} else {
							value = _cell.getNumericCellValue()
						}
						break
					case Cell.CELL_TYPE_STRING:
						value = _cell.getStringCellValue()
						break
					default:
						throw new RuntimeException("UnexpedtedCellType")
					}
					return value
				}
				
				MyCell setValue(Object o) {
					if(o == null) {
						_cell.setCellValue("")
						_cell.setCellType(Cell.CELL_TYPE_STRING)
						return this
					}
					if(o instanceof Date || o instanceof Calendar) {
						if(!isDate()) {
							setFormat("yyyy/m/d;@")
						}
					}
					_cell.setCellValue(o)
					return this
				}
				
				MyCell setText(Object o) {
					if(o == null) {
						return setValue(null)
					}
					String value = o.toString()
					
					if(value ==~ '(?i)true|false') {
						setValue(Boolean.parseBoolean(value))
						_cell.setCellType(Cell.CELL_TYPE_BOOLEAN)
					} else if(value ==~ /\d+|\.\d+|\d+\.|\d+\.\d+/) {
						setValue(Double.parseDouble(value))
						_cell.setCellType(Cell.CELL_TYPE_NUMERIC)
					} else {
						def dtVal = null
						switch(value) {
						case ~'\\d{4}/\\d{1,2}/\\d{1,2}':
							dtVal = new SimpleDateFormat('yyyy/M/d').parse(value)
							break
						case ~'\\d{4}年\\d{1,2}月\\d{1,2}日':
							dtVal = new SimpleDateFormat('yyyy年M月d日').parse(value)
							break
						case ~'\\d{4}年\\d{1,2}月':
							dtVal = new SimpleDateFormat('yyyy年M月').parse(value)
							break
						case ~'\\d{1,2}月\\d{1,2}日':
							dtVal = new SimpleDateFormat('M月d日').parse(value)
							def cal = Calendar.getInstance()
							def year = cal.get(Calendar.YEAR)
							cal.setTime(dtVal)
							cal.set(Calendar.YEAR, year)
							dtVal = cal.getTime()
							break
						case ~'\\d{1,2}/\\d{1,2}':
							dtVal = new SimpleDateFormat('M/d').parse(value)
							def cal = Calendar.getInstance()
							def year = cal.get(Calendar.YEAR)
							cal.setTime(dtVal)
							cal.set(Calendar.YEAR, year)
							dtVal = cal.getTime()
							break
						}
						if(dtVal != null) {
							setValue(dtVal)
							_cell.setCellType(Cell.CELL_TYPE_NUMERIC)
						} else {
							setValue(value)
							_cell.setCellType(Cell.CELL_TYPE_STRING)
						}
					}
					return this
				}
				
				String getFormula() {
					if(_cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
						_cell.getCellFormula()
					} else {
						null
					}
				}
				
				MyCell setFormula(String formula) {
					_cell.setCellFormula(formula)
					return this
				}
				
				String getComment() {
					_cell.getCellComment()
				}
				
				MyCell setComment(String str) {
					def anchor = getCreationHelper().createClientAnchor()
					anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE)
					anchor.setRow1(_cell.getRowIndex())
					anchor.setCol1(_cell.getColumnIndex() + 1)
					anchor.setRow2(_cell.getRowIndex() + ((((str.getBytes().length / 20) as int) + 1) * 2))
					anchor.setCol2(_cell.getColumnIndex() + 3)
					def cmt = getSheet().createDrawingPatriarch().createCellComment(anchor)
					cmt.setString(getCreationHelper().createRichTextString(str))
					_cell.setCellComment(cmt)
					return this
				}
				
				boolean isCancel() {
					getFont().strikeout
				}
				
				MyCell setCancel(boolean flag) {
					def font = cloneFont(getFont())
					font.setStrikeout(flag)
					def style = cloneStyle()
					style.setFont(font)
					setStyle(style)
					return this
				}
				
				Font getFont() {
					getWorkbook().getFontAt(getCellStyle().getFontIndex())
				}
				
				def select() {
					_cell.setAsActiveCell()
				}
				
				MyCell update() {
					getEvaluator().evaluateFormulaCell(_cell)
					return this
				}
				
				def getRichString() {
					def text = _cell.getRichStringCellValue()
					def str = text.getString()
					def start = 0
					def sects = []
					def fonts = [getFont()]
					(0 ..< text.numFormattingRuns()).each {
						def index = text.getIndexOfFormattingRun(it)
						def font = text.getFontAtIndex(index)
						sects.add(str.substring(start, index))
						fonts.add(getWorkbook().getFontAt(font))
						start = index
					}
					sects.add(str.substring(start))
					
					def rs = []
					sects.eachWithIndex {it,i->
						rs << [value:it, font:fonts[i]]
					}
					return rs
				}
				
				def getTextNotCancel() {
					getRichString().findAll {
						!it.font.strikeout
					}.collect {
						it.value
					}.join()
				}
				
				def getStyle() {
					_cell.getCellStyle()
				}
				
				def setStyle(style) {
					_cell.setCellStyle(style)
					return this
				}
				
				def getFormat() {
					getStyle().getDataFormatString()
				}
				
				def setFormat(String format) {
					def style = getWorkbook().createCellStyle()
					style.setDataFormat(getWorkbook().createDataFormat().getFormat(format) as short)
					_cell.setCellStyle(style)
					return this
				}
				
				def getValueType() {
					def type = _cell.getCellType()
					if(type == Cell.CELL_TYPE_FORMULA) {
						def cellValue = getEvaluator().evaluate(_cell)
						def valueType = cellValue.getCellType()
						type = valueType
					}
					return type
				}
				
				def isDate() {
					if(getValueType() != Cell.CELL_TYPE_NUMERIC) {
						return false
					}
					DateUtil.isCellDateFormatted(_cell) || MyDateUtil.PTNS.containsKey(getFormat())
				}
				
				def cloneStyle() {
					def style = getWorkbook().createCellStyle()
					style.cloneStyleFrom(_cell.getCellStyle())
					return style
				}
				
				def setColor(int row, int col) {
					if(!(row >=1 && row <=5 && col >=1 && col <= 8)) {
						throw new IllegalArgumentException("row=${row},col=${col}(row >=1 && row <=5 && col >=1 && col <= 8)")
					}
					def index = _colors[(row - 1) * 8 + col - 1]
					return setColor(index)
				}
				
				def setColor(int index) {
					def style = cloneStyle()
					style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND)
					style.setFillForegroundColor(index as short)
					setCellStyle(style)
					return this
				}
			}
		}
	}
}

class POI {

	static void excel(File file, Closure cl) {
		cl(new MyWorkbook(file))
	}

	static Object map(Sheet sheet, Map map, boolean useText=true) {
		return new SheetWrapper(sheet, map, useText)
	}
	
	static Object map(Row row, Map map, boolean useText=true) {
		return new RowWrapper(row, map, useText)
	}
	
	static void rows(Sheet sheet, def from, final int maxInvalid, Closure isValid, Closure cl) {
		int start = from.index
		int cnt = 0
		for(def i = start; cnt <= maxInvalid; i++) {
			def row = sheet.row(i)
			if(isValid.doCall(row)) {
				cnt = 0
				cl.doCall(row)
			} else {
				cnt++
				continue
			}
		}
	}

	static void rows(Sheet sheet, def from, def to, Closure cl) {
		int start = from.index
		int end = to.index
		for(i in (start..end)) {
			def row = sheet.row(i)
			cl.doCall(row)
		}
	}

	static void editTable(Sheet sheet, startRow, startCol, endCol, Closure cl) {
		startRow = startRow.index
		startCol = startCol.index
		endCol = endCol.index
		
		def head = sheet.row(startRow)
		def colNames = (startCol .. endCol).inject([:]) {map,it->
			map.put(head.cell(it).text, it.col)
			map
		}
		
		def table = [row: {int index->
			def row = sheet.row(startRow + index + 1)
			row.map(colNames)
		}]
		cl.doCall(table)
	}

	static void editTable(Sheet sheet, startRow, Map colMap, Closure cl) {
		startRow = startRow.index
		
		def table = [row: {int index->
			def row = sheet.row(startRow + index)
			row.map(colMap)
		}]
		cl.doCall(table)
	}

	static void editTable(Sheet sheet, startRow, Closure rowMapper, Closure cl) {
		startRow = startRow.index
		
		def table = [row: {int index->
			def row = sheet.row(startRow + index)
			rowMapper.doCall(row)
		}]
		cl.doCall(table)
	}
}
/*
use(POI) {
	def file = new File(/c:\test.xls/)
	file.delete()
	file.excel {wb->
		def sheet = wb.createSheet()
		def cl = {row->
			row
		}
		sheet.editTable("1", cl) {table->
			(0..9).each {
				table.row(0).cell(it).value = "XX${it}"
			}
		}
		sheet.cell("A2").value = "はは"
		wb.save()
	}
	
	file.excel {wb->
		def sheet = wb.sheet(0)
		sheet.editTable("1", ["M1": "A"]) {table->
			assert table.row(0).M1 == "XX0"
		}
		sheet.editTable("1", "A", "D") {table->
			assert table.row(0).XX0 == "はは"
			table.row(0).XX1 = "ちち"
		}
		wb.save()
	}
}
*/
/*
use(POI) {
	def file = new File(/c:\test.xlsx/)
	file.delete()
	file.excel {wb->
		def sheet = wb.createSheet()
		sheet.cell("A1").value = 12.123
		sheet.cell("A2").value = true
		sheet.cell("A3").value = "あいうえお"
		sheet.cell("A4").value = new Date()
		sheet.cell("A5").formula = "A1"
		sheet.cell("A6").comment = "11111111111111111111111222222222222222222222222222222222222222222223333333333333333333333333"
		sheet.cell("A7").comment = "あああああああああああ\r\nあああああああああああああああああああああああああああああああああああ"
		println sheet.cell("A1").font
		sheet.cell("A8").cancel = true
		println sheet.cell("A8").isCancel()
		println sheet.cell("A8").font
		println sheet.cell("A1").font
		wb.save()
	}
}
*/

