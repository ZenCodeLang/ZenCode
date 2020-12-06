package org.openzen.zenscript.ide;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;

import live.LiveObject;
import live.MutableLiveBool;
import live.SimpleLiveObject;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.local.LocalProjectDevelopmentHost;
import org.openzen.drawablegui.swing.SwingWindow;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.ide.host.IDECompileState;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.host.IDEPropertyStore;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.WindowView;

public class Main {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String... args) throws IOException {
		if (args.length == 0) {
			// testing environment - TODO project chooser/creator
			args = new String[]{"../../ZenCode", "SharedJavaSource"};
			//args = new String[] { "../../ZenCode", "CodeModelJavaSource" };
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

		IDETarget targetObject = null;
		for (IDETarget t : host.getTargets())
			if (t.getName().equals(target))
				targetObject = t;

		if (targetObject == null)
			throw new IllegalStateException("No target specified");

		LiveObject<IDETarget> liveTarget = new SimpleLiveObject<>(targetObject);
		LiveObject<IDECompileState> compileState = liveTarget.getValue().load(); // TODO: update this when target changes

		IDEPropertyDirectory uiState = properties.getRoot().getSubdirectory("uiState");
		IDEWindow window = new IDEWindow(host, target);
		WindowView root = new WindowView(window, host, uiState, compileState);

		int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		MutableLiveBool maximized = uiState.getLiveBool("maximized", false);
		SwingWindow swingWindow = new SwingWindow("ZenCode IDE - " + host.getName(), root, false);

		int x = uiState.getInt("x", 0);
		int y = uiState.getInt("y", 0);
		int width = uiState.getInt("width", 800 * pixelPerInch / 96);
		int height = uiState.getInt("height", 600 * pixelPerInch / 96);
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		if (x < bounds.x)
			x = bounds.x;
		if (y < bounds.y)
			y = bounds.y;
		if (width > bounds.width)
			width = bounds.width;
		if (height > bounds.height)
			height = bounds.height;

		swingWindow.setSize(width, height);
		swingWindow.setLocation(x, y);
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
