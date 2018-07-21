/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.dialog;

import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DButton;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DInputField;
import org.openzen.drawablegui.DLabel;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.form.DForm;
import org.openzen.drawablegui.form.DFormComponent;
import org.openzen.drawablegui.layout.DLinearLayout;
import org.openzen.drawablegui.layout.DLinearLayout.Alignment;
import org.openzen.drawablegui.layout.DLinearLayout.Element;
import org.openzen.drawablegui.layout.DLinearLayout.ElementAlignment;
import org.openzen.drawablegui.layout.DLinearLayout.Orientation;
import org.openzen.drawablegui.live.ImmutableLiveBool;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEWindow;

/**
 *
 * @author Hoofdgebruiker
 */
public class CreateSourceFileDialog {
	private final IDEWindow ideWindow;
	private final IDEModule module;
	private final IDEPackage pkg;
	
	private final SimpleLiveString name;
	
	private final DComponent root;
	private final DInputField input;
	
	private DUIWindow window;
	
	public CreateSourceFileDialog(IDEWindow ideWindow, IDEModule module, IDEPackage pkg) {
		this.ideWindow = ideWindow;
		this.module = module;
		this.pkg = pkg;
		
		name = new SimpleLiveString("");
		input = new DInputField(DStyleClass.EMPTY, name, new DDpDimension(100));
		input.setOnEnter(this::ok);
		input.setOnEscape(this::cancel);
		
		DForm form = new DForm(
				DStyleClass.EMPTY,
				new DFormComponent("Module:", new DLabel(DStyleClass.EMPTY, new ImmutableLiveString(module.getName()))),
				new DFormComponent("Package:", new DLabel(DStyleClass.EMPTY, new ImmutableLiveString(pkg.getName().isEmpty() ? "(module root)" : pkg.getName()))),
				new DFormComponent("Filename:", input));

		DButton ok = new DButton(DStyleClass.EMPTY, new SimpleLiveString("Create"), ImmutableLiveBool.FALSE, this::ok);
		DButton cancel = new DButton(DStyleClass.EMPTY, new SimpleLiveString("Cancel"), ImmutableLiveBool.FALSE, this::cancel);
		DLinearLayout buttons = new DLinearLayout(
				DStyleClass.EMPTY,
				Orientation.HORIZONTAL,
				Alignment.RIGHT,
				new Element(cancel, 0, 0, ElementAlignment.TOP),
				new Element(ok, 0, 0, ElementAlignment.TOP));

		root = new DLinearLayout(
				DStyleClass.EMPTY,
				Orientation.VERTICAL,
				Alignment.MIDDLE,
				new Element(form, 1, 1, ElementAlignment.CENTER),
				new Element(buttons, 0, 0, ElementAlignment.RIGHT));
	}
	
	public void open(DUIWindow parent) {
		DIRectangle rectangle = parent.getWindowBounds().getValue();
		window = parent.getContext().openDialog(rectangle.width / 2, rectangle.height / 2, DAnchor.MIDDLE_CENTER, "Create source file", root);
		window.focus(input);
	}
	
	private void cancel() {
		window.close();
	}
	
	private void ok() {
		window.close();
		IDESourceFile file = pkg.createSourceFile(name.getValue() + ".zs");
		ideWindow.open(file);
	}
}
