package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Label;

public class BytecodeLoopLabels {

	/** Just the start, before any pre-condition checks. A loop will jump here after one round has been executed */
	public final Label startOfLoopBody;
	/** After the loop body but before any increments or post-condition checks. Used for continue statements */
	public final Label endOfLoopBody;
	/** After the loop. Used for break statements */
	public final Label afterLoop;


    public BytecodeLoopLabels(Label startOfLoopBody, Label endOfLoopBody, Label afterLoop) {
        this.startOfLoopBody = startOfLoopBody;
		this.endOfLoopBody = endOfLoopBody;
		this.afterLoop = afterLoop;
    }
}
