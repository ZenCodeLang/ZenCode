/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import org.openzen.zenscript.codemodel.serialization.EncodingOperation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hoofdgebruiker
 */
public class EncodingStage {
	private final List<EncodingOperation> operations = new ArrayList<>();
	private boolean locked = false;
	
	public void encode(ModuleEncoder encoder) {
		locked = true;
		for (EncodingOperation operation : operations) {
			operation.encode(encoder);
		}
	}
	
	public void enqueue(EncodingOperation operation) {
		if (locked)
			throw new IllegalStateException("Encoding stage is locked!");
		
		operations.add(operation);
	}
}
