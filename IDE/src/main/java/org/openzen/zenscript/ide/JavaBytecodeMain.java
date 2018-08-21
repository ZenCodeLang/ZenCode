/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide;

import java.io.File;
import java.io.IOException;
import org.openzen.zenscript.constructor.Project;
import static org.openzen.zenscript.ide.Main.open;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.local.LocalProjectDevelopmentHost;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeMain {
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		Arguments arguments = new Arguments(args);
		File directory = arguments.projectDirectory;
		
		Project project = new Project(directory);
		DevelopmentHost host = new LocalProjectDevelopmentHost(project);
		open(host, "SharedJavaBytecode");
    }
}
