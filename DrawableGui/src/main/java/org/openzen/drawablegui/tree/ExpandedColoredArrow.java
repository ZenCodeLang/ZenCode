/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.draw.DDrawTarget;

public class ExpandedColoredArrow implements DColorableIcon {
	public static final ExpandedColoredArrow INSTANCE = new ExpandedColoredArrow();
	
	private ExpandedColoredArrow() {}
	
	private static final DPath PATH_0 = tracer -> {
		tracer.moveTo(17, 7);
		tracer.lineTo(17, 17);
		tracer.lineTo(7, 17);
		tracer.close();
	};
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.fillPath(z, PATH_0, transform, color);
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
