/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.host.IDEPropertyStore;

/**
 * @author Hoofdgebruiker
 */
public class FilePropertyStore implements IDEPropertyStore {
	private final File file;
	private JSONObject data;

	public FilePropertyStore(File file) {
		this.file = file;

		if (!file.exists()) {
			data = new JSONObject();
			save();
		} else {
			try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.UTF_8)) {
				data = new JSONObject(new JSONTokener(input));
			} catch (IOException ex) {
				data = new JSONObject();
			}
		}
	}

	@Override
	public IDEPropertyDirectory getRoot() {
		return new JSONPropertyDirectory(data);
	}

	@Override
	public void save() {
		try (OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			output.write(data.toString(2));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
