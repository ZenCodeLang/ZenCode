/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEDockWindow {
	public final LiveObject<IDESourceFile> currentSourceFile = new SimpleLiveObject<>(null);
}
