/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 *
 * @author Hoofdgebruiker
 */
public class DDimensionPreferences {
	public static final DDimensionPreferences EMPTY = new DDimensionPreferences(0, 0);
	
	public final int minimumWidth;
	public final int minimumHeight;
	public final int preferredWidth;
	public final int preferredHeight;
	public final int maximumWidth;
	public final int maximumHeight;
	
	public DDimensionPreferences(int preferredWidth, int preferredHeight) {
		this.minimumWidth = 0;
		this.minimumHeight = 0;
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
		this.maximumWidth = 1000000;
		this.maximumHeight = 1000000;
	}
	
	public DDimensionPreferences(int minimumWidth, int minimumHeight, int preferredWidth, int preferredHeight, int maximumWidth, int maximumHeight) {
		this.minimumWidth = minimumWidth;
		this.minimumHeight = minimumHeight;
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
		this.maximumWidth = maximumWidth;
		this.maximumHeight = maximumHeight;
	}
}
