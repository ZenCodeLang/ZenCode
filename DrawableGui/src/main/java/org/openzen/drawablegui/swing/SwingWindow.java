/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import javax.swing.JFrame;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.border.DCustomWindowBorder;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveBool;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public final class SwingWindow extends JFrame implements WindowListener, WindowStateListener, DUIWindow {
	public final SwingRoot swingComponent;
	private final boolean noTitleBar;
	private final SimpleLiveObject<State> state = new SimpleLiveObject<>(State.NORMAL);
	private final SimpleLiveBool active = new SimpleLiveBool(true);
	private final MutableLiveObject<DIRectangle> bounds = new SimpleLiveObject<>(DIRectangle.EMPTY);
	
	public SwingWindow(String title, DComponent root, boolean noTitleBar) {
		super(title);
		this.noTitleBar = noTitleBar;
		
		if (noTitleBar) {
		    setUndecorated(true);
			root = new DCustomWindowBorder(DStyleClass.EMPTY, root);
			setBackground(new Color(0, 0, 0, 0));
		}
		
		addWindowListener(this);
		addWindowStateListener(this);
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
		
		getContentPane().add(swingComponent = new SwingRoot(root), BorderLayout.CENTER);
		swingComponent.setWindow(this);
		swingComponent.requestFocusInWindow();
		
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
		return !noTitleBar;
	}

	@Override
	public void close() {
		System.exit(0);
	}

	@Override
	public void maximize() {
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	@Override
	public void restore() {
		setExtendedState(JFrame.NORMAL);
	}

	@Override
	public void minimize() {
		setExtendedState(JFrame.ICONIFIED);
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
		SwingWindow result = new SwingWindow(title, component, false);
		result.setResizable(false);
		
		DSizing size = component.getSizing().getValue();
		result.setLocation(
				getX() + (getWidth() - size.preferredWidth) / 2,
				getY() + (getHeight() - size.preferredHeight) / 2);
		result.setSize(size.preferredWidth, size.preferredHeight);
		return result;
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
		state.setValue(getStateFromWindowState());
	}
	
	private void updateBounds() {
		state.setValue(getStateFromWindowState());
		bounds.setValue(new DIRectangle(getX(), getY(), getWidth(), getHeight()));
	}
	
	private State getStateFromWindowState() {
		switch (getExtendedState()) {
			case NORMAL:
				return State.NORMAL;
			case ICONIFIED:
				return State.MINIMIZED;
			case MAXIMIZED_HORIZ:
			case MAXIMIZED_VERT:
			case MAXIMIZED_BOTH:
				return State.MAXIMIZED;
			default:
				return State.NORMAL;
		}
	}
}
