import org.apache.poi.ss.usermodel.*

public class POICategory {

	public static Object getValue(Cell cell) {
		int type = cell.getCellType();
		Object result = null;
		switch (type) {
		case Cell.CELL_TYPE_BLANK:
			result = null;
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			result = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_ERROR:
			result = ErrorConstants.getText(cell.getErrorCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			throw new RuntimeException("formula cell type is not supported");
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				result = cell.getDateCellValue();
			} else {
				result = cell.getNumericCellValue();
			}
			break;
		case Cell.CELL_TYPE_STRING:
			result = cell.getStringCellValue();
			break;
		default:
			throw new AssertionError();
		}
		return result;
	}

	public static Workbook openwb(File excel) {
		Workbook wb = WorkbookFactory.create(excel);
		return wb;
	}
}

