/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import org.openzen.drawablegui.DRectangle;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DDrawingContext;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public class DTreeView<N extends DTreeNode<N>> implements DComponent {
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private DRectangle bounds;
	
	private int selectedRow = -1;
	private N selectedNode = null;
	
	private final DTreeViewStyle style;
	
	private DDrawingContext context;
	private final N root;
	private final boolean showRoot;
	private final List<Row> rows = new ArrayList<>();
	
	public DTreeView(DTreeViewStyle style, N root, boolean showRoot) {
		this.style = style;
		
		this.root = root;
		this.showRoot = showRoot;
	}

	@Override
	public void setContext(DDrawingContext context) {
		this.context = context;
		updateLayout();
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}
	
	@Override
	public DRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		
		int drawX = bounds.x + style.padding;
		int drawY = bounds.y + style.padding;
		if (showRoot) {
			paintNode(canvas, root, drawX, drawY);
		} else {
			for (N child : root.getChildren()) {
				drawY = paintNode(canvas, child, drawX, drawY);
				drawY += style.rowSpacing;
			}
		}
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		int row = yToRow(e.y);
		if (row >= 0 && row < rows.size()) {
			Row rowEntry = rows.get(row);
			if (e.x >= rowEntry.x && e.x < (rowEntry.x + style.nodeOpenedIcon.getNominalWidth())) {
				if (!rowEntry.node.isLeaf())
					rowEntry.node.isCollapsed().toggle();
				return;
			}
			
			rowEntry.node.onMouseClick(e);
			
			if (e.isSingleClick()) {
				int oldRow = selectedRow;
				selectedRow = row;
				selectedNode = rows.get(row).node;

				if (oldRow >= 0)
					context.repaint(
							bounds.x,
							rowToY(oldRow) - style.selectedPaddingTop,
							bounds.width,
							style.font.size + style.selectedPaddingTop + style.selectedPaddingBottom);
				context.repaint(
						bounds.x,
						rowToY(row) - style.selectedPaddingTop,
						bounds.width,
						style.font.size + style.selectedPaddingTop + style.selectedPaddingBottom);
			}
		}
	}
	
	private int rowToY(int row) {
		return row * (style.rowSpacing + style.font.size) + style.padding;
	}
	
	private int yToRow(int y) {
		return (y - bounds.y - style.padding) / (style.rowSpacing + style.font.size);
	}
	
	private int paintNode(DCanvas canvas, N node, int drawX, int drawY) {
		int textColor = style.nodeTextColor;
		if (node == selectedNode) {
			textColor = style.selectedNodeTextColor;
			canvas.fillRectangle(bounds.x, drawY - style.selectedPaddingTop, bounds.width, style.font.size + style.selectedPaddingTop + style.selectedPaddingBottom, style.selectedBackgroundColor);
		}
		
		int drawingX = drawX;
		if (!node.isLeaf()) {
			DDrawable icon = node.isCollapsed().getValue() ? style.nodeClosedIcon : style.nodeOpenedIcon;
			icon.draw(canvas, DTransform2D.translate(drawingX, drawY + style.font.size - icon.getNominalHeight()));
			drawingX += style.iconTextSpacing + icon.getNominalWidth();
		} else {
			drawingX += style.iconTextSpacing + style.nodeClosedIcon.getNominalWidth();
		}
		node.getIcon().draw(canvas, DTransform2D.translate(drawingX, drawY + style.font.size - node.getIcon().getNominalHeight()), textColor);
		drawingX += style.iconTextSpacing + node.getIcon().getNominalWidth();
		canvas.drawText(style.font, textColor, drawingX, drawY + style.font.size + style.textShift, node.getTitle());
		drawY += style.font.size;
		
		if (!node.isCollapsed().getValue()) {
			for (N child : node.getChildren()) {
				drawY += style.rowSpacing;
				drawY = paintNode(canvas, child, drawX + style.indent, drawY);
			}
		}
		
		return drawY;
	}
	
	private void updateLayout() {
		int oldRowCount = rows.size();
		
		for (Row row : rows)
			row.close();
		
		rows.clear();
		
		if (showRoot) {
			updateLayout(root, 0);
		} else {
			for (N child : root.getChildren()) {
				updateLayout(child, 0);
			}
		}
		
		if (rows.size() != oldRowCount) {
			DDimensionPreferences preferences = dimensionPreferences.getValue();
			dimensionPreferences.setValue(
					new DDimensionPreferences(
							preferences.minimumWidth,
							preferences.minimumHeight,
							preferences.preferredWidth,
							rowToY(rows.size()),
							preferences.maximumWidth,
							1000000));
		}
		
		System.out.println("Rows after updateLayout: " + rows.size());
		System.out.println("Height: " + rowToY(rows.size()));
	}
	
	private void updateLayout(N node, int x) {
		rows.add(new Row(x, node));
		
		if (!node.isCollapsed().getValue()) {
			for (N child : node.getChildren()) {
				updateLayout(child, x + style.indent);
			}
		}
	}
	
	private class Row implements Closeable, LiveBool.Listener {
		private final int x;
		private final N node;
		private final ListenerHandle<LiveBool.Listener> collapseListener;
		
		public Row(int x, N node) {
			this.x = x;
			this.node = node;
			this.collapseListener = node.isCollapsed().addListener(this);
		}

		@Override
		public void onChanged(boolean oldValue, boolean newValue) {
			updateLayout();
			context.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		
		@Override
		public void close() {
			collapseListener.close();
		}
	}
}
