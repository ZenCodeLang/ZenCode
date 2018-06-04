/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.io.File;
import org.json.JSONObject;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TargetType {
	public Target create(File projectDir, JSONObject definition);
}
