/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.WeakHashMap;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DClipboard;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DPathTracer;
import org.openzen.drawablegui.DTimerHandle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.style.DStylePathRoot;
import org.openzen.drawablegui.style.DStyleSheets;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingGraphicsContext implements DUIContext {
	private final DStyleSheets stylesheets;
	private final float scale;
	private final float textScale;
	private final WeakHashMap<DPath, GeneralPath> preparedPaths = new WeakHashMap<>();
	private final SwingRoot root;
	private final JavaClipboard clipboard = new JavaClipboard();
	private Graphics graphics;
	private DUIWindow window;
	private DDrawSurface surface;
	
	public SwingGraphicsContext(DStyleSheets stylesheets, float scale, float textScale, SwingRoot root) {
		this.stylesheets = stylesheets;
		this.scale = scale;
		this.textScale = textScale;
		this.root = root;
	}
	
	public void setSurface(DDrawSurface surface) {
		this.surface = surface;
	}
	
	public GeneralPath getPath(DPath path) {
		GeneralPath generalPath = preparedPaths.get(path);
		if (generalPath == null) {
			generalPath = new GeneralPath();
			path.trace(new PathTracer(generalPath));
			preparedPaths.put(path, generalPath);
		}
		return generalPath;
	}
	
	public void setWindow(DUIWindow window) {
		this.window = window;
	}
	
	@Override
	public DStyleSheets getStylesheets() {
		return stylesheets;
	}
	
	@Override
	public float getScale() {
		return scale;
	}
	
	@Override
	public float getTextScale() {
		return textScale;
	}
	
	@Override
	public void repaint(int x, int y, int width, int height) {
		root.repaint(x, y, width, height);
	}

	@Override
	public void setCursor(Cursor cursor) {
		int translated = java.awt.Cursor.DEFAULT_CURSOR;
		switch (cursor) {
			case NORMAL:
				translated = java.awt.Cursor.DEFAULT_CURSOR;
				break;
			case MOVE:
				translated = java.awt.Cursor.MOVE_CURSOR;
				break;
			case HAND:
				translated = java.awt.Cursor.HAND_CURSOR;
				break;
			case TEXT:
				translated = java.awt.Cursor.TEXT_CURSOR;
				break;
			case E_RESIZE:
				translated = java.awt.Cursor.E_RESIZE_CURSOR;
				break;
			case S_RESIZE:
				translated = java.awt.Cursor.S_RESIZE_CURSOR;
				break;
			case NE_RESIZE:
				translated = java.awt.Cursor.NE_RESIZE_CURSOR;
				break;
			case NW_RESIZE:
				translated = java.awt.Cursor.NW_RESIZE_CURSOR;
				break;
		}
		root.setCursor(java.awt.Cursor.getPredefinedCursor(translated));
	}

	@Override
	public DFontMetrics getFontMetrics(DFont font) {
		if (graphics == null)
			graphics = root.getGraphics();
		
		if (graphics == null)
			throw new AssertionError("No graphics available!");
		
		SwingCanvas.prepare(font);
		return new SwingFontMetrics(graphics.getFontMetrics((Font) font.cached), graphics);
	}

	@Override
	public void scrollInView(int x, int y, int width, int height) {
		// not in a scrollable context
	}

	@Override
	public DTimerHandle setTimer(int millis, Runnable target) {
		Timer timer = new Timer(millis, e -> target.run());
		timer.start();
		return () -> timer.stop();
	}

	@Override
	public DClipboard getClipboard() {
		return clipboard;
	}

	@Override
	public DUIWindow getWindow() {
		return window;
	}

	@Override
	public DUIWindow openDialog(int x, int y, DAnchor anchor, String title, DComponent root) {
		SwingWindow swingWindow = (SwingWindow)this.window;
		SwingDialog window = new SwingDialog(swingWindow, title, root, false);
		SwingGraphicsContext windowContext = window.swingComponent.context;
		windowContext.setWindow(window);
		windowContext.graphics = this.graphics; // help a little...
		windowContext.setSurface(window.swingComponent.surface);
		
		Point rootLocation = swingWindow.swingComponent.getLocationOnScreen();
		
		root.setSurface(DStylePathRoot.INSTANCE, 0, windowContext.surface);
		DSizing dimension = root.getSizing().getValue();
		int tx = (int)(x + rootLocation.x - anchor.alignX * dimension.preferredWidth);
		int ty = (int)(y + rootLocation.y - anchor.alignY * dimension.preferredHeight);
		
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.swingComponent.setPreferredSize(new Dimension(dimension.preferredWidth, dimension.preferredHeight));
		window.setLocation(tx, ty);
		window.pack();
		window.setVisible(true);
		return window;
	}

	@Override
	public DUIWindow openView(int x, int y, DAnchor anchor, DComponent root) {
		SwingWindow swingWindow = (SwingWindow)this.window;
		SwingInlineWindow window = new SwingInlineWindow(swingWindow, "", root);
		SwingGraphicsContext windowContext = window.swingComponent.context;
		windowContext.setWindow(window);
		windowContext.graphics = this.graphics; // help a little...
		windowContext.setSurface(window.swingComponent.surface);
		
		root.setSurface(DStylePathRoot.INSTANCE, 0, windowContext.surface);
		DSizing dimension = root.getSizing().getValue();
		
		Point rootLocation = swingWindow.swingComponent.getLocationOnScreen();
		
		int tx = (int)(x + rootLocation.x - anchor.alignX * dimension.preferredWidth);
		int ty = (int)(y + rootLocation.y - anchor.alignY * dimension.preferredHeight);
		
		window.swingComponent.setPreferredSize(new Dimension(dimension.preferredWidth, dimension.preferredHeight));
		window.setLocation(tx, ty);
		window.pack();
		window.setVisible(true);
		window.swingComponent.repaint();
		
		return window;
	}
	
	private class PathTracer implements DPathTracer {
		private final GeneralPath path;
		
		public PathTracer(GeneralPath path) {
			this.path = path;
		}

		@Override
		public void moveTo(float x, float y) {
			path.moveTo(x, y);
		}

		@Override
		public void lineTo(float x, float y) {
			path.lineTo(x, y);
		}

		@Override
		public void bezierCubic(float x1, float y1, float x2, float y2, float x3, float y3) {
			path.curveTo(x1, y1, x2, y2, x3, y3);
		}

		@Override
		public void bezierQuadratic(float x1, float y1, float x2, float y2) {
			path.quadTo(x1, y1, x2, y2);
		}

		@Override
		public void close() {
			path.closePath();
		}
	}
}
