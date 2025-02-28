/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.openzen.drawablegui.DClipboard;

/**
 * @author Hoofdgebruiker
 */
public class JavaClipboard implements DClipboard {
	private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	@Override
	public void copyAsString(String value) {
		StringSelection stringSelection = new StringSelection(value);
		clipboard.setContents(stringSelection, null);
	}

	@Override
	public String getAsString() {
		Transferable contents = clipboard.getContents(null);
		if (contents == null)
			return null;

		if (!contents.isDataFlavorSupported(DataFlavor.stringFlavor))
			return null;

		try {
			return (String) contents.getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
