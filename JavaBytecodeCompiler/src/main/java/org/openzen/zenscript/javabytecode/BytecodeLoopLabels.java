package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Label;

public class BytecodeLoopLabels {

	public final Label loopStart;
	public final Label loopEnd;

    public BytecodeLoopLabels(Label loopStart, Label loopEnd) {
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
    }
}
