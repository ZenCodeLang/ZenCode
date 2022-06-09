package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;

import java.util.List;

public class MemberUnion implements ResolvedType {
	public static ResolvedType of(List<ResolvedType> resolutions) {
		if (resolutions.isEmpty()) {
			return new MemberSet();
		} else if (resolutions.size() == 1) {
			return resolutions.get(0);
		} else {
			return new MemberUnion(resolutions);
		}
	}

	private final List<ResolvedType> resolutions;

	private MemberUnion(List<ResolvedType> resolutions) {
		this.resolutions = resolutions;
	}
}
