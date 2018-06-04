/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.Objects;

/**
 *
 * @author Hoofdgebruiker
 */
public class DFont {
	public final DFontFamily family;
	public final boolean bold;
	public final boolean italic;
	public final boolean underline;
	public final int size;
	
	public Object cached;
	
	public DFont(DFontFamily family, boolean bold, boolean italic, boolean underline, int size) {
		this.family = family;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.size = size;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.family);
		hash = 29 * hash + (this.bold ? 1 : 0);
		hash = 29 * hash + (this.italic ? 1 : 0);
		hash = 29 * hash + (this.underline ? 1 : 0);
		hash = 29 * hash + Float.floatToIntBits(this.size);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DFont other = (DFont) obj;
		if (this.bold != other.bold) {
			return false;
		}
		if (this.italic != other.italic) {
			return false;
		}
		if (this.underline != other.underline) {
			return false;
		}
		if (this.size != other.size)
			return false;
		if (this.family != other.family) {
			return false;
		}
		return true;
	}
}
