/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatementContext extends TypeContext {
	private final List<VarStatement> variables = new ArrayList<>();
	private final LoopStatement[] loops;
	private final FunctionHeader header;
	
	public StatementContext() {
		super(TypeParameter.NONE);
		loops = LoopStatement.NONE;
		header = null;
	}
	
	public StatementContext(FunctionHeader header) {
		super(header.typeParameters);
		loops = LoopStatement.NONE;
		this.header = header;
	}
	
	public StatementContext(TypeContext outer) {
		super(outer.parameters);
		loops = LoopStatement.NONE;
		header = null;
	}
	
	public StatementContext(TypeContext outer, FunctionHeader header) {
		super(outer, header.typeParameters);
		loops = LoopStatement.NONE;
		this.header = header;
	}
	
	public StatementContext(StatementContext outer) {
		super(outer.parameters);
		
		variables.addAll(outer.variables);
		loops = outer.loops;
		header = outer.header;
	}
	
	public StatementContext(StatementContext outer, LoopStatement loop) {
		super(outer.parameters);
		
		variables.addAll(outer.variables);
		loops = Arrays.copyOf(outer.loops, outer.loops.length + 1);
		loops[loops.length - 1] = loop;
		header = outer.header;
	}
	
	public void add(VarStatement variable) {
		variables.add(variable);
	}
	
	public int getVariableId(VarStatement variable) {
		return variables.indexOf(variable);
	}
	
	public int getLoopId(LoopStatement loop) {
		for (int i = 0; i < loops.length; i++)
			if (loops[i] == loop)
				return i;
		
		return -1;
	}
	
	public int getParameterIndex(FunctionParameter parameter) {
		if (header == null)
			return -1;
		
		for (int i = 0; i < header.parameters.length; i++)
			if (header.parameters[i] == parameter)
				return i;
		
		return -1;
	}
}
