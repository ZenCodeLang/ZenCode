package org.openzen.zenscript.lexer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Implements a DFA. Used as intermediate form when compiling an NFA to a
 * CompiledDFA for usage in a TokenStream.
 *
 * @param <T> final type
 */
public class DFA<T>
{
    public static final int NOFINAL = Integer.MIN_VALUE;

	private final Class<T> tokenClass;
    private DFAState<T> initial;

    /**
     * Constructs a new DFA with the specified initial state.
     *
     * @param initial initial state
	 * @param tokenClass token class
     */
    public DFA(Class<T> tokenClass, DFAState<T> initial)
	{
		this.tokenClass = tokenClass;
        this.initial = initial;
    }

    /**
     * Compiles this DFA into a more efficient structure.
     *
     * @return the compiled DFA
     */
    public CompiledDFA<T> compile()
	{
        ArrayList<DFAState<T>> nodeList = new ArrayList<>();
        HashMap<DFAState<T>, Integer> nodes = new HashMap<>();
        nodes.put(initial, 0);
        nodeList.add(initial);

        /* Find all reachable nodes in the dfs */
        int counter = 1;
        Queue<DFAState<T>> todo = new LinkedList<>();
        todo.add(initial);

        while (!todo.isEmpty()) {
            DFAState<T> current = todo.poll();
    
            Iterator<Integer> it = current.transitions.keySet().iterator();
            while (it.hasNext()) {
                int k = it.next();
                DFAState<T> next = current.transitions.get(k);
                if (!nodes.containsKey(next)) {
                    todo.add(next);
                    nodes.put(next, counter++);
                    nodeList.add(next);
                }
            }
        }

        /* Compile */
        Map<Integer, Integer>[] transitions = new HashMap[counter];
		@SuppressWarnings("unchecked")
        T[] finals2 = (T[]) Array.newInstance(tokenClass, counter);
		
        for (DFAState<T> node : nodeList) {
            int index = nodes.get(node);
            finals2[index] = node.finalCode;
    
            transitions[index] = new HashMap<>();
            Iterator<Integer> it = node.transitions.keySet().iterator();
            while (it.hasNext()) {
                int k = it.next();
                DFAState<T> next = node.transitions.get(k);
                transitions[index].put(k, nodes.get(next));
            }
        }

        return new CompiledDFA<>(transitions, finals2);
    }

    /**
     * Generates the minimal version of this DFA.
     *
     * @return the minimal DFA
     */
    public DFA<T> optimize()
	{
        CompiledDFA<T> compiled = compile();
        Map<Integer, Integer>[] transitions = compiled.transitions;
        int size = transitions.length;

        /* Collect all edges and determine alphabet */
        Set<Integer> alphabet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Iterator<Integer> it = transitions[i].keySet().iterator();
            while (it.hasNext()) {
                int k = it.next();
                alphabet.add(k);
            }
        }

        /* Initialize distinguishing array */
        boolean[][] distinguishable = new boolean[size + 1][size + 1];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distinguishable[i][j] = compiled.finals[i] != compiled.finals[j];
            }
        }
        for (int i = 0; i < size; i++) {
            distinguishable[i][size] = true;
            distinguishable[size][i] = true;
        }

        /* Minimization algorithm implementation */
        boolean changed;
        do {
            changed = false;
            Iterator<Integer> ita = alphabet.iterator();
            while (ita.hasNext()) {
                int x = ita.next();
                for (int i = 0; i < size; i++) {
                    int ti = transitions[i].containsKey(x) ? transitions[i].get(x) : size;
                    for (int j = 0; j < size; j++) {
                        if (distinguishable[i][j]) continue;

                        int tj = transitions[j].containsKey(x) ? transitions[j].get(x) : size;
                        if (distinguishable[ti][tj]) {
                            distinguishable[i][j] = true;
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        /* Group nodes */
        Map<Integer, DFAState<T>> nodeMap = new HashMap<>();
        outer: for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!distinguishable[i][j] && nodeMap.containsKey(j)) {
                    nodeMap.put(i, nodeMap.get(j));
                    if (compiled.finals[i] != null) {
                        if (nodeMap.get(j).getFinal() != null && nodeMap.get(j).getFinal() != compiled.finals[i]) {
                            throw new RuntimeException("Eh?");
                        }
                    }
                    continue outer;
                }
            }
            DFAState<T> node = new DFAState<>();
            node.setFinal(compiled.finals[i]);
            nodeMap.put(i, node);
        }

        for (int i = 0; i < compiled.transitions.length; i++) {
            Iterator<Integer> iter = transitions[i].keySet().iterator();
            while (iter.hasNext()) {
                int k = iter.next();

                nodeMap.get(i).addTransition(k, nodeMap.get(transitions[i].get(k)));
            }
        }
        
        DFA<T> result = new DFA<>(tokenClass, nodeMap.get(0));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        CompiledDFA<T> dfs = compile();
        for (int i = 0; i < dfs.transitions.length; i++) {
            Map<Integer, Integer> map = dfs.transitions[i];
    
            Iterator<Integer> it = map.keySet().iterator();
            while (it.hasNext()) {
                int v = it.next();
                result.append("edge(");
                result.append(i);
                result.append(", ");
                result.append(v);
                result.append("): ");
                result.append(map.get(v));
                result.append("\r\n");
            }
        }
        for (int i = 0; i < dfs.finals.length; i++) {
            if (dfs.finals[i] != null) {
                result.append("final(");
                result.append(i);
                result.append("): ");
                result.append(dfs.finals[i]);
                result.append("\r\n");
            }
        }
        return result.toString();
    }

    // ============================
    // === Public inner classes ===
    // ============================

    /**
     * Represents a state in a DFA.
     */
    public static class DFAState<T>
	{
        
        private Map<Integer, DFAState<T>> transitions;
        private T finalCode = null;

        /**
         * Creates a new DFA state.
         */
        public DFAState() {
            transitions = new HashMap<>();
        }

        /**
         * Adds a transition.
         *
         * @param label transition edge label
         * @param next next state
         */
        public void addTransition(int label, DFAState<T> next) {
            transitions.put(label, next);
        }

        /**
         * Sets the final class of this state. Finals can be divided in multiple
         * class, in which case each class gets its own index. Class NOFINAL is
         * used to indicate nonfinals.
         *
         * @param finalCode final index
         */
        public void setFinal(T finalCode) {
            this.finalCode = finalCode;
        }

        /**
         * Gets the final class of this state. Equals NOFINAL if this state is
         * not a final.
         *
         * @return final index
         */
        public T getFinal() {
            return finalCode;
        }
    }
}
