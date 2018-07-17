/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawTarget;

/**
 *
 * @author Hoofdgebruiker
 */
public class DCompositeIcon implements DColorableIcon {
	private final DColorableIcon base;
	private final DColorableIcon addition;
	private final DTransform2D additionTransform;
	
	public DCompositeIcon(DColorableIcon base, DColorableIcon addition, DTransform2D additionTransform) {
		this.base = base;
		this.addition = addition;
		this.additionTransform = additionTransform;
	}

	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		base.draw(canvas, transform, color);
		addition.draw(canvas, transform.mul(additionTransform), color);
	}
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		base.draw(target, z, transform, color);
		addition.draw(target, z + 1, transform.mul(additionTransform), color);
	}

	@Override
	public float getNominalWidth() {
		return base.getNominalWidth();
	}

	@Override
	public float getNominalHeight() {
		return base.getNominalHeight();
	}
}
