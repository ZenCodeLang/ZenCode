package org.openzen.zenscript.ide;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import live.MutableLiveBool;
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
    public static void main(String... args) throws IOException {
		if (args.length == 0) {
			// testing environment - TODO project chooser/creator
			args = new String[] { "../../ZenCode", "SharedJavaSource" };
		}
		
		Arguments arguments = new Arguments(args);
		File directory = arguments.projectDirectory;
		
		Project project = new Project(directory);
		DevelopmentHost host = new LocalProjectDevelopmentHost(project);
		open(host, arguments.defaultTarget);
    }
	
	public static void open(DevelopmentHost host, String target) {
		IDEPropertyStore properties = host.getPropertyStore();
		
		IDEPropertyDirectory runState = properties.getRoot().getSubdirectory("runState");
		if (target == null)
			target = runState.getString("target", null);
		if (target == null && host.getTargets().getLength() > 0)
			target = host.getTargets().getAt(0).getName();
		
		IDEPropertyDirectory uiState = properties.getRoot().getSubdirectory("uiState");
		IDEWindow window = new IDEWindow(host, target);
		WindowView root = new WindowView(window, host, uiState);
		
		int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		MutableLiveBool maximized = uiState.getLiveBool("maximized", false);
		SwingWindow swingWindow = new SwingWindow("ZenCode IDE - " + host.getName(), root, false);
		swingWindow.setSize(
				uiState.getInt("width", 800 * pixelPerInch / 96),
				uiState.getInt("height", 600 * pixelPerInch / 96));
		swingWindow.setLocation(
				uiState.getInt("x", 0),
				uiState.getInt("y", 0));
		swingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		swingWindow.setExtendedState(maximized.getValue() ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
		swingWindow.setVisible(true);
		swingWindow.addWindowListener(new MyWindowListener(properties));
		
		swingWindow.getWindowBounds().addListener((oldBounds, newBounds) -> {
			if (swingWindow.getWindowState().getValue() == DUIWindow.State.NORMAL) {
				uiState.setInt("x", newBounds.x);
				uiState.setInt("y", newBounds.y);
				uiState.setInt("width", newBounds.width);
				uiState.setInt("height", newBounds.height);
			}
		});
		swingWindow.getWindowState().addListener((oldState, newState) -> {
			maximized.setValue(newState == DUIWindow.State.MAXIMIZED);
		});
	}
	
	private static class MyWindowListener extends WindowAdapter {
		private final IDEPropertyStore properties;
		
		public MyWindowListener(IDEPropertyStore properties) {
			this.properties = properties;
		}
		
		@Override
		public void windowClosing(WindowEvent e) {
			properties.save();
		}
	}
}
