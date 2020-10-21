/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.expression.ParsedExpressionBinary;
import org.openzen.zenscript.parser.expression.ParsedExpressionString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoofdgebruiker and kindlich
 */
public class EscapableBracketParser implements BracketExpressionParser {
    private final FunctionalMemberRef method;
    private final TypeID targetType;

    public EscapableBracketParser(GlobalTypeRegistry registry, FunctionalMemberRef method) {
        if (!method.isStatic())
            throw new IllegalArgumentException("Method must be static");
        if (method.getHeader().getNumberOfTypeParameters() > 0)
            throw new IllegalArgumentException("Method cannot have type parameters");

        this.method = method;
        this.targetType = registry.getForDefinition(method.getTarget().definition);
    }

    @Override
    public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
        StringBuilder string = new StringBuilder();

        //This list will contain the BEP calls
        //If this is only a normal BEP, then it will contain exactly one String ParsedExpression.
        final List<ParsedExpression> expressionList = new ArrayList<>();

        while (tokens.optional(ZSTokenType.T_GREATER) == null) {
            ZSToken next = tokens.next();

            if (next.type != ZSTokenType.T_DOLLAR) {
                string.append(next.content);
                string.append(tokens.getLastWhitespace());
                continue;
            }

            //We found a $, now check that it has a { directly after it.
            final String ws = tokens.getLastWhitespace();
            if (!ws.isEmpty()) {
                //$  {..} is not ${..} so we print it as literal
                string.append(next.content).append(ws);
                continue;
            }

            next = tokens.next();
            //Now we check if it is a {
            if (next.type == ZSTokenType.T_AOPEN) {
                if (string.length() != 0) {
                    expressionList.add(new ParsedExpressionString(position, string.toString(), false));
                    string = new StringBuilder();
                }
                expressionList.add(ParsedExpression.parse(tokens));
                tokens.required(ZSTokenType.T_ACLOSE, "} expected.");
                string.append(tokens.getLastWhitespace());
            } else {
                //No { after the $, so we treat them both as literal
                string.append("$").append(ws); //Technically, the whitespace here is empty, but let's be sure
                string.append(next.content).append(tokens.getLastWhitespace());
            }

        }

        if (string.length() != 0) {
            expressionList.add(new ParsedExpressionString(position, string.toString(), false));
        }

        return new StaticMethodCallExpression(position, expressionList);
    }

    private class StaticMethodCallExpression extends ParsedExpression {

        private final ParsedExpression call;

        public StaticMethodCallExpression(CodePosition position, List<ParsedExpression> expressions) {
            super(position);
            ParsedExpression p = null;
            for (ParsedExpression expression : expressions) {
                p = p == null ? expression : new ParsedExpressionBinary(expression.position, p, expression, OperatorType.ADD);
            }

            this.call = p;
        }

        @Override
        public IPartialExpression compile(ExpressionScope scope) throws CompileException {
            final Expression methodCall = call.compile(scope.withHint(StringTypeID.AUTO)).eval();
            final CallArguments arguments = new CallArguments(methodCall);
            return new CallStaticExpression(position, targetType, method, method.getHeader(), arguments);
        }

        @Override
        public boolean hasStrongType() {
            return true;
        }
    }
}
