/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface AnnotationDefinition {
	public String getAnnotationName();
	
	public List<FunctionHeader> getInitializers(BaseScope scope);
	
	public ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope);
	
	public ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope);
	
	public ExpressionScope getScopeForStatement(Statement statement, StatementScope scope);
	
	public ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope);
	
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments);
	
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments);
	
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments);
	
	public Annotation createForParameter(CodePosition position, CallArguments arguments);
}
