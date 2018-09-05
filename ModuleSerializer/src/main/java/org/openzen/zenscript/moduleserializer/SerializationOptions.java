/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

/**
 *
 * @author Hoofdgebruiker
 */
public class SerializationOptions {
	public final boolean positions;
	public final boolean expressionPositions;
	public final boolean positionOffsets;
	public final boolean typeParameterNames;
	
	public SerializationOptions(
			boolean positions,
			boolean expressionPositions,
			boolean positionOffsets,
			boolean typeParameterNames) {
		this.positions = positions;
		this.expressionPositions = expressionPositions;
		this.positionOffsets = positionOffsets;
		this.typeParameterNames = typeParameterNames;
	}
}
