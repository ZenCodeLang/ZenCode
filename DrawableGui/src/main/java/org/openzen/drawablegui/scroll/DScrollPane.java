/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import java.util.function.BiConsumer;
import listeners.ListenerHandle;
import live.LiveInt;
import live.LiveObject;
import live.SimpleLiveInt;
import live.SimpleLiveObject;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DScalableSize;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DSubSurface;
import org.openzen.drawablegui.style.DStyleClass;
import zsynthetic.FunctionIntIntToVoid;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollPane implements DComponent, DScrollContext {
	private final DStyleClass styleClass;
	private final DComponent contents;
	private final DScrollBar verticalScrollBar;
	private final DScrollBar horizontalScrollBar;
	
	private DComponentContext context;
	private DScrollPaneStyle style;
	private DIRectangle bounds;
	
	private DDrawnShape shape;
	private final LiveInt contentsWidth;
	private final LiveInt contentsHeight;
	private final LiveInt offsetX;
	private final LiveInt offsetY;
	
	private final SimpleLiveObject<DSizing> sizing = new SimpleLiveObject<>(DSizing.EMPTY);
	private final LiveObject<DScalableSize> size;
	
	private final ListenerHandle<FunctionIntIntToVoid> contentsWidthListener;
	private final ListenerHandle<FunctionIntIntToVoid> contentsHeightListener;
	private final ListenerHandle<FunctionIntIntToVoid> offsetXListener;
	private final ListenerHandle<FunctionIntIntToVoid> offsetYListener;
	private final ListenerHandle<BiConsumer<DSizing, DSizing>> contentsSizingListener;
	
	private boolean isHorizontalScrollbarVisible = true;
	private boolean isVerticalScrollbarVisible = true;
	
	private DComponent hovering = null;
	
	private DSubSurface subSurface;
	
	public DScrollPane(DStyleClass styleClass, DComponent contents, LiveObject<DScalableSize> size) {
		this.size = size;
		this.styleClass = styleClass;
		this.contents = contents;
		
		contentsWidth = new SimpleLiveInt(0);
		contentsHeight = new SimpleLiveInt(0);
		offsetX = new SimpleLiveInt(0);
		offsetY = new SimpleLiveInt(0);
		
		verticalScrollBar = new DScrollBar(DStyleClass.EMPTY, contentsHeight, offsetY, false);
		horizontalScrollBar = new DScrollBar(DStyleClass.EMPTY, contentsWidth, offsetX, true);
		
		contentsWidthListener = contentsWidth.addListener(new ScrollListener());
		contentsHeightListener = contentsHeight.addListener(new ScrollListener());
		offsetXListener = offsetX.addListener(new ScrollListener());
		offsetYListener = offsetY.addListener(new ScrollListener());
		
		contentsSizingListener = contents.getSizing().addListener((oldPreferences, newPreferences) -> {
			if (bounds == null)
				return;
			
			contents.setBounds(new DIRectangle(0, 0, bounds.width, Math.max(bounds.height, newPreferences.preferredHeight)));
			contentsHeight.setValue(newPreferences.preferredHeight);
			contentsWidth.setValue(newPreferences.preferredWidth);
		});
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("scrollpane", styleClass);
		style = context.getStyle(DScrollPaneStyle::new);
		
		subSurface = context.createSubSurface(1);
		DComponentContext newContext = new DComponentContext(this, context.path, 0, subSurface);
		contents.mount(newContext);
		horizontalScrollBar.mount(context);
		verticalScrollBar.mount(context);
		
		sizing.setValue(new DSizing(
				size.getValue().width.evalInt(parent.getUIContext()),
				size.getValue().height.evalInt(parent.getUIContext())));
	}
	
	@Override
	public void unmount() {
		context = null;
		
		subSurface.close();
		style.border.close();
		
		contents.unmount();
		horizontalScrollBar.unmount();
		verticalScrollBar.unmount();
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
		
		if (shape != null)
			shape.close();
		shape = context.shadowPath(0, style.shape.instance(style.margin.apply(bounds)), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		style.border.update(context, style.margin.apply(bounds));
		
		int scrollBarWidth = verticalScrollBar.getSizing().getValue().preferredWidth;
		int scrollBarHeight = horizontalScrollBar.getSizing().getValue().preferredHeight;
		int scrollBarMarginX = isHorizontalScrollbarVisible ? scrollBarWidth : 0;
		int scrollBarMarginY = isVerticalScrollbarVisible ? scrollBarHeight : 0;
		
		int width = Math.max(
				bounds.width - style.border.getPaddingHorizontal() - scrollBarMarginX,
				contents.getSizing().getValue().preferredWidth);
		int height = Math.max(
				bounds.height - style.border.getPaddingHorizontal() - scrollBarMarginY,
				contents.getSizing().getValue().preferredHeight);
		
		verticalScrollBar.setBounds(new DIRectangle(
				bounds.x + bounds.width - scrollBarWidth - style.border.getPaddingRight() - style.margin.right,
				bounds.y + style.border.getPaddingTop() + style.margin.top,
				scrollBarWidth,
				bounds.height - style.border.getPaddingVertical() - style.margin.getVertical() - scrollBarMarginY));
		
		horizontalScrollBar.setBounds(new DIRectangle(
				bounds.x + style.border.getPaddingLeft() + style.margin.left,
				bounds.y + bounds.height - scrollBarHeight - style.border.getPaddingBottom() - style.margin.bottom,
				bounds.width - style.border.getPaddingHorizontal() - style.margin.getHorizontal() - scrollBarMarginX,
				scrollBarHeight));
		
		contents.setBounds(new DIRectangle(0, 0, width, height));
		contentsWidth.setValue(width);
		contentsHeight.setValue(height);
		
		subSurface.setOffset(bounds.x - offsetX.getValue(), bounds.y - offsetY.getValue());
		subSurface.setClip(new DIRectangle(
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop(),
				bounds.width - style.margin.getHorizontal() - style.border.getPaddingHorizontal(),
				bounds.height - style.margin.getVertical() - style.border.getPaddingVertical()));
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		if (e.has(DMouseEvent.SHIFT))
			offsetX.setValue(offsetX.getValue() + e.deltaZ * 50);
		else
			offsetY.setValue(offsetY.getValue() + e.deltaZ * 50);
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		if (e.x >= verticalScrollBar.getBounds().x) {
			setHovering(verticalScrollBar, e);
		} else if (e.y >= horizontalScrollBar.getBounds().y) {
			setHovering(horizontalScrollBar, e);
		} else {
			setHovering(contents, e);
		}
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		setHovering(null, e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		if (e.x >= verticalScrollBar.getBounds().x) {
			if (hovering != verticalScrollBar) {
				setHovering(verticalScrollBar, e);
			} else {
				verticalScrollBar.onMouseMove(e);
			}
		} else if (e.y >= horizontalScrollBar.getBounds().y) {
			if (hovering != horizontalScrollBar) {
				setHovering(horizontalScrollBar, e);
			} else {
				horizontalScrollBar.onMouseMove(e);
			}
		} else {
			if (hovering != contents) {
				setHovering(contents, e);
			} else {
				contents.onMouseMove(translateMouseEvent(e));
			}
		}
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDrag(forward(hovering, e));
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseClick(forward(hovering, e));
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDown(forward(hovering, e));
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseRelease(forward(hovering, e));
		
		onMouseMove(e);
	}

	@Override
	public void close() {
		contents.close();
		unmount();
		
		contentsSizingListener.close();
		contentsHeightListener.close();
		offsetXListener.close();
		offsetYListener.close();
	}
	
	private DMouseEvent forward(DComponent target, DMouseEvent e) {
		return target == contents ? translateMouseEvent(e) : e;
	}
	
	private void setHovering(DComponent target, DMouseEvent e) {
		if (target == hovering)
			return;
		
		if (hovering != null)
			hovering.onMouseExit(forward(hovering, e));
		this.hovering = target;
		if (hovering != null)
			hovering.onMouseEnter(forward(hovering, e));
	}
	
	private DMouseEvent translateMouseEvent(DMouseEvent e) {
		return new DMouseEvent(
				e.window,
				toLocalX(e.x),
				toLocalY(e.y),
				e.modifiers,
				e.deltaZ,
				e.clickCount);
	}

	private int toGlobalX(int x) {
		return x + bounds.x - offsetX.getValue();
	}

	private int toGlobalY(int y) {
		return y + bounds.y - offsetY.getValue();
	}

	private int toLocalX(int x) {
		return x - bounds.x + offsetX.getValue();
	}

	private int toLocalY(int y) {
		return y - bounds.y + offsetY.getValue();
	}

	@Override
	public void scrollInView(int x, int y, int width, int height) {
		if (x < offsetX.getValue())
			offsetX.setValue(x);
		if (x + width > offsetX.getValue() + bounds.width)
			offsetX.setValue(x + width - bounds.width);
		
		if (y < offsetY.getValue())
			offsetY.setValue(y);
		if (y + height > offsetY.getValue() + bounds.height)
			offsetY.setValue(y + height - bounds.height);
	}

	@Override
	public int getViewportWidth() {
		return bounds.width;
	}

	@Override
	public int getViewportHeight() {
		return bounds.height;
	}
	
	private class ScrollListener implements FunctionIntIntToVoid {

		@Override
		public void invoke(int oldValue, int newValue) {
			int scrollBarWidth = verticalScrollBar.getSizing().getValue().preferredWidth;
			int scrollBarHeight = horizontalScrollBar.getSizing().getValue().preferredHeight;
			int scrollBarMarginX = isHorizontalScrollbarVisible ? scrollBarWidth : 0;
			int scrollBarMarginY = isVerticalScrollbarVisible ? scrollBarHeight : 0;
			
			int valueX = offsetX.getValue();
			if (valueX > contentsWidth.getValue() - (bounds.width - scrollBarMarginX))
				valueX = contentsWidth.getValue() - (bounds.width - scrollBarMarginX);
			if (valueX < 0)
				valueX = 0;
			
			if (valueX != offsetX.getValue())
				offsetX.setValue(valueX);
			
			int valueY = offsetY.getValue();
			if (valueY > contentsHeight.getValue() - (bounds.height - scrollBarMarginY))
				valueY = contentsHeight.getValue() - (bounds.height - scrollBarMarginY);
			if (valueY < 0)
				valueY = 0;
			
			if (valueY != offsetY.getValue())
				offsetY.setValue(valueY);
			
			subSurface.setOffset(bounds.x - offsetX.getValue(), bounds.y - offsetY.getValue());
		}
	}
}
