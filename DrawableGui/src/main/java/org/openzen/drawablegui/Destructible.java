/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.List;

/**
 *
 * @author Hoofdgebruiker
 */
public interface Destructible extends AutoCloseable {
	public static <T extends Destructible> void close(List<T> list) {
		for (T item : list)
			item.close();
	}
	
	@Override
	public void close();
}
