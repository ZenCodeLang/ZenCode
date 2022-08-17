/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

/**
 * @author Hoofdgebruiker
 */
public interface JavaCompileSpace {
	void register(JavaCompiledModule module);

	JavaCompiledModule getCompiled(Module module);
}
