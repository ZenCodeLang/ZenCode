export interface StringBuildable {
	toString(output as StringBuilder) as void;
	
	as string
		=> new StringBuilder() << this;
}
