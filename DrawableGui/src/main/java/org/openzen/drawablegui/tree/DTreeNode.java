/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.MutableLiveBool;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DTreeNode<N extends DTreeNode<N>> {
	void close();
	
	DColorableIcon getIcon();
	
	String getTitle();
	
	LiveList<N> getChildren();
	
	MutableLiveBool isCollapsed();
	
	boolean isLeaf();
	
	default void onMouseClick(DMouseEvent e) {}
}
