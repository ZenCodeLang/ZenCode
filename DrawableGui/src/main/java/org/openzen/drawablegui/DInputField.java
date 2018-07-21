/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.MutableLiveString;
import org.openzen.drawablegui.style.DDimension;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DInputField implements DComponent {
	public final MutableLiveString value;
	private final ListenerHandle<LiveString.Listener> valueListener;
	
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private DIRectangle bounds = DIRectangle.EMPTY;
	private final DDimension preferredWidth;
	
	private DComponentContext context;
	private DInputFieldStyle style;
	private DFontMetrics fontMetrics;
	private int cursorFrom = -1;
	private int cursorTo = -1;
	
	private Runnable onEnter = null;
	private Runnable onEscape = null;
	
	private boolean cursorBlink = true;
	private DTimerHandle blinkTimer;
	
	private DDrawnShape shape;
	private DDrawnText text;
	private DDrawnRectangle cursor;
	private DDrawnRectangle selection;
	
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
		unmount();
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("input", styleClass);
		style = context.getStyle(DInputFieldStyle::new);
		fontMetrics = context.getFontMetrics(style.font);
		
		sizing.setValue(new DSizing(
				preferredWidth.evalInt(context.getUIContext()) + style.margin.getHorizontal() + style.border.getPaddingHorizontal(),
				fontMetrics.getAscent() + fontMetrics.getDescent() + style.margin.getVertical() + style.border.getPaddingVertical()));
		
		if (blinkTimer != null)
			blinkTimer.close();
		blinkTimer = context.getUIContext().setTimer(300, this::blink);
		
		if (text != null)
			text.close();
		text = parent.drawText(
				2,
				style.font,
				style.color,
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop() + fontMetrics.getAscent(),
				value.getValue());
		
		if (cursor != null)
			cursor.close();
		cursor = parent.fillRect(2, DIRectangle.EMPTY, cursorBlink ? style.cursorColor : 0);
		
		if (selection != null)
			selection.close();
		selection = parent.fillRect(1, DIRectangle.EMPTY, 0);
		
		setCursor(cursorFrom, cursorTo);
	}
	
	@Override
	public void unmount() {
		blinkTimer.close();
		
		if (style != null)
			style.border.close();
		if (shape != null)
			shape.close();
		if (text != null)
			text.close();
		if (cursor != null)
			cursor.close();
		if (selection != null)
			selection.close();
	}
	
	private void blink() {
		cursorBlink = !cursorBlink;
		cursor.setColor(cursorBlink ? style.cursorColor : 0);
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
		return style.margin.top + style.border.getPaddingTop() + fontMetrics.getAscent();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		setCursor(cursorFrom, cursorTo);
		
		if (shape != null)
			shape.close();
		shape = context.fillPath(0, style.shape.instance(style.margin.apply(bounds)), DTransform2D.IDENTITY, style.backgroundColor);
		text.setPosition(
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop() + fontMetrics.getAscent());
		style.border.update(context, bounds);
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		context.getUIContext().setCursor(DUIContext.Cursor.TEXT);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		context.getUIContext().setCursor(DUIContext.Cursor.NORMAL);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		context.getUIContext().getWindow().focus(this);
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
		
		int cursorXFrom = fontMetrics.getWidth(value.getValue(), 0, Math.min(cursorFrom, cursorTo));
		int cursorXTo = fontMetrics.getWidth(value.getValue(), 0, Math.max(cursorFrom, cursorTo));
		if (cursorFrom != cursorTo) {
			selection.setRectangle(new DIRectangle(
					bounds.x + style.margin.left + style.border.getPaddingLeft() + cursorXFrom,
					bounds.y + style.margin.top + style.border.getPaddingTop(),
					cursorXTo - cursorXFrom,
					fontMetrics.getAscent() + fontMetrics.getDescent()));
			selection.setColor(style.selectionColor);
		} else {
			selection.setColor(0);
		}
		
		cursor.setRectangle(new DIRectangle(
				bounds.x + style.margin.left + style.border.getPaddingLeft() + cursorXTo,
				bounds.y + style.margin.top + style.border.getPaddingTop(),
				style.cursorWidth,
				fontMetrics.getAscent() + fontMetrics.getDescent()));
	}
	
	private void handleValueUpdated(String newValue) {
		if (text != null)
			text.close();
		text = context.drawText(
				2,
				style.font,
				style.color,
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop() + fontMetrics.getAscent(),
				value.getValue());
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
