/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Hoofdgebruiker
 */
public class JSONUtils {
	private JSONUtils() {}
	
	public static JSONObject load(File file) throws IOException, JSONException {
		try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
			return new JSONObject(new JSONTokener(input));
		}
	}
}
