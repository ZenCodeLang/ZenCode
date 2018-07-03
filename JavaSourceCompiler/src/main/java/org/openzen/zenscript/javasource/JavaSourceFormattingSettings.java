/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.formattershared.FormattingSettings;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFormattingSettings extends FormattingSettings {
	public final boolean showAnyInFunctionHeaders;
	public final boolean spaceBeforeLabelColon;
	public final boolean spaceAfterLabelColon;
	public final boolean bracketsAroundConditions;
	public final boolean ifElseForceBrackets;
	public final boolean ifElseAvoidBrackets;
	public final boolean loopForceBrackets;
	public final boolean loopAvoidBrackets;
	public final boolean tryCatchForceBrackets;
	public final boolean tryCatchAvoidBrackets;
	public final boolean ifSingleLineOnSameLine;
	public final boolean elseSingleLineOnSameLine;
	public final boolean loopSingleLineOnSameLine;
	public final boolean ifBracketOnSameLine;
	public final boolean elseBracketOnSameLine;
	public final boolean loopBracketOnSameLine;
	public final boolean tryCatchNewLine; // new line between } and catch or } and finally
	public final boolean tryCatchBracketOnSameLine;
	public final boolean classBracketOnSameLine;
	public final boolean functionBracketOnSameLine;
	
	private JavaSourceFormattingSettings(Builder builder) {
		super(builder);
		
		showAnyInFunctionHeaders = builder.showAnyInFunctionHeaders;
		spaceBeforeLabelColon = builder.spaceBeforeLabelColon;
		spaceAfterLabelColon = builder.spaceAfterLabelColon;
		bracketsAroundConditions = builder.bracketsAroundConditions;
		ifElseForceBrackets = builder.ifElseForceBrackets;
		ifElseAvoidBrackets = builder.ifElseAvoidBrackets;
		loopForceBrackets = builder.loopForceBrackets;
		loopAvoidBrackets = builder.loopAvoidBrackets;
		tryCatchForceBrackets = builder.tryCatchForceBrackets;
		tryCatchAvoidBrackets = builder.tryCatchAvoidBrackets;
		ifSingleLineOnSameLine = builder.ifSingleLineOnSameLine;
		elseSingleLineOnSameLine = builder.elseSingleLineOnSameLine;
		loopSingleLineOnSameLine = builder.loopSingleLineOnSameLine;
		ifBracketOnSameLine = builder.ifBracketOnSameLine;
		elseBracketOnSameLine = builder.elseBracketOnSameLine;
		loopBracketOnSameLine = builder.loopBracketOnSameLine;
		tryCatchNewLine = builder.tryCatchNewLine;
		tryCatchBracketOnSameLine = builder.tryCatchBracketOnSameLine;
		classBracketOnSameLine = builder.classBracketOnSameLine;
		functionBracketOnSameLine = builder.functionBracketOnSameLine;
	}
	
	public String getSingleLineSeparator(String indent, ParentStatementType position) {
		switch (position) {
			case NONE:
				return "\n" + indent;
				
			case IF:
			case IF_WITH_ELSE:
				if (ifSingleLineOnSameLine)
					return " ";
				else
					return "\n" + indent + this.indent;
				
			case ELSE:
				if (elseSingleLineOnSameLine)
					return " ";
				else
					return "\n" + indent + this.indent;
				
			case LOOP:
				if (loopSingleLineOnSameLine)
					return " ";
				else
					return "\n" + indent + this.indent;
				
			case TRY:
			case CATCH:
			case FINALLY:
				return "\n" + indent + this.indent;
			
			default:
				return "\n" + indent + this.indent;
		}
	}
	
	public String getBlockSeparator(String indent, ParentStatementType position) {
		switch (position) {
			case NONE:
				return "\n" + indent + "{";
				
			case IF:
			case IF_WITH_ELSE:
				if (ifBracketOnSameLine)
					return " {";
				else
					return "\n" + indent + "{";
				
			case ELSE:
				if (elseBracketOnSameLine)
					return " {";
				else
					return "\n" + indent + "{";
				
			case LOOP:
				if (loopBracketOnSameLine)
					return " {";
				else
					return "\n" + indent + "{";
			
			case TRY:
			case CATCH:
			case FINALLY:
				if (tryCatchBracketOnSameLine)
					return " {";
				else
					return "\n" + indent + "{";
			
			default:
				return "\n" + indent + this.indent;
		}
	}
	
	public static class Builder extends FormattingSettings.Builder<Builder> {
		private boolean showAnyInFunctionHeaders = false;
		private boolean useSingleQuotesForStrings = true;
		private boolean useTabs = false;
		private int spacesPerTab = 4;
		private boolean spaceBeforeLabelColon = true;
		private boolean spaceAfterLabelColon = false;
		private boolean bracketsAroundConditions = false;
		private boolean ifElseForceBrackets = false;
		private boolean ifElseAvoidBrackets = true;
		private boolean loopForceBrackets = false;
		private boolean loopAvoidBrackets = true;
		private boolean tryCatchForceBrackets = true;
		private boolean tryCatchAvoidBrackets = false;
		private boolean ifSingleLineOnSameLine = false;
		private boolean elseSingleLineOnSameLine = false;
		private boolean loopSingleLineOnSameLine = false;
		private boolean ifBracketOnSameLine = true;
		private boolean elseBracketOnSameLine = true;
		private boolean loopBracketOnSameLine = true;
		private boolean tryCatchNewLine = true;
		private boolean tryCatchBracketOnSameLine = true;
		private boolean classBracketOnSameLine = false;
		private boolean functionBracketOnSameLine = false;
		private boolean lambdaMethodOnSameLine = false;
		
		public Builder() {
			super(JavaSourceCommentFormatter::format);
		}
		
		public Builder showAnyInFunctionHeaders(boolean show) {
			showAnyInFunctionHeaders = show;
			return this;
		}
		
		public Builder useSingleQuotesForStrings(boolean single) {
			useSingleQuotesForStrings = single;
			return this;
		}
		
		public Builder useTabs(boolean tabs) {
			useTabs = tabs;
			return this;
		}
		
		public Builder spacesPerTabs(int spaces) {
			spacesPerTab = spaces;
			return this;
		}
		
		public Builder spaceBeforeLabelColon(boolean space) {
			spaceBeforeLabelColon = space;
			return this;
		}
		
		public Builder spaceAfterLabelColon(boolean space) {
			spaceAfterLabelColon = space;
			return this;
		}
		
		public Builder bracketsAroundConditions(boolean brackets) {
			bracketsAroundConditions = brackets;
			return this;
		}
		
		public Builder ifElseForceBrackets(boolean force) {
			ifElseForceBrackets = force;
			return this;
		}
		
		public Builder ifElseAvoidBrackets(boolean avoid) {
			ifElseAvoidBrackets = avoid;
			return this;
		}
		
		public Builder loopForceBrackets(boolean force) {
			loopForceBrackets = force;
			return this;
		}
		
		public Builder loopAvoidBrackets(boolean avoid) {
			loopAvoidBrackets = avoid;
			return this;
		}
		
		public Builder tryCatchForceBrackets(boolean force) {
			tryCatchForceBrackets = force;
			return this;
		}
		
		public Builder tryCatchAvoidBrackets(boolean avoid) {
			tryCatchAvoidBrackets = avoid;
			return this;
		}
		
		public Builder ifSingleLineOnSameLine(boolean sameLine) {
			ifSingleLineOnSameLine = sameLine;
			return this;
		}
		
		public Builder elseSingleLineOnSameLine(boolean sameLine) {
			elseSingleLineOnSameLine = sameLine;
			return this;
		}
		
		public Builder loopSingleLineOnSameLine(boolean sameLine) {
			loopSingleLineOnSameLine = sameLine;
			return this;
		}
		
		public Builder ifBracketOnSameLine(boolean sameLine) {
			ifBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder elseBracketOnSameLine(boolean sameLine) {
			elseBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder loopBracketOnSameLine(boolean sameLine) {
			loopBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder tryCatchNewLine(boolean newLine) {
			tryCatchNewLine = newLine;
			return this;
		}
		
		public Builder tryCatchBracketOnSameLine(boolean sameLine) {
			tryCatchBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder classBracketOnSameLine(boolean sameLine) {
			classBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder functionBracketOnSameLine(boolean sameLine) {
			functionBracketOnSameLine = sameLine;
			return this;
		}
		
		public Builder lambdaMethodOnSameLine(boolean sameLine) {
			lambdaMethodOnSameLine = sameLine;
			return this;
		}
		
		public JavaSourceFormattingSettings build() {
			return new JavaSourceFormattingSettings(this);
		}
	}
}
