/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDETarget {
	String getName();
	
	boolean canBuild();
	
	boolean canRun();
	
	void build();
	
	void run();
}
