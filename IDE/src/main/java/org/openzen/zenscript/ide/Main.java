package org.openzen.zenscript.ide;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.local.LocalProjectDevelopmentHost;
import org.openzen.drawablegui.swing.SwingWindow;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.host.IDEPropertyStore;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.WindowView;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		Arguments arguments = new Arguments(args);
		File directory = arguments.projectDirectory;
		
		Project project = new Project(directory);
		DevelopmentHost host = new LocalProjectDevelopmentHost(project);
		open(host, "SharedJavaSource");
    }
	
	public static void open(DevelopmentHost host, String target) {
		IDEPropertyStore properties = host.getPropertyStore();
		
		IDEWindow window = new IDEWindow(host, target);
		WindowView root = new WindowView(window, host);
		
		IDEPropertyDirectory uiState = properties.getRoot().getSubdirectory("uiState");
		
		int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		SwingWindow swingWindow = new SwingWindow("ZenCode IDE - " + host.getName(), root, false);
		swingWindow.setSize(
				uiState.getInt("width", 800 * pixelPerInch / 96),
				uiState.getInt("height", 600 * pixelPerInch / 96));
		swingWindow.setLocation(
				uiState.getInt("x", 0),
				uiState.getInt("y", 0));
		swingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		swingWindow.setExtendedState(uiState.getBool("maximized", false) ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
		swingWindow.setVisible(true);
		
		swingWindow.getWindowBounds().addListener((oldBounds, newBounds) -> {
			if (swingWindow.getWindowState().getValue() == DUIWindow.State.NORMAL) {
				uiState.setInt("x", newBounds.x);
				uiState.setInt("y", newBounds.y);
				uiState.setInt("width", newBounds.width);
				uiState.setInt("height", newBounds.height);
				properties.save();
			}
		});
		swingWindow.getWindowState().addListener((oldState, newState) -> {
			boolean isMaximized = newState == DUIWindow.State.MAXIMIZED;
			if (isMaximized != uiState.getBool("maximized", false)) {
				uiState.setBool("maximized", isMaximized);
				properties.save();
			}
		});
	}
}
