package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.formattershared.FormattingSettings;

public class RustSourceFormattingSettings extends FormattingSettings {
	private RustSourceFormattingSettings(Builder builder) {
		super(builder);
	}

	public static class Builder extends FormattingSettings.Builder<Builder> {
		public Builder() {
			super(RustSourceCommentFormatter::format);
		}

		public RustSourceFormattingSettings build() {
			return new RustSourceFormattingSettings(this);
		}
	}
}
