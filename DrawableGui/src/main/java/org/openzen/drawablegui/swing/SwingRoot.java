/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DKeyEvent;
import static org.openzen.drawablegui.DKeyEvent.KeyCode.*;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.style.DEmptyStylesheets;
import org.openzen.drawablegui.style.DStylePathRoot;

/**
 *
 * @author Hoofdgebruiker
 */
public final class SwingRoot extends Component implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	public final SwingGraphicsContext context;
	public final SwingDrawSurface surface;
	public final DComponent component;
	private DComponent focus = null;
	private boolean firstPaint = true;

	public SwingRoot(DComponent root) {
		this.component = root;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false); // prevent tab from being handled by focus traversal
		
		context = new SwingGraphicsContext(
				DEmptyStylesheets.INSTANCE,
				Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f,
				Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f,
				this);
		surface = new SwingDrawSurface(context);
		context.setSurface(surface);
		
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}
	
	public void setWindow(DUIWindow window) {
		context.setWindow(window);
	}
	
	public void focus(DComponent component) {
		if (component == focus)
			return;
		
		if (focus != null)
			focus.onFocusLost();
		this.focus = component;
		if (focus != null)
			focus.onFocusGained();
	}

	@Override
	public void paint(Graphics g) {
		if (firstPaint) {
			firstPaint = false;
			component.setSurface(DStylePathRoot.INSTANCE, 0, surface);
			component.setBounds(new DIRectangle(0, 0, getWidth(), getHeight()));
		}
		
		long start = System.currentTimeMillis();
		Rectangle clipBounds = g.getClipBounds();
		DIRectangle clipBounds2 = clipBounds == null ? null : new DIRectangle(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		surface.paint(g2d);
		SwingCanvas canvas = new SwingCanvas(g2d, context, clipBounds2);
		component.paint(canvas);
		
		System.out.println("Paint in " + (System.currentTimeMillis() - start) + " ms");
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (firstPaint)
			return;
		
		component.setBounds(new DIRectangle(0, 0,
				e.getComponent().getWidth(),
				e.getComponent().getHeight()));

		invalidate();
	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		component.onMouseClick(translateMouseEvent(e));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		component.onMouseDown(translateMouseEvent(e));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		component.onMouseRelease(translateMouseEvent(e));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		component.onMouseEnter(translateMouseEvent(e));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		component.onMouseExit(translateMouseEvent(e));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		component.onMouseDrag(translateMouseEvent(e));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		component.onMouseMove(translateMouseEvent(e));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		component.onMouseScroll(translateScrollEvent(e));
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (focus == null)
			return;

		focus.onKeyTyped(translateKeyEvent(e));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (focus == null)
			return;
		
		focus.onKeyPressed(translateKeyEvent(e));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (focus == null)
			return;
		
		focus.onKeyReleased(translateKeyEvent(e));
	}

	private DMouseEvent translateMouseEvent(MouseEvent e) {
		return new DMouseEvent(context.getWindow(), e.getX(), e.getY(), getModifiers(e.getModifiersEx()), 0, e.getClickCount());
	}

	private DMouseEvent translateScrollEvent(MouseWheelEvent e) {
		return new DMouseEvent(context.getWindow(), e.getX(), e.getY(), getModifiers(e.getModifiersEx()), e.getUnitsToScroll(), e.getClickCount());
	}
	
	private DKeyEvent translateKeyEvent(KeyEvent e) {
		return new DKeyEvent(e.getKeyChar(), getKeyCode(e.getKeyCode()), getModifiers(e.getModifiersEx()));
	}
	
	private DKeyEvent.KeyCode getKeyCode(int code) {
		switch (code) {
			case KeyEvent.VK_ENTER:
				return ENTER;
			case KeyEvent.VK_BACK_SPACE:
				return BACKSPACE;
			case KeyEvent.VK_TAB:
				return TAB;
    		case KeyEvent.VK_CANCEL:
				return DKeyEvent.KeyCode.CANCEL;
    		case KeyEvent.VK_CLEAR:
				return DKeyEvent.KeyCode.CLEAR;
    		case KeyEvent.VK_SHIFT:
				return DKeyEvent.KeyCode.SHIFT;
    		case KeyEvent.VK_CONTROL:
				return DKeyEvent.KeyCode.CONTROL;
    		case KeyEvent.VK_ALT:
				return DKeyEvent.KeyCode.ALT;
    		case KeyEvent.VK_PAUSE:
				return DKeyEvent.KeyCode.PAUSE;
    		case KeyEvent.VK_CAPS_LOCK:
				return DKeyEvent.KeyCode.CAPS_LOCK;
    		case KeyEvent.VK_ESCAPE:
				return DKeyEvent.KeyCode.ESCAPE;
    		case KeyEvent.VK_SPACE:
				return DKeyEvent.KeyCode.SPACE;
    		case KeyEvent.VK_PAGE_UP:
				return DKeyEvent.KeyCode.PAGE_UP;
    		case KeyEvent.VK_PAGE_DOWN:
				return DKeyEvent.KeyCode.PAGE_DOWN;
    		case KeyEvent.VK_END:
				return DKeyEvent.KeyCode.END;
    		case KeyEvent.VK_HOME:
				return DKeyEvent.KeyCode.HOME;
    		case KeyEvent.VK_LEFT:
				return DKeyEvent.KeyCode.LEFT;
    		case KeyEvent.VK_UP:
				return DKeyEvent.KeyCode.UP;
    		case KeyEvent.VK_RIGHT:
				return DKeyEvent.KeyCode.RIGHT;
    		case KeyEvent.VK_DOWN:
				return DKeyEvent.KeyCode.DOWN;
    		case KeyEvent.VK_COMMA:
				return DKeyEvent.KeyCode.COMMA;
    		case KeyEvent.VK_MINUS:
				return DKeyEvent.KeyCode.MINUS;
    		case KeyEvent.VK_PERIOD:
				return DKeyEvent.KeyCode.PERIOD;
    		case KeyEvent.VK_SLASH:
				return DKeyEvent.KeyCode.SLASH;
    		case KeyEvent.VK_0:
				return DKeyEvent.KeyCode.NUM0;
    		case KeyEvent.VK_1:
				return DKeyEvent.KeyCode.NUM1;
    		case KeyEvent.VK_2:
				return DKeyEvent.KeyCode.NUM2;
    		case KeyEvent.VK_3:
				return DKeyEvent.KeyCode.NUM3;
    		case KeyEvent.VK_4:
				return DKeyEvent.KeyCode.NUM4;
    		case KeyEvent.VK_5:
				return DKeyEvent.KeyCode.NUM5;
    		case KeyEvent.VK_6:
				return DKeyEvent.KeyCode.NUM6;
    		case KeyEvent.VK_7:
				return DKeyEvent.KeyCode.NUM7;
    		case KeyEvent.VK_8:
				return DKeyEvent.KeyCode.NUM8;
    		case KeyEvent.VK_9:
				return DKeyEvent.KeyCode.NUM9;
    		case KeyEvent.VK_SEMICOLON:
				return DKeyEvent.KeyCode.SEMICOLON;
    		case KeyEvent.VK_EQUALS:
				return DKeyEvent.KeyCode.EQUALS;
    		case KeyEvent.VK_A:
				return DKeyEvent.KeyCode.A;
    		case KeyEvent.VK_B:
				return DKeyEvent.KeyCode.B;
    		case KeyEvent.VK_C:
				return DKeyEvent.KeyCode.C;
    		case KeyEvent.VK_D:
				return DKeyEvent.KeyCode.D;
    		case KeyEvent.VK_E:
				return DKeyEvent.KeyCode.E;
    		case KeyEvent.VK_F:
				return DKeyEvent.KeyCode.F;
    		case KeyEvent.VK_G:
				return DKeyEvent.KeyCode.G;
    		case KeyEvent.VK_H:
				return DKeyEvent.KeyCode.H;
    		case KeyEvent.VK_I:
				return DKeyEvent.KeyCode.I;
    		case KeyEvent.VK_J:
				return DKeyEvent.KeyCode.J;
    		case KeyEvent.VK_K:
				return DKeyEvent.KeyCode.K;
    		case KeyEvent.VK_L:
				return DKeyEvent.KeyCode.L;
    		case KeyEvent.VK_M:
				return DKeyEvent.KeyCode.M;
    		case KeyEvent.VK_N:
				return DKeyEvent.KeyCode.N;
    		case KeyEvent.VK_O:
				return DKeyEvent.KeyCode.O;
    		case KeyEvent.VK_P:
				return DKeyEvent.KeyCode.P;
    		case KeyEvent.VK_Q:
				return DKeyEvent.KeyCode.Q;
    		case KeyEvent.VK_R:
				return DKeyEvent.KeyCode.R;
    		case KeyEvent.VK_S:
				return DKeyEvent.KeyCode.S;
    		case KeyEvent.VK_T:
				return DKeyEvent.KeyCode.T;
    		case KeyEvent.VK_U:
				return DKeyEvent.KeyCode.U;
    		case KeyEvent.VK_V:
				return DKeyEvent.KeyCode.V;
    		case KeyEvent.VK_W:
				return DKeyEvent.KeyCode.W;
    		case KeyEvent.VK_X:
				return DKeyEvent.KeyCode.X;
    		case KeyEvent.VK_Y:
				return DKeyEvent.KeyCode.Y;
    		case KeyEvent.VK_Z:
				return DKeyEvent.KeyCode.Z;
    		case KeyEvent.VK_OPEN_BRACKET:
				return DKeyEvent.KeyCode.OPEN_BRACKET;
    		case KeyEvent.VK_BACK_SLASH:
				return DKeyEvent.KeyCode.BACKSLASH;
    		case KeyEvent.VK_CLOSE_BRACKET:
				return DKeyEvent.KeyCode.CLOSE_BRACKET;
    		case KeyEvent.VK_NUMPAD0:
				return DKeyEvent.KeyCode.NUMPAD0;
    		case KeyEvent.VK_NUMPAD1:
				return DKeyEvent.KeyCode.NUMPAD1;
    		case KeyEvent.VK_NUMPAD2:
				return DKeyEvent.KeyCode.NUMPAD2;
    		case KeyEvent.VK_NUMPAD3:
				return DKeyEvent.KeyCode.NUMPAD3;
    		case KeyEvent.VK_NUMPAD4:
				return DKeyEvent.KeyCode.NUMPAD4;
    		case KeyEvent.VK_NUMPAD5:
				return DKeyEvent.KeyCode.NUMPAD5;
    		case KeyEvent.VK_NUMPAD6:
				return DKeyEvent.KeyCode.NUMPAD6;
    		case KeyEvent.VK_NUMPAD7:
				return DKeyEvent.KeyCode.NUMPAD7;
    		case KeyEvent.VK_NUMPAD8:
				return DKeyEvent.KeyCode.NUMPAD8;
    		case KeyEvent.VK_NUMPAD9:
				return DKeyEvent.KeyCode.NUMPAD9;
    		case KeyEvent.VK_MULTIPLY:
				return DKeyEvent.KeyCode.MULTIPLY;
    		case KeyEvent.VK_ADD:
				return DKeyEvent.KeyCode.ADD;
			case KeyEvent.VK_SEPARATER:
				return DKeyEvent.KeyCode.SEPARATOR;
    		case KeyEvent.VK_SUBTRACT:
				return DKeyEvent.KeyCode.SUBTRACT;
    		case KeyEvent.VK_DECIMAL:
				return DKeyEvent.KeyCode.DECIMAL;
    		case KeyEvent.VK_DIVIDE:
				return DKeyEvent.KeyCode.DIVIDE;
    		case KeyEvent.VK_DELETE:
				return DKeyEvent.KeyCode.DELETE; /* ASCII DEL */
    		case KeyEvent.VK_NUM_LOCK:
				return DKeyEvent.KeyCode.NUM_LOCK;
    		case KeyEvent.VK_SCROLL_LOCK:
				return DKeyEvent.KeyCode.SCROLL_LOCK;
    		case KeyEvent.VK_F1:
				return DKeyEvent.KeyCode.F1;
    		case KeyEvent.VK_F2:
				return DKeyEvent.KeyCode.F2;
    		case KeyEvent.VK_F3:
				return DKeyEvent.KeyCode.F3;
    		case KeyEvent.VK_F4:
				return DKeyEvent.KeyCode.F4;
    		case KeyEvent.VK_F5:
				return DKeyEvent.KeyCode.F5;
    		case KeyEvent.VK_F6:
				return DKeyEvent.KeyCode.F6;
    		case KeyEvent.VK_F7:
				return DKeyEvent.KeyCode.F7;
    		case KeyEvent.VK_F8:
				return DKeyEvent.KeyCode.F8;
    		case KeyEvent.VK_F9:
				return DKeyEvent.KeyCode.F9;
    		case KeyEvent.VK_F10:
				return DKeyEvent.KeyCode.F10;
    		case KeyEvent.VK_F11:
				return DKeyEvent.KeyCode.F11;
    		case KeyEvent.VK_F12:
				return DKeyEvent.KeyCode.F12;
    		case KeyEvent.VK_F13:
				return DKeyEvent.KeyCode.F13;
    		case KeyEvent.VK_F14:
				return DKeyEvent.KeyCode.F14;
    		case KeyEvent.VK_F15:
				return DKeyEvent.KeyCode.F15;
    		case KeyEvent.VK_F16:
				return DKeyEvent.KeyCode.F16;
    		case KeyEvent.VK_F17:
				return DKeyEvent.KeyCode.F17;
    		case KeyEvent.VK_F18:
				return DKeyEvent.KeyCode.F18;
    		case KeyEvent.VK_F19:
				return DKeyEvent.KeyCode.F19;
    		case KeyEvent.VK_F20:
				return DKeyEvent.KeyCode.F20;
    		case KeyEvent.VK_F21:
				return DKeyEvent.KeyCode.F21;
    		case KeyEvent.VK_F22:
				return DKeyEvent.KeyCode.F22;
    		case KeyEvent.VK_F23:
				return DKeyEvent.KeyCode.F23;
    		case KeyEvent.VK_F24:
				return DKeyEvent.KeyCode.F24;
    		case KeyEvent.VK_PRINTSCREEN:
				return DKeyEvent.KeyCode.PRINTSCREEN;
    		case KeyEvent.VK_INSERT:
				return DKeyEvent.KeyCode.INSERT;
    		case KeyEvent.VK_HELP:
				return DKeyEvent.KeyCode.HELP;
    		case KeyEvent.VK_META:
				return DKeyEvent.KeyCode.META;
    		case KeyEvent.VK_BACK_QUOTE:
				return DKeyEvent.KeyCode.BACKQUOTE;
    		case KeyEvent.VK_QUOTE:
				return DKeyEvent.KeyCode.QUOTE;
    		case KeyEvent.VK_KP_UP:
				return DKeyEvent.KeyCode.KEYPAD_UP;
    		case KeyEvent.VK_KP_DOWN:
				return DKeyEvent.KeyCode.KEYPAD_DOWN;
    		case KeyEvent.VK_KP_LEFT:
				return DKeyEvent.KeyCode.KEYPAD_LEFT;
    		case KeyEvent.VK_KP_RIGHT:
				return DKeyEvent.KeyCode.KEYPAD_RIGHT;
    		case KeyEvent.VK_AMPERSAND:
				return DKeyEvent.KeyCode.AMPERSAND;
    		case KeyEvent.VK_ASTERISK:
				return DKeyEvent.KeyCode.ASTERISK;
    		case KeyEvent.VK_QUOTEDBL:
				return DKeyEvent.KeyCode.QUOTEDBL;
    		case KeyEvent.VK_LESS:
				return DKeyEvent.KeyCode.LESS;
			case KeyEvent.VK_GREATER:
				return DKeyEvent.KeyCode.GREATER;
			case KeyEvent.VK_BRACELEFT:
				return DKeyEvent.KeyCode.BRACELEFT;
			case KeyEvent.VK_BRACERIGHT:
				return DKeyEvent.KeyCode.BRACERIGHT;
    		case KeyEvent.VK_AT:
				return DKeyEvent.KeyCode.AT;
    		case KeyEvent.VK_COLON:
				return DKeyEvent.KeyCode.COLON;
    		case KeyEvent.VK_CIRCUMFLEX:
				return DKeyEvent.KeyCode.CIRCUMFLEX;
    		case KeyEvent.VK_DOLLAR:
				return DKeyEvent.KeyCode.DOLLAR;
    		case KeyEvent.VK_EURO_SIGN:
				return DKeyEvent.KeyCode.EURO_SIGN;
    		case KeyEvent.VK_EXCLAMATION_MARK:
				return DKeyEvent.KeyCode.EXCLAMATION_MARK;
    		case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
				return DKeyEvent.KeyCode.INVERTED_EXCLAMATION_MARK;
    		case KeyEvent.VK_LEFT_PARENTHESIS:
				return DKeyEvent.KeyCode.LEFT_PARENTHESIS;
    		case KeyEvent.VK_NUMBER_SIGN:
				return DKeyEvent.KeyCode.NUMBER_SIGN;
    		case KeyEvent.VK_PLUS:
				return DKeyEvent.KeyCode.PLUS;
    		case KeyEvent.VK_RIGHT_PARENTHESIS:
				return DKeyEvent.KeyCode.RIGHT_PARENTHESIS;
    		case KeyEvent.VK_UNDERSCORE:
				return DKeyEvent.KeyCode.UNDERSCORE;
    		case KeyEvent.VK_WINDOWS:
				return DKeyEvent.KeyCode.WINDOWS;
    		case KeyEvent.VK_CONTEXT_MENU:
				return DKeyEvent.KeyCode.CONTEXT_MENU;
    		case KeyEvent.VK_FINAL:
				return DKeyEvent.KeyCode.FINAL;
    		case KeyEvent.VK_CONVERT:
				return DKeyEvent.KeyCode.CONVERT;
    		case KeyEvent.VK_NONCONVERT:
				return DKeyEvent.KeyCode.NONCONVERT;
    		case KeyEvent.VK_ACCEPT:
				return DKeyEvent.KeyCode.ACCEPT;
    		case KeyEvent.VK_KANA:
				return DKeyEvent.KeyCode.KANA;
    		case KeyEvent.VK_KANJI:
				return DKeyEvent.KeyCode.KANJI;
    		case KeyEvent.VK_ALPHANUMERIC:
				return DKeyEvent.KeyCode.ALPHANUMERIC;
    		case KeyEvent.VK_KATAKANA:
				return DKeyEvent.KeyCode.KATAKANA;
    		case KeyEvent.VK_HIRAGANA:
				return DKeyEvent.KeyCode.HIRAGANA;
    		case KeyEvent.VK_FULL_WIDTH:
				return DKeyEvent.KeyCode.FULL_WIDTH;
    		case KeyEvent.VK_HALF_WIDTH:
				return DKeyEvent.KeyCode.HALF_WIDTH;
    		case KeyEvent.VK_ROMAN_CHARACTERS:
				return DKeyEvent.KeyCode.ROMAN_CHARACTERS;
    		case KeyEvent.VK_ALL_CANDIDATES:
				return DKeyEvent.KeyCode.ALL_CANDIDATES;
    		case KeyEvent.VK_PREVIOUS_CANDIDATE:
				return DKeyEvent.KeyCode.PREVIOUS_CANDIDATE;
    		case KeyEvent.VK_CODE_INPUT:
				return DKeyEvent.KeyCode.CODE_INPUT;
    		case KeyEvent.VK_JAPANESE_KATAKANA:
				return DKeyEvent.KeyCode.JAPANESE_KATAKANA;
    		case KeyEvent.VK_JAPANESE_HIRAGANA:
				return DKeyEvent.KeyCode.JAPANESE_HIRAGANA;
    		case KeyEvent.VK_JAPANESE_ROMAN:
				return DKeyEvent.KeyCode.JAPANESE_ROMAN;
			case KeyEvent.VK_KANA_LOCK:
				return DKeyEvent.KeyCode.KANA_LOCK;
    		case KeyEvent.VK_INPUT_METHOD_ON_OFF:
				return DKeyEvent.KeyCode.INPUT_METHOD_ON_OFF;
    		case KeyEvent.VK_CUT:
				return DKeyEvent.KeyCode.CUT;
    		case KeyEvent.VK_COPY:
				return DKeyEvent.KeyCode.COPY;
    		case KeyEvent.VK_PASTE:
				return DKeyEvent.KeyCode.PASTE;
    		case KeyEvent.VK_UNDO:
				return DKeyEvent.KeyCode.UNDO;
    		case KeyEvent.VK_AGAIN:
				return DKeyEvent.KeyCode.AGAIN;
    		case KeyEvent.VK_FIND:
				return DKeyEvent.KeyCode.FIND;
    		case KeyEvent.VK_PROPS:
				return DKeyEvent.KeyCode.PROPS;
    		case KeyEvent.VK_STOP:
				return DKeyEvent.KeyCode.STOP;
    		case KeyEvent.VK_COMPOSE:
				return DKeyEvent.KeyCode.COMPOSE;
    		case KeyEvent.VK_ALT_GRAPH:
				return DKeyEvent.KeyCode.ALT_GRAPH;
    		case KeyEvent.VK_BEGIN:
				return DKeyEvent.KeyCode.BEGIN;
			default:
				return DKeyEvent.KeyCode.UNKNOWN;
		}
	}

	private int getModifiers(int swingModifiers) {
		int result = 0;
		if ((swingModifiers & MouseEvent.BUTTON1_DOWN_MASK) > 0)
			result |= DMouseEvent.BUTTON1;
		if ((swingModifiers & MouseEvent.BUTTON2_DOWN_MASK) > 0)
			result |= DMouseEvent.BUTTON2;
		if ((swingModifiers & MouseEvent.BUTTON3_DOWN_MASK) > 0)
			result |= DMouseEvent.BUTTON3;
		if ((swingModifiers & MouseEvent.SHIFT_DOWN_MASK) > 0)
			result |= DMouseEvent.SHIFT;
		if ((swingModifiers & MouseEvent.CTRL_DOWN_MASK) > 0)
			result |= DMouseEvent.CTRL;
		if ((swingModifiers & MouseEvent.ALT_DOWN_MASK) > 0)
			result |= DMouseEvent.ALT;
		if ((swingModifiers & MouseEvent.ALT_GRAPH_DOWN_MASK) > 0)
			result |= DMouseEvent.ALT_GRAPH;
		if ((swingModifiers & MouseEvent.META_DOWN_MASK) > 0)
			result |= DMouseEvent.META;
		return result;
	}
}
