require 'win32ole'

module Excel
  module Color
    BLACK = 1
    WHITE = 2
    RED = 3
    GREEN = 4
    BLUE = 5
    YELLOW = 6
    MAGENTA = 7
    CYAN = 8
  end

  module LineStyle
    module Direction
      TOP = 8
      BOTTOM = 9
      LEFT = 7
      RIGHT = 10
    end
    CONTINUOUS  = 1
    NONE = -4142
  end

  def Excel.row_index(coord)
    if coord.kind_of? String
      index = coord.to_i - 1
    elsif coord.kind_of? Fixnum
      index = coord
    else
      raise "Argument Type Error"
    end
    raise "Row index out of range: #{index}" unless 0 <= index and index <= 65535
    index
  end

  def Excel.col_index(col)
    if col.kind_of? String
      index = col.upcase.bytes.inject(0) do |value, ch|
        value * 26 + (ch - 65) + 1
      end - 1
    elsif col.kind_of? Fixnum
      index = col
    else
      raise "Argument Type Error"
    end
    raise "Column index out of range: #{index}" unless index >= 0 and index <= 255
    index
  end

  class Workbook
    attr_reader :path
    def Workbook.open(*path)
      excel = WIN32OLE.new('Excel.Application')
      begin
        excel.Visible = false
        excel.DisplayAlerts = false
        excel.AutomationSecurity = 3
        excel.AlertBeforeOverwriting = false
        excel.AskToUpdateLinks = false
        excel.FeatureInstall = 0
        unless path.empty?
          yield *path.map {|it| Workbook.new excel, it}
        else
          yield Workbook.new(excel)
        end
      ensure
        excel.Quit
      end
    end

    private

    def initialize(excel, path=nil)
      @excel = excel
      if path.nil?
        @workbook = excel.Workbooks.Add()
        @path = File.expand_path @workbook.Name
      else
        @workbook = excel.Workbooks.Add(path)
        @path = File.expand_path path
      end
      @sheets = @workbook.Sheets
    end

    public

    def each_sheet()
      @workbook.Worksheets.each do |sheet|
        yield  Sheet.new(self, sheet)
      end
    end

    def method_missing(name, *args, &block)
      @workbook.send name, *args, &block
    end

    def sheet_num
      @sheets.Count
    end

    def sheet(index)
      if index.kind_of? Fixnum
        if index < 0 or index >= sheet_num
          raise "Index Out of bound"
        end
        Sheet.new self, @workbook.Worksheets(index + 1)
      elsif index.kind_of? String
        Sheet.new self, @workbook.Worksheets(index)
      else
        raise "Wrong Argument Type #{index.class}"
      end
    end

    def insert(name=nil, index=0)
      if index < sheet_num
        sheet(index).select
        sheet = Sheet.new self, @sheets.Add
      elsif index == sheet_num
        sheet = Sheet.new self, @sheets.Add
        sheet.after sheet(sheet_num)
      else
        raise "Index Outof bound"
      end
      sheet.name = name unless name.nil?
      sheet
    end

    def save
      save_as(@path)
    end

    def close
      @closed ||= @workbook.close
    end

    def save_as(path)
      @workbook.SaveAs path
      @path = path
    end
  end

  class Sheet
    attr_reader :sheet
    protected   :sheet
    def initialize(workbook, sheet)
      @workbook = workbook
      @sheet = sheet
    end

    def method_missing(name, *args, &block)
      @sheet.send name, *args, &block
    end

    def name
      @sheet.Name
    end

    def name=(name)
      @sheet.Name = name
    end

    def to_s
      @sheet.Name
    end

    def select
      @workbook.activate
      @sheet.select
      self
    end

    def after(other)
      self.before other
      other.before self
      self.select
    end

    def before(other)
      self.select
      @sheet.move other.sheet
    end

    def cell(coord)
      mch = /([A-Za-z]+)(\d+)/.match(coord)
      raise "Invalid coord. #{coord}" if mch.nil?
      col_index, row_index = mch[1, 2]
      row(row_index).cell(col_index)
    end

    def row(coord)
      Row.new self, @sheet.rows[Excel::row_index(coord) + 1]
    end

    def each_row(range)
      range.each do |it|
        yield row(it)
      end
    end

    def paste
      select
      @sheet.paste
      self
    end

    def delete
      select
      @sheet.delete
    end
  end

  class Row
    def initialize(sheet, row)
      @sheet = sheet
      @row = row
    end

    def cell(coord)
      Cell.new @row.cells[Excel::col_index(coord) + 1]
    end

    def each_cell(range)
      range.each do |coord|
        yield cell(coord)
      end
    end

    def index
      @row.row - 1
    end

    def select
      @sheet.select
      @row.select
      self
    end

    def copy
      select
      @row.copy
      self
    end

    def paste
      select
      @sheet.paste
      self
    end

    def insert
      @row.insert
    end

    def hide
      @row.entirerow.hidden = true
      self
    end

    def hidden?
      @row.entirerow.hidden
    end

    def color=(color)
      @row.interior.colorindex = color
    end
  end

  class Cell
    def initialize(cell)
      @cell = cell
    end

    def formula=(value)
      @cell.formula = value
    end

    def format=(value)
      @cell.numberformatlocal = value
    end

    def value
      @cell.value
    end

    def value=(value)
      @cell.value = value
    end

    def text
      @cell.text
    end

    def border?(direction)
      style = @cell.mergearea.borders(direction).linestyle
      style != Excel::LineStyle::NONE
    end
  end
end
