/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveList;
import org.openzen.drawablegui.live.SimpleLiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectBar {
	public final MutableLiveList<IDEAspectToolbar> toolbars = new LiveArrayList<>();
	public final LiveObject<IDEAspectToolbar> active = new SimpleLiveObject<>(null);
}
