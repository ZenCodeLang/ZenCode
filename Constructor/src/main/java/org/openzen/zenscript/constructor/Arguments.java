/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

/**
 * @author Hoofdgebruiker
 */
public class Arguments {
	public final String directory;
	public final String target;
	public final boolean help;

	public Arguments(String directory, String target) {
		this(directory, target, false);
	}

	private Arguments(String directory, String target, boolean help) {
		this.directory = directory;
		this.target = target;
		this.help = help;
	}

	public static Arguments parse(String[] args) {
		String directory = ".";
		boolean help = false;
		String target = null;

		int position = 0;
		outer:
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-d":
					i++;
					directory = args[0];
					continue outer;
				case "-h":
				case "--help":
					help = true;
					continue outer;
				default:
					if (position == 0) {
						position++;
						target = args[i];
					}
			}
		}

		return new Arguments(directory, target, help);
	}
}
