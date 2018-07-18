/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.draw.DDrawTarget;
import org.openzen.drawablegui.style.DShadow;

public class ShadedSaveIcon implements DColorableIcon {
	public static final ShadedSaveIcon INSTANCE = new ShadedSaveIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	public static final ColoredIcon PURPLE = new ColoredIcon(INSTANCE, 0xFF5758BB);
	
	private ShadedSaveIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(17f, 3f);
		tracer.lineTo(5.0f, 3.0f);
		tracer.bezierCubic(3.8899999f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f);
		tracer.lineTo(3.0f, 19.0f);
		tracer.bezierCubic(3.0f, 20.1f, 3.8899999f, 21.0f, 5.0f, 21.0f);
		tracer.lineTo(19.0f, 21.0f);
		tracer.bezierCubic(20.1f, 21.0f, 21.0f, 20.1f, 21.0f, 19.0f);
		tracer.lineTo(21.0f, 7.0f);
		tracer.lineTo(17.0f, 3.0f);
		tracer.close();
		tracer.moveTo(12.0f, 19.0f);
		tracer.bezierCubic(10.34f, 19.0f, 9.0f, 17.66f, 9.0f, 16.0f);
		tracer.bezierCubic(9.0f, 14.34f, 10.34f, 13.0f, 12.0f, 13.0f);
		tracer.bezierCubic(13.66f, 13.0f, 15.0f, 14.34f, 15.0f, 16.0f);
		tracer.bezierCubic(15.0f, 17.66f, 13.66f, 19.0f, 12.0f, 19.0f);
		tracer.close();
		tracer.moveTo(15.0f, 9.0f);
		tracer.lineTo(5.0f, 9.0f);
		tracer.lineTo(5.0f, 5.0f);
		tracer.lineTo(15.0f, 5.0f);
		tracer.lineTo(15.0f, 9.0f);
		tracer.close();
	};
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.shadowPath(z, PATH, transform, color, new DShadow(0xFFCCCCCC, 0, 1, 4));
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
