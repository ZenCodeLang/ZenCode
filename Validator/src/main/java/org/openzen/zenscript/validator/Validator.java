/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.validator.analysis.StatementScope;
import org.openzen.zenscript.validator.logger.ValidatorLogger;
import org.openzen.zenscript.validator.visitors.DefinitionValidator;
import org.openzen.zenscript.validator.visitors.StatementValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hoofdgebruiker
 */
public class Validator {
	public final GlobalTypeRegistry registry;
	public final List<ExpansionDefinition> expansions;
	public final AnnotationDefinition[] annotations;
	private final List<ValidationLogEntry> log = new ArrayList<>();
	private final DefinitionValidator definitionValidator = new DefinitionValidator(this);
	private boolean hasErrors = false;

	public Validator(GlobalTypeRegistry registry, List<ExpansionDefinition> expansions, AnnotationDefinition[] annotations) {
		this.registry = registry;
		this.expansions = expansions;
		this.annotations = annotations;
	}

	public static SemanticModule validate(SemanticModule module, ValidatorLogger logger) {
		if (module.state != SemanticModule.State.NORMALIZED)
			throw new IllegalStateException("Module is not yet normalized");

		Validator validator = new Validator(module.registry, module.expansions, module.annotations);
		for (ScriptBlock script : module.scripts) {
			validator.validate(script);
		}
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			validator.validate(definition);
		}

		for (ValidationLogEntry entry : validator.getLog()) {
			logger.logValidationLogEntry(entry);
		}

		SemanticModule.State state = validator.hasErrors() ? SemanticModule.State.INVALID : SemanticModule.State.VALIDATED;
		return new SemanticModule(
				module.module,
				module.dependencies,
				module.parameters,
				state,
				module.rootPackage,
				module.modulePackage,
				module.definitions,
				module.scripts,
				module.registry,
				module.expansions,
				module.annotations,
				logger);
	}

	public List<ValidationLogEntry> getLog() {
		return Collections.unmodifiableList(log);
	}

	public void validate(ScriptBlock script) {
		StatementValidator statementValidator = new StatementValidator(this, new ScriptScope(new AccessScope(script.module, null)));
		for (Statement statement : script.statements) {
			statement.accept(statementValidator);
		}
	}

	public void validate(HighLevelDefinition definition) {
		definition.accept(definitionValidator);
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
		private final AccessScope access;

		public ScriptScope(AccessScope access) {
			this.access = access;
		}

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

		@Override
		public AccessScope getAccessScope() {
			return access;
		}
	}
}
