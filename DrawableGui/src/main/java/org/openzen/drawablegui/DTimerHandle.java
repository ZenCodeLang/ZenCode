/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.io.Closeable;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DTimerHandle extends Closeable {
	@Override
	public void close();
}
