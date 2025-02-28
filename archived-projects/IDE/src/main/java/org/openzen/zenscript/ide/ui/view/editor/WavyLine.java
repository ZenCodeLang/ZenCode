/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DPathTracer;

/**
 * @author Hoofdgebruiker
 */
public class WavyLine implements DPath {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final float stride;

	public WavyLine(int x, int y, int width, int height, float stride) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.stride = stride;
	}

	@Override
	public void trace(DPathTracer tracer) {
		tracer.moveTo(x, y);
		for (int i = 0; i < (width / stride); i++) {
			if ((i % 2) == 0) {
				tracer.bezierCubic(
						x + (i + 0.5f) * stride, y,
						x + (i + 0.5f) * stride, y + height,
						x + (i + 1) * stride, y + height);
			} else {
				tracer.bezierCubic(
						x + (i + 0.5f) * stride, y + height,
						x + (i + 0.5f) * stride, y,
						x + (i + 1) * stride, y);
			}
		}
	}
}
