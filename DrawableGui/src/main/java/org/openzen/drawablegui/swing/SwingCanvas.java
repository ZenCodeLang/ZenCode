/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Stack;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DPathBoundsCalculator;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DShadow;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingCanvas implements DCanvas {
	private final Graphics2D g;
	private final SwingGraphicsContext context;
	private final Stack<Rectangle> bounds = new Stack<>();
	private final Stack<AffineTransform> transformStack = new Stack<>();
	
	public SwingCanvas(Graphics2D g, SwingGraphicsContext context, DIRectangle bounds) {
		this.g = g;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		this.context = context;
		
		if (bounds != null)
			pushBounds(new DIRectangle(bounds.x, bounds.y, bounds.width, bounds.height));
	}
	
	@Override
	public void pushBounds(DIRectangle bounds) {
		this.bounds.push(g.getClipBounds());
		g.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	@Override
	public void popBounds() {
		Rectangle bounds = this.bounds.pop();
		g.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	@Override
	public void pushOffset(int x, int y) {
		transformStack.push(g.getTransform());
		g.transform(AffineTransform.getTranslateInstance(x, y));
	}
	
	@Override
	public void popOffset() {
		g.setTransform(transformStack.pop());
	}
	
	@Override
	public DIRectangle getBounds() {
		Rectangle bounds = g.getClipBounds();
		return new DIRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public void drawText(DFont font, int color, float x, float y, String text) {
		prepare(font);
		g.setColor(new Color(color, true));
		g.setFont((Font) font.cached);
		g.drawString(text, x, y);
	}

	@Override
	public DUIContext getContext() {
		return context;
	}

	@Override
	public void strokePath(DPath path, DTransform2D transform, int color, float lineWidth) {
		if (color == 0)
			return;
		
		AffineTransform old = g.getTransform();
		GeneralPath jPath = context.getPath(path);
		g.setColor(new Color(color, true));
		g.setStroke(new BasicStroke(lineWidth));
		g.transform(getTransform(transform));
		g.draw(jPath);
		g.setTransform(old);
	}

	@Override
	public void fillPath(DPath path, DTransform2D transform, int color) {
		if (color == 0)
			return;
		
		AffineTransform old = g.getTransform();
		GeneralPath jPath = context.getPath(path);
		g.setColor(new Color(color, true));
		g.transform(getTransform(transform));
		g.fill(jPath);
		g.setTransform(old);
	}

	@Override
	public void shadowPath(DPath path, DTransform2D transform, DShadow shadow) {
		if (shadow.color == 0)
			return;
		
		if (shadow.radius == 0) {
			fillPath(path, transform, shadow.color);
			return;
		}
		
		DIRectangle bounds = DPathBoundsCalculator.getBounds(path, transform.offset(shadow.offsetX, shadow.offsetY));
		int offset = 2 * (int)Math.ceil(shadow.radius);
		
		GeneralPath jPath = context.getPath(path);
		
		BufferedImage image = new BufferedImage(bounds.width + 2 * offset, bounds.height + 2 * offset, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D imageG = (Graphics2D) image.getGraphics();
		
		imageG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		imageG.setColor(new Color(shadow.color, true));
		imageG.setTransform(getTransform(transform.offset(offset + shadow.offsetX - bounds.x, offset + shadow.offsetY - bounds.y)));
		imageG.fill(jPath);
		
		image = getGaussianBlurFilter((int)Math.ceil(shadow.radius), true).filter(image, null);
		image = getGaussianBlurFilter((int)Math.ceil(shadow.radius), false).filter(image, null);
		g.drawImage(image, bounds.x - offset, bounds.y - offset, null);
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height, int color) {
		if (color == 0)
			return;
		
		g.setColor(new Color(color, true));
		g.fillRect(x, y, width, height);
	}
	
	public static void prepare(DFont font) {
		if (font.cached != null && font.cached instanceof Font)
			return;
		
		String baseFontName = font.family == DFontFamily.CODE ? "Consolas" : Font.DIALOG;
		int style = 0;
		if (font.bold)
			style |= Font.BOLD;
		if (font.italic)
			style |= Font.ITALIC;
		
		font.cached = Font.decode(baseFontName).deriveFont(style, font.size);
	}
	
	private AffineTransform getTransform(DTransform2D transform) {
		return new AffineTransform(transform.xx, transform.xy, transform.yx, transform.yy, transform.dx, transform.dy);
	}
    
	
	// taken from http://www.java2s.com/Code/Java/Advanced-Graphics/GaussianBlurDemo.htm
    public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }
        
        int size = radius * 2 + 1;
        float[] data = new float[size];
        
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }        
        
        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
}
