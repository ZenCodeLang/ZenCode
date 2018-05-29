/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;

public class ColoredCollapsedArrow implements DColorableIcon {
	public static final ColoredCollapsedArrow INSTANCE = new ColoredCollapsedArrow();
	
	private ColoredCollapsedArrow() {}
	
	private static final DPath PATH_0 = tracer -> {
		tracer.moveTo(10, 19);
		tracer.lineTo(17, 12);
		tracer.lineTo(10, 5);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.fillPath(PATH_0, transform, color);
	}

	@Override
	public float getNominalWidth() {
		return 24;
	}

	@Override
	public float getNominalHeight() {
		return 24;
	}
}
