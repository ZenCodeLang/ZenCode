/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSizing {
	public static MutableLiveObject<DSizing> create() {
		return new SimpleLiveObject<>(DSizing.EMPTY);
	}
	
	public static final DSizing EMPTY = new DSizing(0, 0);
	
	public final int minimumWidth;
	public final int minimumHeight;
	public final int preferredWidth;
	public final int preferredHeight;
	public final int maximumWidth;
	public final int maximumHeight;
	
	public DSizing(int preferredWidth, int preferredHeight) {
		this.minimumWidth = 0;
		this.minimumHeight = 0;
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
		this.maximumWidth = 1000000;
		this.maximumHeight = 1000000;
	}
	
	public DSizing(int minimumWidth, int minimumHeight, int preferredWidth, int preferredHeight, int maximumWidth, int maximumHeight) {
		this.minimumWidth = minimumWidth;
		this.minimumHeight = minimumHeight;
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
		this.maximumWidth = maximumWidth;
		this.maximumHeight = maximumHeight;
	}
}
