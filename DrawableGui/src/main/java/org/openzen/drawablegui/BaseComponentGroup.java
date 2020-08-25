/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseComponentGroup implements DComponent {
	private DComponent hovering = null;
	
	protected abstract void forEachChild(Consumer<DComponent> children);
	
	protected abstract DComponent findChild(Predicate<DComponent> predicate);
	
	protected void onComponentRemoved(DComponent component) {
		if (hovering == component)
			hovering = null;
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		DComponent target = getComponent(e.x, e.y);
		setHovering(target, e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		setHovering(null, e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		DComponent target = getComponent(e.x, e.y);
		if (target == hovering) {
			if (target != null)
				target.onMouseMove(e);
		} else {
			setHovering(target, e);
		}
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDrag(e);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseClick(e);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseDown(e);
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseRelease(e);
		onMouseMove(e);
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		if (hovering != null)
			hovering.onMouseScroll(e);
	}
	
	private void setHovering(DComponent component, DMouseEvent e) {
		if (component == hovering)
			return;
		
		if (hovering != null)
			hovering.onMouseExit(e);
		hovering = component;
		if (hovering != null)
			hovering.onMouseEnter(e);
	}
	
	private DComponent getComponent(int x, int y) {
		return findChild(child -> child.getBounds().contains(x, y));
	}
}
