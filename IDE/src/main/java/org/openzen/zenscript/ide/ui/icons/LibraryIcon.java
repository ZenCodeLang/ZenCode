/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

/**
 *
 * @author Hoofdgebruiker
 */
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.draw.DDrawTarget;

public class LibraryIcon implements DColorableIcon {
	public static final LibraryIcon INSTANCE = new LibraryIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	
	private LibraryIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(20.54f, 5.23f);
		tracer.lineTo(19.15f, 3.55f);
		tracer.bezierCubic(18.88f, 3.21f, 18.47f, 3.0f, 18.0f, 3.0f);
		tracer.lineTo(6f, 3f);
		tracer.bezierCubic(5.53f, 3.0f, 5.12f, 3.21f, 4.84f, 3.55f);
		tracer.lineTo(3.46f, 5.23f);
		tracer.bezierCubic(3.17f, 5.57f, 3.0f, 6.02f, 3.0f, 6.5f);
		tracer.lineTo(3f, 19f);
		tracer.bezierCubic(3.0f, 20.1f, 3.9f, 21.0f, 5.0f, 21.0f);
		tracer.lineTo(19f, 21f);
		tracer.bezierCubic(20.1f, 21.0f, 21.0f, 20.1f, 21.0f, 19.0f);
		tracer.lineTo(21f, 6.5f);
		tracer.bezierCubic(21.0f, 6.02f, 20.83f, 5.57f, 20.54f, 5.23f);
		tracer.close();
		tracer.moveTo(5.12f, 5f);
		tracer.lineTo(5.93f, 4f);
		tracer.lineTo(17.93f, 4f);
		tracer.lineTo(18.87f, 5f);
		tracer.lineTo(5.12f, 5f);
		tracer.close();
	};
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.fillPath(z, PATH, transform, color);
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
