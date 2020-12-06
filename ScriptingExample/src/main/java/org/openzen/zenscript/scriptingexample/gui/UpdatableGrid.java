package org.openzen.zenscript.scriptingexample.gui;

import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("example.gui.UpdatableGrid")
public class UpdatableGrid {

	private final char[][] grid;
	private SwingGrid swingGrid;

	@ZenCodeType.Constructor
	public UpdatableGrid(int rows, int columns) {
		grid = new char[rows][columns];
	}

	@ZenCodeType.Operator(ZenCodeType.OperatorType.INDEXSET)
	public void setValue(@ZenCodeType.USize int row, @ZenCodeType.USize int column, char value) {
		grid[row][column] = value;
	}

	@ZenCodeType.Method
	public void show(String name, @ZenCodeType.OptionalInt(600) int width, @ZenCodeType.OptionalInt(480) int height) {
		if (swingGrid == null) {
			swingGrid = new SwingGrid(name, grid);
		}
		swingGrid.setSize(width, height);
	}

	@ZenCodeType.Method
	public void update() {
		if (swingGrid != null) {
			swingGrid.update();
		}
	}
}
