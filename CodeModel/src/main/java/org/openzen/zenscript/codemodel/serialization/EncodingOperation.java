/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

/**
 *
 * @author Hoofdgebruiker
 */
public interface EncodingOperation {
	void encode(CodeSerializationOutput output);
}
