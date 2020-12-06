package org.openzen.zenscript.compiler;

import java.io.File;

import org.json.JSONObject;

public interface TargetType {
	Target create(File projectDir, JSONObject definition);
}
