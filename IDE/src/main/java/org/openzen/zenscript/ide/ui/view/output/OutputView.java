/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

import org.openzen.drawablegui.live.LiveList;

/**
 *
 * @author Hoofdgebruiker
 */
public class OutputView {
	public final LiveList<OutputLine> lines;
	
	public OutputView(LiveList<OutputLine> lines) {
		this.lines = lines;
	}
}
