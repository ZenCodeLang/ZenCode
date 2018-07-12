/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MutableLiveList<T> extends LiveList<T> {
	void add(T value);
	
	void add(int index, T value);
	
	void set(int index, T value);
	
	void remove(int index);
	
	void remove(T value);
}
