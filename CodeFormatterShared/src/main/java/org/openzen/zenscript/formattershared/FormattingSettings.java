package org.openzen.zenscript.formattershared;

import stdlib.Chars;

public class FormattingSettings {
	public final boolean useTabs; // use tabs instead of spaces
	public final int spacesPerTab; // number of spaces per tab
	public final String indent;
	public final CommentFormatter commentFormatter;
	
	protected FormattingSettings(Builder builder) {
		useTabs = builder.useTabs;
		spacesPerTab = builder.spacesPerTab;
		commentFormatter = builder.commentFormatter;
		
		if (useTabs) {
			indent = "\t";
		} else {
			indent = Chars.times(' ', spacesPerTab);
		}
	}
	
	public static class Builder<T extends Builder<T>> {
		private final CommentFormatter commentFormatter;
		private boolean useTabs = false;
		private int spacesPerTab = 4;
		protected T instance;
		
		public Builder(CommentFormatter commentFormatter) {
			this.commentFormatter = commentFormatter;
		}
		
		public T useTabs(boolean tabs) {
			useTabs = tabs;
			return instance;
		}
		
		public T spacesPerTabs(int spaces) {
			spacesPerTab = spaces;
			return instance;
		}
		
		public FormattingSettings build() {
			return new FormattingSettings(this);
		}
	}
}
