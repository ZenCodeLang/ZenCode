/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import live.LiveArrayList;
import live.MutableLiveList;
import live.MutableLiveObject;
import live.SimpleLiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectBar {
	public final MutableLiveList<IDEAspectToolbar> toolbars = new LiveArrayList<>();
	public final MutableLiveObject<IDEAspectToolbar> active = new SimpleLiveObject<>(null);
}
