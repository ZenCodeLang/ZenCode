package org.openzen.zenscript.rustsource.definitions;

public class RustModule {
	public static final RustModule STD_RC = new RustModule(false, new String[] { "std", "rc" });
	public static final RustModule STD_SYNC = new RustModule(false, new String[] { "std", "sync" });
	public static final RustModule STD_COLLECTIONS = new RustModule(false, new String[] { "std", "collections" });
	public static final RustModule STD_ITER = new RustModule(false, new String[] { "std", "iter" });
	public static final RustModule STD_OPTION = new RustModule(false, new String[] { "std", "option" });
	public static final RustModule STD_OPS = new RustModule(false, new String[] { "std", "ops" });

	public final boolean crate;
	public final String[] path;

	public RustModule(boolean crate, String[] path) {
		this.crate = crate;
		this.path = path;
	}
}
