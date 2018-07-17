/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class OutputView implements DComponent {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final DStyleClass styleClass;
	private final LiveList<OutputLine> lines;
	
	private DDrawSurface surface;
	private int z;
	private DIRectangle bounds;
	private OutputViewStyle style;
	
	private DDrawnShape shape;
	
	public OutputView(DStyleClass styleClass, LiveList<OutputLine> lines) {
		this.styleClass = styleClass;
		this.lines = lines;
	}

	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("outputview", styleClass);
		style = new OutputViewStyle(surface.getStylesheet(path));
	}
	
	@Override
	public void unmount() {
		if (shape != null) {
			shape.close();
			shape = null;
		}
	}

	@Override
	public MutableLiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		shape = surface.shadowPath(z, style.shape.instance(bounds), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
	}

	@Override
	public void close() {
		unmount();
	}
}
