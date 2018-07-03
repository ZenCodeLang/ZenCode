package org.openzen.zenscript.ide;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.local.LocalProjectDevelopmentHost;
import org.openzen.drawablegui.swing.SwingWindow;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.WindowView;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		File directory = new File("../../ZenCode");
		CompilationUnit compilationUnit = new CompilationUnit();
		ModuleLoader loader = new ModuleLoader(compilationUnit);
		Project project = new Project(loader, directory);
		
		DevelopmentHost host = new LocalProjectDevelopmentHost(project);
		
		int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		
		IDEWindow window = new IDEWindow(host);
		WindowView root = new WindowView(window, host);
		
		SwingWindow swingWindow = new SwingWindow("ZenCode IDE - " + host.getName(), root, false);
		swingWindow.setSize(800 * pixelPerInch / 96, 600 * pixelPerInch / 96);
		swingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		swingWindow.setVisible(true);
    }
}
