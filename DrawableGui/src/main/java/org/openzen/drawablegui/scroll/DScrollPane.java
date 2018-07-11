/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DClipboard;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTimerHandle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveInt;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveInt;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.drawablegui.style.DStyleSheets;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollPane implements DComponent {
	private final DStyleClass styleClass;
	private final DComponent contents;
	private final DScrollBar scrollBar;
	private DUIContext context;
	private DIRectangle bounds;
	
	private DScrollPaneStyle style;
	private final LiveInt contentsHeight;
	private final LiveInt offsetX;
	private final LiveInt offsetY;
	
	private final ListenerHandle<LiveInt.Listener> contentsHeightListener;
	private final ListenerHandle<LiveInt.Listener> offsetXListener;
	private final ListenerHandle<LiveInt.Listener> offsetYListener;
	
	private DComponent hovering = null;
	
	public DScrollPane(DStyleClass styleClass, DComponent contents) {
		this.styleClass = styleClass;
		this.contents = contents;
		
		contentsHeight = new SimpleLiveInt();
		offsetX = new SimpleLiveInt();
		offsetY = new SimpleLiveInt();
		
		scrollBar = new DScrollBar(DStyleClass.EMPTY, contentsHeight, offsetY);
		
		contentsHeightListener = contentsHeight.addListener(new ScrollListener());
		offsetXListener = offsetX.addListener(new ScrollListener());
		offsetYListener = offsetY.addListener(new ScrollListener());
		
		contents.getDimensionPreferences().addListener((oldPreferences, newPreferences) -> {
			if (bounds == null)
				return;
			
			contents.setBounds(new DIRectangle(0, 0, bounds.width, Math.max(bounds.height, newPreferences.preferredHeight)));
			contentsHeight.setValue(newPreferences.preferredHeight);
		});
	}
	
	@Override
	public void onMounted() {
		contents.onMounted();
	}
	
	@Override
	public void onUnmounted() {
		contents.onUnmounted();
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("scrollpane", styleClass);
		contents.setContext(path, new TranslatedContext());
		scrollBar.setContext(path, context);
		style = new DScrollPaneStyle(context.getStylesheets().get(context, path));
		
		if (bounds != null)
			setBounds(bounds);
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return contents.getDimensionPreferences(); // TODO: derived preferences
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
		
		if (this.context == null)
			return;
		
		int height = Math.max(
				bounds.height - style.border.getPaddingTop() - style.border.getPaddingBottom(),
				contents.getDimensionPreferences().getValue().preferredHeight);
		int scrollBarWidth = scrollBar.getDimensionPreferences().getValue().preferredWidth;
		scrollBar.setBounds(new DIRectangle(
				bounds.x + bounds.width - scrollBarWidth - style.border.getPaddingRight(),
				bounds.y + style.border.getPaddingTop(),
				scrollBarWidth,
				bounds.height - style.border.getPaddingTop() - style.border.getPaddingBottom()));
		contents.setBounds(new DIRectangle(0, 0, bounds.width - scrollBar.getBounds().width, height));
		contentsHeight.setValue(height);
	}

	@Override
	public void paint(DCanvas canvas) {
		if (bounds == null)
			return;
		
		style.border.paint(canvas, bounds);
		scrollBar.paint(canvas);
		
		canvas.pushBounds(new DIRectangle(
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop(),
				bounds.width - style.border.getPaddingLeft() - style.border.getPaddingTop() - scrollBar.getBounds().width,
				bounds.height - style.border.getPaddingTop() - style.border.getPaddingBottom()));
		canvas.pushOffset(bounds.x - offsetX.getValue(), bounds.y - offsetY.getValue());
		contents.paint(canvas);
		canvas.popOffset();
		canvas.popBounds();
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		offsetY.setValue(offsetY.getValue() + e.deltaZ * 50);
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		if (e.x >= scrollBar.getBounds().x) {
			setHovering(scrollBar, e);
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
		if (e.x >= scrollBar.getBounds().x) {
			if (hovering != scrollBar) {
				setHovering(scrollBar, e);
			} else {
				scrollBar.onMouseMove(e);
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
				e.x - bounds.x + offsetX.getValue(),
				e.y - bounds.y + offsetY.getValue(),
				e.modifiers,
				e.deltaZ,
				e.clickCount);
	}
	
	private class TranslatedContext implements DUIContext {
		
		@Override
		public DStyleSheets getStylesheets(){
			return context.getStylesheets();
		}

		@Override
		public float getScale() {
			return context.getScale();
		}
		
		@Override
		public float getTextScale() {
			return context.getTextScale();
		}

		@Override
		public void repaint(int x, int y, int width, int height) {
			int left = x + bounds.x - offsetX.getValue();
			int top = y + bounds.y - offsetY.getValue();
			int right = left + width;
			int bottom = top + height;
			
			left = Math.max(bounds.x, left);
			top = Math.max(bounds.y, top);
			right = Math.min(bounds.x + bounds.width, right);
			bottom = Math.min(bounds.y + bounds.height, bottom);
			
			if (left >= right || top >= bottom)
				return;
			
			context.repaint(left, top, right - left, bottom - top);
		}

		@Override
		public void setCursor(Cursor cursor) {
			context.setCursor(cursor);
		}

		@Override
		public DFontMetrics getFontMetrics(DFont font) {
			return context.getFontMetrics(font);
		}

		@Override
		public void scrollInView(int x, int y, int width, int height) {
			if (y < offsetY.getValue())
				offsetY.setValue(y);
			if (y + height > offsetY.getValue() + bounds.height)
				offsetY.setValue(y + height - bounds.height);
		}

		@Override
		public DTimerHandle setTimer(int millis, Runnable target) {
			return context.setTimer(millis, target);
		}

		@Override
		public DClipboard getClipboard() {
			return context.getClipboard();
		}

		@Override
		public DUIWindow getWindow() {
			return context.getWindow();
		}

		@Override
		public DUIWindow openDialog(int x, int y, DAnchor anchor, String title, DComponent root) {
			return context.openDialog(x, y, anchor, title, root);
		}

		@Override
		public DUIWindow openView(int x, int y, DAnchor anchor, DComponent root) {
			return context.openView(x, y, anchor, root);
		}
	}
	
	private class ScrollListener implements LiveInt.Listener {

		@Override
		public void onChanged(int oldValue, int newValue) {
			int value = offsetY.getValue();
			if (value > contentsHeight.getValue() - bounds.height)
				value = contentsHeight.getValue() - bounds.height;
			if (value < 0)
				value = 0;
			
			if (value != offsetY.getValue())
				offsetY.setValue(value);
			if (context != null && bounds != null)
				context.repaint(bounds);
		}
	}
}
