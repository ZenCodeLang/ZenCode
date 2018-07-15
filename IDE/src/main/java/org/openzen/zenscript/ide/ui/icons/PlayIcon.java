/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.style.DShadow;

public class PlayIcon implements DColorableIcon {
	public static final PlayIcon INSTANCE = new PlayIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	public static final ColoredIcon GREEN = new ColoredIcon(INSTANCE, 0xFF009432);
	
	private PlayIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(8f, 5f);
		tracer.lineTo(8.0f, 19.0f);
		tracer.lineTo(19.0f, 12.0f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.shadowPath(PATH, transform, color, new DShadow(0xFFCCCCCC, 0, 1, 4));
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
