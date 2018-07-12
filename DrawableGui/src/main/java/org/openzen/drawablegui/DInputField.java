/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveString;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DDimension;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DInputField implements DComponent {
	public final MutableLiveString value;
	private final ListenerHandle<LiveString.Listener> valueListener;
	
	private final DStyleClass styleClass;
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private DIRectangle bounds = DIRectangle.EMPTY;
	private final DDimension preferredWidth;
	
	private DUIContext context;
	private DInputFieldStyle style;
	private DFontMetrics fontMetrics;
	private int cursorFrom = -1;
	private int cursorTo = -1;
	
	private Runnable onEnter = null;
	private Runnable onEscape = null;
	
	private boolean cursorBlink = true;
	private DTimerHandle blinkTimer;
	
	public DInputField(DStyleClass styleClass, MutableLiveString value, DDimension preferredWidth) {
		this.styleClass = styleClass;
		this.value = value;
		this.preferredWidth = preferredWidth;
		
		valueListener = value.addListener((oldValue, newValue) -> handleValueUpdated(newValue));
		cursorFrom = 0;
		cursorTo = value.getValue().length();
	}
	
	public void setOnEnter(Runnable onEnter) {
		this.onEnter = onEnter;
	}
	
	public void setOnEscape(Runnable onEscape) {
		this.onEscape = onEscape;
	}
	
	@Override
	public void close() {
		valueListener.close();
		blinkTimer.close();
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("input", styleClass);
		style = new DInputFieldStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
		dimensionPreferences.setValue(new DDimensionPreferences(
				preferredWidth.evalInt(context) + style.paddingLeft + style.paddingRight + 2 * style.borderWidth,
				fontMetrics.getAscent() + fontMetrics.getDescent() + style.paddingTop + style.paddingBottom + 2 * style.borderWidth));
		
		if (blinkTimer != null)
			blinkTimer.close();
		blinkTimer = context.setTimer(300, this::blink);
	}
	
	private void blink() {
		cursorBlink = !cursorBlink;
		context.repaint(bounds);
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return style.borderWidth + style.paddingTop + fontMetrics.getAscent();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		if (style.borderWidth > 0) {
			canvas.strokePath(
					DPath.rectangle(bounds.x, bounds.y, bounds.width - style.borderWidth, bounds.height - style.borderWidth),
					DTransform2D.IDENTITY,
					style.borderColor,
					style.borderWidth);
		}
		
		int cursorXFrom = fontMetrics.getWidth(value.getValue(), 0, Math.min(cursorFrom, cursorTo));
		int cursorXTo = fontMetrics.getWidth(value.getValue(), 0, Math.max(cursorFrom, cursorTo));
		if (cursorFrom != cursorTo) {
			canvas.fillRectangle(
					bounds.x + style.paddingLeft + cursorXFrom,
					bounds.y + style.paddingTop,
					cursorXTo - cursorXFrom,
					fontMetrics.getAscent() + fontMetrics.getDescent(),
					style.selectionColor);
		}
		
		canvas.drawText(style.font, style.color, bounds.x + style.paddingLeft + style.borderWidth, bounds.y + style.paddingBottom + style.borderWidth + fontMetrics.getAscent(), value.getValue());
		
		if (cursorBlink) {
			canvas.fillRectangle(
					bounds.x + style.paddingLeft + cursorXTo,
					bounds.y + style.paddingTop,
					style.cursorWidth,
					fontMetrics.getAscent() + fontMetrics.getDescent(),
					style.cursorColor);
		}
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		context.setCursor(DUIContext.Cursor.TEXT);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		context.setCursor(DUIContext.Cursor.NORMAL);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		context.getWindow().focus(this);
	}
	
	@Override
	public void onKeyPressed(DKeyEvent e) {
		boolean shift = e.has(DKeyEvent.SHIFT);
		switch (e.keyCode) {
			case UP:
				setCursor(0, 0);
				break;
			case DOWN:
				setCursor(value.getValue().length(), value.getValue().length());
				break;
			case LEFT: {
				int to = Math.max(0, cursorTo - 1);
				setCursor(shift ? cursorFrom : to, to);
				break;
			}
			case RIGHT: {
				int to = Math.min(value.getValue().length(), cursorTo + 1);
				setCursor(shift ? cursorFrom : to, to);
				break;
			}
			case DELETE:
				delete();
				break;
			case BACKSPACE:
				backspace();
				break;
			case ENTER:
				enter();
				break;
			case ESCAPE:
				escape();
				break;
			default:
				if (e.character == DKeyEvent.CHAR_UNDEFINED)
					return;
				
				insert(Character.toString(e.character));
				break;
		}
	}
	
	private void setCursor(int from, int to) {
		cursorFrom = from;
		cursorTo = to;
		context.repaint(bounds);
	}
	
	private void handleValueUpdated(String newValue) {
		context.repaint(bounds);
	}
	
	private void backspace() {
		if (cursorFrom == 0 && cursorTo == 0)
			return;
		
		if (cursorFrom == cursorTo) {
			value.setValue(value.getValue().substring(0, cursorFrom - 1) + value.getValue().substring(cursorFrom));
			setCursor(cursorFrom - 1, cursorTo - 1);
		} else {
			int from = Math.min(cursorFrom, cursorTo);
			int to = Math.max(cursorFrom, cursorTo);
			setCursor(from, from);
			value.setValue(value.getValue().substring(0, from) + value.getValue().substring(to));
		}
	}
	
	private void delete() {
		if (cursorFrom == 0 && cursorTo == 0)
			return;
		
		if (cursorFrom == cursorTo) {
			if (cursorFrom < value.getValue().length()) {
				value.setValue(value.getValue().substring(0, cursorFrom) + value.getValue().substring(cursorFrom + 1));
			}
		} else {
			int from = Math.min(cursorFrom, cursorTo);
			int to = Math.max(cursorFrom, cursorTo);
			setCursor(from, from);
			value.setValue(value.getValue().substring(0, from) + value.getValue().substring(to));
		}
	}
	
	private void insert(String value) {
		int from = Math.min(cursorFrom, cursorTo);
		int to = Math.max(cursorFrom, cursorTo);
		this.value.setValue(this.value.getValue().substring(0, from) + value + this.value.getValue().substring(to));
		setCursor(from + value.length(), from + value.length());
	}
	
	private void enter() {
		if (onEnter != null)
			onEnter.run();
	}
	
	private void escape() {
		if (onEscape != null)
			onEscape.run();
	}
}
