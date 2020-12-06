package org.openzen.zenscript.scriptingexample.gui;

import javax.swing.*;
import java.awt.*;

public class SwingGrid extends JFrame {

	private final char[][] grid;
	private final JLabel[][] labelGrid;

	public SwingGrid(String title, char[][] grid) throws HeadlessException {
		super(title);
		this.grid = grid;
		this.setLayout(new GridLayout(grid.length, 0));

		labelGrid = new JLabel[grid.length][grid[0].length];
		for (int i = 0; i < grid.length; i++) {
			char[] chars = grid[i];
			for (int j = 0; j < chars.length; j++) {
				this.add(labelGrid[i][j] = new JLabel(String.valueOf(chars[j]), SwingConstants.CENTER));
			}
		}

		this.setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		EventQueue.invokeLater(() -> {
			this.setVisible(true);
		});
	}


	public void update() {
		for (int i = 0; i < grid.length; i++) {
			char[] chars = grid[i];
			for (int j = 0; j < chars.length; j++) {
				labelGrid[i][j].setText(String.valueOf(chars[j]));
			}
		}
	}
}
