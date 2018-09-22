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
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCatchClause {
	public final CodePosition position;
	public final String exceptionName;
	public final IParsedType exceptionType;
	public final ParsedStatement content;
	
	public ParsedCatchClause(CodePosition position, String exceptionName, IParsedType exceptionType, ParsedStatement content) {
		this.position = position;
		this.exceptionName = exceptionName;
		this.exceptionType = exceptionType;
		this.content = content;
	}
	
	public CatchClause compile(StatementScope scope) {
		VarStatement exceptionVariable = new VarStatement(position, exceptionName, exceptionType.compile(scope), null, true);
		CatchScope localScope = new CatchScope(scope, exceptionVariable);
		return new CatchClause(position, exceptionVariable, content.compile(localScope));
	}
	
	private class CatchScope extends StatementScope {
		private final StatementScope outer;
		private final VarStatement exceptionVariable;
		
		public CatchScope(StatementScope outer, VarStatement exceptionVariable) {
			this.outer = outer;
			this.exceptionVariable = exceptionVariable;
		}
	
		@Override
		public IPartialExpression get(CodePosition position, GenericName name) {
			if (name.hasNoArguments() && exceptionVariable.name.equals(name.name))
				return new GetLocalVariableExpression(position, exceptionVariable);

			return outer.get(position, name);
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name, StorageTag storage) {
			return outer.getType(position, name, storage);
		}
		
		@Override
		public StorageTag getStorageTag(CodePosition position, String name, String[] arguments) {
			return outer.getStorageTag(position, name, arguments);
		}

		@Override
		public LoopStatement getLoop(String name) {
			return outer.getLoop(name);
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

		@Override
		public TypeMemberPreparer getPreparer() {
			return outer.getPreparer();
		}

		@Override
		public GenericMapper getLocalTypeParameters() {
			return outer.getLocalTypeParameters();
		}
	}
}
