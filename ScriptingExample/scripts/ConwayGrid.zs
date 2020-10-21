import example.gui.UpdatableGrid;

public class ConwayGrid {
    public var cells as Cell[,];
    private var width as usize;
    private var height as usize;

    public this(width as usize, height as usize) {
        this.cells = new Cell[,](width, height, (row, column) => new Cell(row, column));
        this.width = width;
        this.height = height;
    }

    public []=(row as int, column as int, alive as bool) as void {
        this.cells[row,column].alive = alive;
    }

    public update() as void {
        var gridCapture = this;
        this.cells = new Cell[,](width, height, (row, column) => gridCapture.cells[row, column].getCellNextTick(gridCapture));
    }

    //public [](row as usize, column as usize) as Cell => cells[row,column];

    public [](rows as usize .. usize, columns as usize .. usize) as Cell[]{
        var usedRows = rows.withBounds(0 .. height);
        var usedColumns = columns.withBounds(0 .. width);

        var rowSpan = usedRows.to - usedRows.from;
        var columnSpan = usedColumns.to - usedColumns.from;
        var cells = this.cells;
        return new Cell[](rowSpan * columnSpan, cellNo => cells[usedRows.from + (cellNo / columnSpan), usedColumns.from + (cellNo % columnSpan)]);
    }

    public updateDisplay(display as UpdatableGrid) as void {
        for row in 0 .. height {
            for column in 0 .. width {
                display[row, column] = (cells[row,column].alive ? 'X' : ' ') as char;
            }
        }
    }
}