/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleRegistry {
	private final Map<String, ModuleReference> modules = new HashMap<>();
	private final Set<String> loading = new HashSet<>();
	private final Stack<String> loadingStack = new Stack<>();
	
	public void register(ModuleReference reference) {
		modules.put(reference.getModuleName(), reference);
	}
	
	public SemanticModule load(String name) throws CompileException {
		if (loading.contains(name)) {
			StringBuilder sequence = new StringBuilder();
			boolean first = true;
			for (String item : loadingStack) {
				if (first)
					first = false;
				else
					sequence.append(" -> ");
				sequence.append(item);
			}
			throw new IllegalStateException("Circular reference when loading module " + name + ": " + sequence);
		}
		
		loadingStack.push(name);
		loading.add(name);
		
		if (!modules.containsKey(name))
			throw new CompileException(CodePosition.META, CompileExceptionCode.NO_SUCH_MODULE, "Module not found: " + name);
		SemanticModule result = modules.get(name).load(this);
		
		loadingStack.pop();
		loading.remove(name);
		
		return result;
	}
}
