package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;

import java.util.Objects;

public class EnumConstantMapping {

	private final EnumConstantMember member;
	private final String name;

	public EnumConstantMapping(EnumConstantMember member, String name) {
		this.member = member;
		this.name = name;
	}


	public EnumConstantMember getMember() {
		return member;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EnumConstantMapping{");
		sb.append("member=").append(member);
		sb.append(", name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EnumConstantMapping that = (EnumConstantMapping) o;

		if (!Objects.equals(member, that.member)) return false;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		int result = member != null ? member.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
