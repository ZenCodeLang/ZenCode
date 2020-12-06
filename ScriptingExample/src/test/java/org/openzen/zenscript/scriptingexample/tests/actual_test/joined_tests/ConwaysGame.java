package org.openzen.zenscript.scriptingexample.tests.actual_test.joined_tests;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class ConwaysGame extends ZenCodeTest {

	private static final String expandUsizeZs;
	private static final String cellZs;
	private static final String conwayGridZs;
	private static final String gridTestZs;
	private static final int[] aliveCellsAtGeneration;

	static {
		aliveCellsAtGeneration = new int[]{4, 7, 6, 6, 8, 8, 12, 12, 20, 12, 12, 12};

		expandUsizeZs = "public expand usize .. usize {\n" + "    public withBounds(bounds as usize .. usize) as usize .. usize {\n" + "        return (from < bounds.from ? bounds.from : from) .. (to > bounds.to ? bounds.to : to);\n" + "    }\n" + "}";
		gridTestZs = "var height = 25;\n" + "var width = 25;\n" + "\n" + "var conwayGrid = new ConwayGrid(height, width);\n" + "\n" + "conwayGrid[4,5] = true;\n" + "conwayGrid[5,4] = true;\n" + "conwayGrid[5,5] = true;\n" + "conwayGrid[5,6] = true;\n" + "\n" + "\n" + "\n" + "for i in 0 .. " + aliveCellsAtGeneration.length + " {\n" + "    println(\"Generation \" + i);\n" + "    println(\"Number of alive cells: \" + conwayGrid.totalNumberOfAliveCells);" + "    conwayGrid.update();\n" + "}";
		cellZs = "public class Cell {\n" + "\n" + "    public var alive as bool;\n" + "    private var row as usize;\n" + "    private var column as usize;\n" + "\n" + "    public this(row as usize, column as usize) {\n" + "        this(row, column, false);\n" + "    }\n" + "\n" + "    public this(row as usize, column as usize, alive as bool) {\n" + "            this.row = row;\n" + "            this.column = column;\n" + "            this.alive = alive;\n" + "    }\n" + "\n" + "    public getCellNextTick(currentGrid as ConwayGrid) as Cell {\n" + "        //+2 because upperbound exclusive\n" + "        var range = currentGrid[(row - 1) .. (row + 2), (column - 1) .. (column + 2)];\n" + "        var aliveCellsIn3x3Grid = range.filter(element => element.alive).length;\n" + "\n" + "\n" + "        if(!alive) {\n" + "            return new Cell(row, column, aliveCellsIn3x3Grid == 3);\n" + "        }\n" + "\n" + "        //2. Any live cell with two or three live neighbours lives on to the next generation.\n" + "        return new Cell(row, column, aliveCellsIn3x3Grid == 3 || aliveCellsIn3x3Grid == 4);\n" + "    }\n" + "}";
		conwayGridZs = "public class ConwayGrid {\n" + "    public var cells as Cell[,];\n" + "    private var width as usize;\n" + "    private var height as usize;\n" + "\n" + "    public this(width as usize, height as usize) {\n" + "        this.cells = new Cell[,](width, height, (row, column) => new Cell(row, column));\n" + "        this.width = width;\n" + "        this.height = height;\n" + "    }\n" + "\n" + "    public []=(row as int, column as int, alive as bool) as void {\n" + "        this.cells[row,column].alive = alive;\n" + "    }\n" + "\n" + "    public update() as void {\n" + "        var gridCapture = this;\n" + "        this.cells = new Cell[,](width, height, (row, column) => gridCapture.cells[row, column].getCellNextTick(gridCapture));\n" + "    }\n" + "\n" + "    //public [](row as usize, column as usize) as Cell => cells[row,column];\n" + "\n" + "    public [](rows as usize .. usize, columns as usize .. usize) as Cell[]{\n" + "        var usedRows = rows.withBounds(0 .. height);\n" + "        var usedColumns = columns.withBounds(0 .. width);\n" + "\n" + "        var rowSpan = usedRows.to - usedRows.from;\n" + "        var columnSpan = usedColumns.to - usedColumns.from;\n" + "        var cells = this.cells;\n" + "        return new Cell[](rowSpan * columnSpan, cellNo => cells[usedRows.from + (cellNo / columnSpan), usedColumns.from + (cellNo % columnSpan)]);\n" + "    }\n" + "\n" + "    public get totalNumberOfAliveCells as usize => this[0 .. height + 1, 0 .. width + 1].filter(element => element.alive).length;\n" + "}";
	}

	@Test
	public void doTheTest() {
		ScriptBuilder.create()
				.add(expandUsizeZs)
				.startNewScript()
				.add(cellZs)
				.startNewScript()
				.add(conwayGridZs)
				.startNewScript()
				.add(gridTestZs)
				.execute(this);

		logger.assertPrintOutputSize(aliveCellsAtGeneration.length * 2);
		for (int generation = 0; generation < aliveCellsAtGeneration.length; generation++) {
			logger.assertPrintOutput(generation * 2, "Generation " + generation);
			logger.assertPrintOutput(generation * 2 + 1, "Number of alive cells: " + aliveCellsAtGeneration[generation]);
		}
	}
}
