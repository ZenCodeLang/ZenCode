/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import javax.swing.JWindow;

import live.ImmutableLiveObject;
import live.LiveBool;
import live.LiveObject;
import live.MutableLiveObject;
import live.SimpleLiveBool;
import live.SimpleLiveObject;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;

/**
 * @author Hoofdgebruiker
 */
public final class SwingInlineWindow extends JWindow implements WindowListener, WindowStateListener, DUIWindow {
	public final SwingRoot swingComponent;
	private final LiveObject<State> state = new ImmutableLiveObject<>(State.NORMAL);
	private final SimpleLiveBool active = new SimpleLiveBool(true);
	private final MutableLiveObject<DIRectangle> bounds = new SimpleLiveObject<>(DIRectangle.EMPTY);

	public SwingInlineWindow(SwingWindow owner, String title, DComponent root) {
		super(owner);

		addWindowListener(this);
		addWindowStateListener(this);

		getContentPane().add(swingComponent = new SwingRoot(root), BorderLayout.CENTER);
		swingComponent.setWindow(this);
		swingComponent.requestFocusInWindow();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent componentEvent) {
				updateBounds();
			}

			@Override
			public void componentResized(ComponentEvent componentEvent) {
				updateBounds();
			}
		});
	}

	@Override
	public DUIContext getContext() {
		return swingComponent.context;
	}

	@Override
	public LiveObject<DIRectangle> getWindowBounds() {
		return bounds;
	}

	@Override
	public boolean hasTitleBar() {
		return false;
	}

	@Override
	public void close() {
		dispose();
	}

	@Override
	public void maximize() {
		// cannot maximize
	}

	@Override
	public void restore() {
		// cannot restore
	}

	@Override
	public void minimize() {
		// cannot minimize
	}

	@Override
	public LiveObject<State> getWindowState() {
		return state;
	}

	@Override
	public LiveBool getActive() {
		return active;
	}

	@Override
	public void focus(DComponent component) {
		swingComponent.focus(component);
	}

	@Override
	public DUIWindow openModal(String title, DComponent component) {
		throw new IllegalArgumentException("Cannot open a modal from an inline window!");
	}

	@Override
	public void windowOpened(WindowEvent e) {
		updateBounds();
	}

	@Override
	public void windowClosing(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {
		active.setValue(true);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		active.setValue(false);
	}

	@Override
	public void windowStateChanged(WindowEvent e) {

	}

	private void updateBounds() {
		bounds.setValue(new DIRectangle(getX(), getY(), getWidth(), getHeight()));
	}
}
