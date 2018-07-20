/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.List;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementSwitch extends ParsedStatement {
	private final String name;
	private final ParsedExpression value;
	private final List<ParsedSwitchCase> cases;
	
	public ParsedStatementSwitch(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name, ParsedExpression value, List<ParsedSwitchCase> cases) {
		super(position, annotations, whitespace);
		
		this.name = name;
		this.value = value;
		this.cases = cases;
	}

	@Override
	public Statement compile(StatementScope scope) {
		SwitchStatement result = new SwitchStatement(position, name, value.compile(new ExpressionScope(scope)).eval());
		SwitchScope innerScope = new SwitchScope(scope, result);
		
		for (ParsedSwitchCase switchCase : cases) {
			result.cases.add(switchCase.compile(result.value.type, innerScope));
		}
		
		return result;
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		ITypeID result = null;
		ITypeID valueType = value.precompileForType(new ExpressionScope(scope), precompileState);
		for (ParsedSwitchCase switchCase : cases) {
			result = union(scope, result, switchCase.precompileForResultType(valueType, scope, precompileState));
		}
		return result;
	}
	
	private class SwitchScope extends StatementScope {
		private final StatementScope outer;
		private final SwitchStatement target;
		
		public SwitchScope(StatementScope outer, SwitchStatement target) {
			this.outer = outer;
			this.target = target;
		}
	
		@Override
		public IPartialExpression get(CodePosition position, GenericName name) {
			IPartialExpression result = super.get(position, name);
			if (result == null) {
				return outer.get(position, name);
			} else {
				return result;
			}
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name) {
			return outer.getType(position, name);
		}

		@Override
		public LoopStatement getLoop(String name) {
			return name == null || name.equals(target.label) ? target : outer.getLoop(name);
		}

		@Override
		public FunctionHeader getFunctionHeader() {
			return outer.getFunctionHeader();
		}

		@Override
		public ITypeID getThisType() {
			return outer.getThisType();
		}

		@Override
		public Function<CodePosition, Expression> getDollar() {
			return outer.getDollar();
		}

		@Override
		public IPartialExpression getOuterInstance(CodePosition position) {
			return outer.getOuterInstance(position);
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
