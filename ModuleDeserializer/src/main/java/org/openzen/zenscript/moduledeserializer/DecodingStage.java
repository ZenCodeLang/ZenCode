/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zenscript.codemodel.serialization.DecodingOperation;
import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.serialization.DeserializationException;

/**
 *
 * @author Hoofdgebruiker
 */
public class DecodingStage {
	private final List<DecodingOperation> operations = new ArrayList<>();
	private boolean locked = false;
	
	public void decode(CodeReader encoder) throws DeserializationException {
		locked = true;
		for (DecodingOperation operation : operations) {
			operation.decode(encoder);
		}
	}
	
	public void enqueue(DecodingOperation operation) {
		if (locked)
			throw new IllegalStateException("Encoding stage is locked!");
		
		operations.add(operation);
	}
}
