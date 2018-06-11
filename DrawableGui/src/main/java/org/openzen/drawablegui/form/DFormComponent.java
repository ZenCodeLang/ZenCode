/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.form;

import org.openzen.drawablegui.DComponent;

/**
 *
 * @author Hoofdgebruiker
 */
public class DFormComponent {
	public final String label;
	public final DComponent component;
	
	public DFormComponent(String label, DComponent component) {
		this.label = label;
		this.component = component;
	}
}
