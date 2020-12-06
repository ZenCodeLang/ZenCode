/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.util.HashMap;
import java.util.Map;

import live.LiveArrayList;
import live.LiveList;
import org.openzen.zenscript.ide.host.IDECodeError;
import org.openzen.zenscript.ide.host.IDECompileState;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 * @author Hoofdgebruiker
 */
public class LocalCompileState implements IDECompileState {
	private final Map<IDESourceFile, LiveArrayList<IDECodeError>> errors = new HashMap<>();

	public void addError(IDESourceFile file, IDECodeError error) {
		if (!errors.containsKey(file))
			errors.put(file, new LiveArrayList<>());

		errors.get(file).add(error);
	}

	@Override
	public LiveList<IDECodeError> getErrors(IDESourceFile file) {
		return errors.getOrDefault(file, new LiveArrayList<>());
	}
}
