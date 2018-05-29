/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;

public class ModuleIcon implements DDrawable {
	public static final ModuleIcon INSTANCE = new ModuleIcon();
	
	private ModuleIcon() {}
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform) {
		ColorableModuleIcon.INSTANCE.draw(canvas, transform, 0xFF000000);
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
