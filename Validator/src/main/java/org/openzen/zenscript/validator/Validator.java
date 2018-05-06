/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.validator.analysis.StatementScope;
import org.openzen.zenscript.validator.visitors.DefinitionValidator;
import org.openzen.zenscript.validator.visitors.StatementValidator;

/**
 *
 * @author Hoofdgebruiker
 */
public class Validator {
	private final List<ValidationLogEntry> log = new ArrayList<>();
	private boolean hasErrors = false;
	
	private final DefinitionValidator definitionValidator = new DefinitionValidator(this);
	
	public List<ValidationLogEntry> getLog() {
		return Collections.unmodifiableList(log);
	}
	
	public boolean validate(ScriptBlock script) {
		StatementValidator statementValidator = new StatementValidator(this, new ScriptScope());
		boolean isValid = true;
		for (Statement statement : script.statements) {
			isValid &= statement.accept(statementValidator);
		}
		return isValid;
	}
	
	public boolean validate(HighLevelDefinition definition) {
		return definition.accept(definitionValidator);
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
	
	public void logError(ValidationLogEntry.Code code, CodePosition position, String message) {
		log.add(new ValidationLogEntry(ValidationLogEntry.Kind.ERROR, code, position, message));
		hasErrors = true;
	}
	
	public void logWarning(ValidationLogEntry.Code code, CodePosition position, String message) {
		log.add(new ValidationLogEntry(ValidationLogEntry.Kind.WARNING, code, position, message));
	}
	
	private class ScriptScope implements StatementScope {

		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isStatic() {
			return true;
		}

		@Override
		public FunctionHeader getFunctionHeader() {
			return null;
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return null;
		}
	}
}
