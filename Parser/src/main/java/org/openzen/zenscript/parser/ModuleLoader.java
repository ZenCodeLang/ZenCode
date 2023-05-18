package org.openzen.zenscript.parser;

import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.logger.ParserLogger;

public interface ModuleLoader {

	SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies, FunctionParameter[] scriptParameters, ZSPackage pkg, ParserLogger logger) throws ParseException;
}