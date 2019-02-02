/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import java.util.function.Consumer;
import live.LiveObject;
import org.openzen.zenscript.ide.ui.view.output.OutputLine;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDETarget {
	String getName();
	
	boolean canBuild();
	
	boolean canRun();
	
	LiveObject<IDECompileState> load();
	
	void build(Consumer<OutputLine> output);
	
	void run(Consumer<OutputLine> output);
}
