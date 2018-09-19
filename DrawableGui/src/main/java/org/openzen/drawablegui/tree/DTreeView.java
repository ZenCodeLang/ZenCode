/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import java.util.ArrayList;
import java.util.List;
import org.openzen.drawablegui.DColorableIconInstance;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DDrawableInstance;
import org.openzen.drawablegui.Destructible;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DTreeView<N extends DTreeNode<N>> implements DComponent {
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DComponentContext context;
	private DIRectangle bounds = DIRectangle.EMPTY;
	private float iconScale;
	
	private int selectedRow = -1;
	private N selectedNode = null;
	
	private DTreeViewStyle style;
	private DFontMetrics fontMetrics;
	
	private final DDrawable nodeOpenedIcon;
	private final DDrawable nodeClosedIcon;
	
	private final N root;
	private final boolean showRoot;
	private final List<Row> rows = new ArrayList<>();
	private boolean selectedIsPresent = false;
	
	private DDrawnRectangle background;
	private DDrawnRectangle selectedBackground;
	
	public DTreeView(
			DStyleClass styleClass,
			DDrawable nodeOpenedIcon,
			DDrawable nodeClosedIcon,
			N root,
			boolean showRoot)
	{
		this.styleClass = styleClass;
		this.root = root;
		this.showRoot = showRoot;
		
		this.nodeOpenedIcon = nodeOpenedIcon;
		this.nodeClosedIcon = nodeClosedIcon;
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("tree", styleClass);
		style = context.getStyle(DTreeViewStyle::new);
		fontMetrics = context.getFontMetrics(style.font);
		iconScale = context.getUIContext().getScale() / 1.75f;
		
		background = context.fillRect(0, DIRectangle.EMPTY, style.backgroundColor);
		selectedBackground = context.fillRect(1, DIRectangle.EMPTY, 0);
		
		updateLayout();
	}
	
	@Override
	public void unmount() {
		background.close();
		background = null;
		selectedBackground.close();
		selectedBackground = null;
		
		for (Row row : rows)
			row.close();
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		background.setRectangle(bounds);
		
		for (Row row : rows)
			row.setBounds(bounds);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		int rowIndex = yToRow(e.y);
		if (rowIndex >= 0 && rowIndex < rows.size()) {
			Row rowEntry = rows.get(rowIndex);
			if (e.x >= rowEntry.x && e.x < (rowEntry.x + nodeOpenedIcon.getNominalWidth())) {
				if (!rowEntry.node.isLeaf())
					rowEntry.node.isCollapsed().toggle();
				return;
			}
			
			rowEntry.node.onMouseClick(e);
			
			if (e.isSingleClick() && rowIndex != selectedRow) {
				int oldRowIndex = selectedRow;
				
				selectedRow = rowIndex;
				Row row = rows.get(rowIndex);
				selectedNode = row.node;
				
				int selectionX = bounds.x + style.padding + row.x - style.selectedPaddingLeft;
				int selectionY = bounds.y + style.padding + rowIndex * (style.rowSpacing + fontMetrics.getAscent() + fontMetrics.getDescent()) - style.selectedPaddingTop;
				int selectionWidth = (int)(fontMetrics.getWidth(row.node.getTitle())
						+ style.iconTextSpacing
						+ row.icon.getNominalWidth()
						+ style.iconTextSpacing
						+ row.node.getIcon().getNominalWidth()
						+ style.selectedPaddingLeft
						+ style.selectedPaddingRight);
				int selectionHeight = fontMetrics.getAscent() + fontMetrics.getDescent() + style.selectedPaddingTop + style.selectedPaddingBottom;
				
				if (row.node.isLeaf()) {
					int delta = (int)(row.icon.getNominalWidth() + style.iconTextSpacing);
					selectionX += delta;
					selectionWidth -= delta;
				}
				
				selectedBackground.setRectangle(new DIRectangle(
						selectionX,
						selectionY,
						selectionWidth,
						selectionHeight));
				selectedBackground.setColor(style.selectedBackgroundColor);
				row.text.setColor(style.selectedNodeTextColor);
				row.nodeIcon.setColor(style.selectedNodeTextColor);
				
				if (oldRowIndex >= 0) {
					Row oldRow = rows.get(oldRowIndex);
					oldRow.text.setColor(style.nodeTextColor);
					oldRow.nodeIcon.setColor(style.nodeTextColor);
				}
			}
		}
	}
	
	private int rowToY(int row) {
		return row * (style.rowSpacing + fontMetrics.getAscent() + fontMetrics.getDescent()) + style.padding;
	}
	
	private int yToRow(int y) {
		return (y - bounds.y - style.padding) / (style.rowSpacing + fontMetrics.getAscent() + fontMetrics.getDescent());
	}
	
	private void updateLayout() {
		int oldRowCount = rows.size();
		
		for (Row row : rows)
			row.close();
		
		rows.clear();
		selectedIsPresent = false;
		
		if (showRoot) {
			updateLayout(root, 0);
		} else {
			for (N child : root.getChildren()) {
				updateLayout(child, 0);
			}
		}
		
		if (!selectedIsPresent) {
			selectedNode = null;
			selectedRow = -1;
			selectedBackground.setColor(0);
		}
		
		if (rows.size() != oldRowCount) {
			DSizing preferences = sizing.getValue();
			sizing.setValue(new DSizing(
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
		rows.add(new Row(x, rows.size(), node));
		
		if (!node.isCollapsed().getValue()) {
			for (N child : node.getChildren()) {
				updateLayout(child, x + style.indent);
			}
		}
	}

	@Override
	public void close() {
		// nothing to clean up
	}
	
	private class Row implements Destructible, LiveBool.Listener, LiveList.Listener<N> {
		private final int x;
		private final int index;
		private final N node;
		private final DDrawable icon;
		private final ListenerHandle<LiveBool.Listener> collapseListener;
		private final ListenerHandle<LiveList.Listener<N>> childListener;
		
		private DDrawnText text;
		private DDrawableInstance collapseIcon = null;
		private DColorableIconInstance nodeIcon;
		
		public Row(int x, int index, N node) {
			this.x = x;
			this.index = index;
			this.node = node;
			this.collapseListener = node.isCollapsed().addListener(this);
			this.childListener = node.getChildren().addListener(this);
			
			if (node == selectedNode)
				selectedIsPresent = true;
			
			int baseX = bounds.x + style.padding + x;
			int baseY = (int)(bounds.y + style.padding + index * (fontMetrics.getAscent() + fontMetrics.getDescent() + style.rowSpacing));
			
			icon = node.isCollapsed().getValue() ? nodeClosedIcon : nodeOpenedIcon;
			nodeIcon = new DColorableIconInstance(
					context.surface,
					context.z + 2,
					node.getIcon(),
					DTransform2D.scaleAndTranslate(baseX + icon.getNominalWidth() + style.iconTextSpacing, baseY + fontMetrics.getAscent() + fontMetrics.getDescent() - icon.getNominalHeight(), iconScale),
					node == selectedNode ? style.selectedNodeTextColor : style.nodeTextColor);
			
			if (!node.isLeaf())
				collapseIcon = new DDrawableInstance(
						context.surface, 
						context.z + 2,
						icon,
						DTransform2D.scaleAndTranslate(baseX, baseY + fontMetrics.getAscent() + fontMetrics.getDescent() - icon.getNominalHeight(), iconScale));
			
			text = context.drawText(
					2,
					style.font,
					node == selectedNode ? style.selectedNodeTextColor : style.nodeTextColor,
					baseX + style.iconTextSpacing + icon.getNominalWidth() + style.iconTextSpacing + node.getIcon().getNominalWidth(),
					baseY + fontMetrics.getAscent(),
					node.getTitle());
		}
		
		public void setBounds(DIRectangle bounds) {
			int baseX = bounds.x + style.padding + x;
			int baseY = (int)(bounds.y + style.padding + index * (fontMetrics.getAscent() + fontMetrics.getDescent() + style.rowSpacing));
			
			if (collapseIcon != null) {
				collapseIcon.setTransform(DTransform2D.translate(
						baseX,
						baseY + fontMetrics.getAscent() + fontMetrics.getDescent() - icon.getNominalHeight()));
			}
			nodeIcon.setTransform(DTransform2D.translate(
					baseX + icon.getNominalWidth() + style.iconTextSpacing,
					baseY + fontMetrics.getAscent() + fontMetrics.getDescent() - icon.getNominalHeight()));
			text.setPosition(
					baseX + style.iconTextSpacing + icon.getNominalWidth() + style.iconTextSpacing + node.getIcon().getNominalWidth(),
					baseY + fontMetrics.getAscent());
		}

		@Override
		public void onChanged(boolean oldValue, boolean newValue) {
			updateLayout();
		}

		@Override
		public void onInserted(int index, N value) {
			updateLayout();
		}

		@Override
		public void onChanged(int index, N oldValue, N newValue) {
			updateLayout();
		}

		@Override
		public void onRemoved(int index, N oldValue) {
			updateLayout();
		}
		
		@Override
		public void close() {
			collapseListener.close();
			childListener.close();
			
			text.close();
			nodeIcon.close();
			
			if (collapseIcon != null)
				collapseIcon.close();
		}
	}
}
