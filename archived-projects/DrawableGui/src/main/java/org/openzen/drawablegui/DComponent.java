/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import live.LiveObject;

/**
 * @author Hoofdgebruiker
 */
public interface DComponent extends Destructible {
	void mount(DComponentContext context);

	void unmount();

	LiveObject<DSizing> getSizing();

	DIRectangle getBounds();

	void setBounds(DIRectangle bounds);

	int getBaselineY();

	default void onMouseEnter(DMouseEvent e) {
	}

	default void onMouseExit(DMouseEvent e) {
	}

	default void onMouseMove(DMouseEvent e) {
	}

	default void onMouseDrag(DMouseEvent e) {
	}

	default void onMouseClick(DMouseEvent e) {
	}

	default void onMouseDown(DMouseEvent e) {
	}

	default void onMouseRelease(DMouseEvent e) {
	}

	default void onMouseScroll(DMouseEvent e) {
	}

	default void onFocusLost() {
	}

	default void onFocusGained() {
	}

	default void onKeyTyped(DKeyEvent e) {
	}

	default void onKeyPressed(DKeyEvent e) {
	}

	default void onKeyReleased(DKeyEvent e) {
	}
}
