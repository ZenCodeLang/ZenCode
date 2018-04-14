/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents an NFA. NFAs can be compiled from a list of regular expressions.
 * 
 * @author Stan Hebben
 * @param <T> final state type (for a TokenStream, a TokenType)
 */
public class NFA<T extends Comparable<T>>
{
    public static final int EPSILON = Integer.MIN_VALUE + 1;

    private final NFAState<T> initial;
	private final Class<T> tokenClass;

    private HashMap<NodeSet, DFA.DFAState<T>> converted;

    /**
     * Creates a new NFA with the specified initial state.
     *
	 * @param tokenClass token class
     * @param initial initial state
     */
    public NFA(Class<T> tokenClass, NFAState<T> initial)
	{
        this.initial = initial;
		this.tokenClass = tokenClass;
    }

    /**
     * Creates a new NFA from the specified regular expression.
     *
     * Note: the regular expression implementation is not complete. Shorthand
     * character classes (\s, \w, \d), hexadecimal and unicode escape sequences and
     * unicode properties are not implemented.
     * 
     * Anchors, lazy plus operators, lookbehind and lookforward are not implemented
     * since they cannot be implemented in an NFA.
     *
     * @param regexp regular expression
	 * @param state resulting state
     */
	@SuppressWarnings("unchecked")
    public NFA(String regexp, T state)
	{
		tokenClass = (Class<T>) state.getClass();
		
        Partial<T> main = processRegExp(new CharStream(regexp));
        initial = new NFAState<>();
        initial.addTransition(main.tailLabel, main.tail);
        main.head.setFinal(state);
    }

    /**
     * Converts an array of regular expressions to an NFA. Each regular expression
     * can have its own final class. The length of both arrays must match.
     *
     * @param regexp regular expression array
     * @param finals final classes
     */
	@SuppressWarnings("unchecked")
    public NFA(String[] regexp, T[] finals)
	{
		tokenClass = (Class<T>) finals[0].getClass();
        initial = new NFAState<>();
		
        for (int i = 0; i < regexp.length; i++) {
			if (regexp[i] == null)
				continue;
			
			try {
				Partial<T> partial = processRegExp(new CharStream(regexp[i]));
				partial.head.setFinal(finals[i]);
				initial.addTransition(partial.tailLabel, partial.tail);
			} catch (IllegalArgumentException ex) {
				throw new RuntimeException("Error parsing " + regexp[i], ex);
			}
        }
    }
	
	/**
	 * Converts a list of regular expressions to an NFA. Each regular expression
     * can have its own final class. The length of both arrays must match.
     *
     * @param regexp regular expression array
     * @param finals final classes
	 */
	public NFA(List<String> regexp, List<T> finals, Class<T> tokenClass)
	{
		this.tokenClass = tokenClass;
		initial = new NFAState<>();
		
        for (int i = 0; i < regexp.size(); i++) {
			if (regexp.get(i) == null)
				continue;
			
			Partial<T> partial = processRegExp(new CharStream(regexp.get(i)));
            partial.head.setFinal(finals.get(i));
            initial.addTransition(partial.tailLabel, partial.tail);
        }
	}

    /**
     * Converts this NFA to a DFA. The resulting DFA is not optimal. If the
     * NFA is ambiguous, the token with lowest value will receive preference.
     *
     * @return this NFA as DFA
     */
    public DFA<T> toDFA()
	{
        converted = new HashMap<>();
        HashSet<NFAState<T>> closure = new HashSet<>();
        this.initial.closure(closure);
        DFA.DFAState<T> init = convert(new NodeSet(closure));
		
        return new DFA<>(tokenClass, init);
    }
	
	/**
	 * Converts the NFA to a DFA and optimizes and compiles it into an efficient
	 * format.
	 * 
	 * @return compiled DFA
	 */
	public CompiledDFA<T> compile()
	{
		return toDFA().optimize().compile();
	}

    // =======================
    // === Private methods ===
    // =======================

    /* Converts a set of possible states to a DFA state. */
    private DFA.DFAState<T> convert(NodeSet nodes)
	{
        if (!converted.containsKey(nodes)) {
            DFA.DFAState<T> node = new DFA.DFAState<>();
            converted.put(nodes, node);

            HashSet<Integer> edgeSet = new HashSet<>();
            for (NFAState<T> n : nodes.nodes) {
                n.alphabet(edgeSet);
            }
            for (int i : edgeSet) {
                HashSet<NFAState<T>> edge = new HashSet<>();
                for (NFAState<T> n : nodes.nodes) {
					n.edge(i, edge);
				}
                NodeSet set = new NodeSet(edge);
                node.addTransition(i, convert(set));
            }
            T finalCode = null;
            for (NFAState<T> n : nodes.nodes) {
                if (n.state != null) {
                    if (finalCode != null) {
                        finalCode = n.state.compareTo(finalCode) < 0 ? n.state : finalCode;
                    } else {
                        finalCode = n.state;
                    }
                }
            }
            node.setFinal(finalCode);
            
            return node;
        }
        return converted.get(nodes);
    }

    /* Processes a regular expression */
    private Partial<T> processRegExp(CharStream stream) {
        Partial<T> partial = processRegExp0(stream);
        if (stream.optional('|')) {
            ArrayList<Partial<T>> partials = new ArrayList<>();
            partials.add(partial);
            partials.add(processRegExp0(stream));
            while (stream.optional('|')) {
                partials.add(processRegExp0(stream));
            }
            NFAState<T> head = new NFAState<>();
            NFAState<T> tail = new NFAState<>();
            for (Partial<T> p : partials) {
                tail.addTransition(p.tailLabel, p.tail);
                p.head.addTransition(EPSILON, head);
            }
            return new Partial<>(EPSILON, tail, head);
        } else {
            return partial;
        }
    }

    /* Processes an element of an alternation clause */
    private Partial<T> processRegExp0(CharStream stream)
	{
        Partial<T> partial = processRegExp1(stream);
        while (!stream.peek(')') && !stream.peek('|') && stream.hasMore()) {
            Partial<T> partial2 = processRegExp1(stream);
            partial.head.addTransition(partial2.tailLabel, partial2.tail);
            partial = new Partial<>(partial.tailLabel, partial.tail, partial2.head);
        }
        return partial;
    }

    /* Processes a partial with optional repetition */
    private Partial<T> processRegExp1(CharStream stream)
	{
        Partial<T> partial = processPartial(stream);
        if (stream.optional('*')) {
            NFAState<T> node = new NFAState<>();
            partial.head.addTransition(EPSILON, node);
            node.addTransition(partial.tailLabel, partial.tail);
            return new Partial<>(EPSILON, node, node);
        } else if (stream.optional('+')) {
            NFAState<T> node = new NFAState<>();
            partial.head.addTransition(EPSILON, node);
            node.addTransition(partial.tailLabel, partial.tail);
            return new Partial<>(partial.tailLabel, partial.tail, node);
        } else if (stream.optional('?')) {
            NFAState<T> node = new NFAState<>();
            node.addTransition(EPSILON, partial.head);
            node.addTransition(partial.tailLabel, partial.tail);
            return new Partial<>(EPSILON, node, partial.head);
        } else if (stream.optional('{')) {
            int amount = processInt(stream);
            if (amount < 0) throw new IllegalArgumentException("Repitition count expected");
            if (stream.optional(',')) {
                int amount2 = processInt(stream);
                stream.required('}');
                if (amount2 < 0) {
                    // unbounded
					@SuppressWarnings("unchecked")
                    Partial<T>[] duplicates = new Partial[amount];
                    for (int i = 0; i < duplicates.length - 1; i++) {
                        duplicates[i] = partial.duplicate();
                    }
                    duplicates[amount - 1] = partial;
                    for (int i = 0; i < duplicates.length - 1; i++) {
                        duplicates[i].head.addTransition(duplicates[i + 1].tailLabel, duplicates[i + 1].tail);
                    }
                    duplicates[amount - 1].head.addTransition(duplicates[amount - 1].tailLabel, duplicates[amount - 1].tail);
                    return new Partial<>(duplicates[0].tailLabel, duplicates[0].tail, duplicates[amount - 1].head);
                } else {
					@SuppressWarnings("unchecked")
                    Partial<T>[] duplicates = new Partial[amount2];
                    for (int i = 0; i < duplicates.length - 1; i++) {
                        duplicates[i] = partial.duplicate();
                    }
                    duplicates[amount2 - 1] = partial;
                    for (int i = 0; i < duplicates.length - 1; i++) {
                        duplicates[i].head.addTransition(duplicates[i + 1].tailLabel, duplicates[i + 1].tail);
                    }
                    for (int i = amount; i < amount2; i++) {
                        if (i == 0) {
                            /* insert additional node before the chain because minimal repeat is 0 */
                            NFAState<T> additional = new NFAState<>();
                            additional.addTransition(duplicates[0].tailLabel, duplicates[0].tail);
                            additional.addTransition(EPSILON, duplicates[amount2 - 1].head);
                            duplicates[0].tailLabel = EPSILON;
                            duplicates[0].tail = additional;
                        } else {
                            duplicates[i - 1].head.addTransition(EPSILON, duplicates[amount2 - 1].head);
                        }
                    }
                    return new Partial<>(duplicates[0].tailLabel, duplicates[0].tail, duplicates[amount2 - 1].head);
                }
            } else {
                stream.required('}');
                
				@SuppressWarnings("unchecked")
                Partial<T>[] duplicates = new Partial[amount];
                for (int i = 0; i < duplicates.length - 1; i++) {
                    duplicates[i] = partial.duplicate();
                }
                duplicates[amount - 1] = partial;
                for (int i = 0; i < duplicates.length - 1; i++) {
                    duplicates[i].head.addTransition(duplicates[i + 1].tailLabel, duplicates[i + 1].tail);
                }
                return new Partial<>(duplicates[0].tailLabel, duplicates[0].tail, duplicates[amount - 1].head);
            }
        } else {
            return partial;
        }
    }

    /* Processes a part of a regular expression, which can be a character,
     * expression between brackets, a dot or a character list */
    private Partial<T> processPartial(CharStream stream) {
        if (stream.optional('(')) {
            Partial<T> result = processRegExp(stream);
            stream.required(')');
            return result;
        } else if (stream.optional('[')) {
            NFAState<T> head = new NFAState<>();
            NFAState<T> tail = new NFAState<>();

            TIntIterator iter = processCharList(stream).iterator();
            while (iter.hasNext()) {
                tail.addTransition(iter.next(), head);
            }
            stream.required(']');
            return new Partial<>(EPSILON, tail, head);
        } else if (stream.optional('.')) {
            NFAState<T> head = new NFAState<>();
            NFAState<T> tail = new NFAState<>();

            for (int i = 0; i <= 256; i++) {
                tail.addTransition(i, head);
            }
            return new Partial<>(EPSILON, tail, head);
        } else {
            NFAState<T> node = new NFAState<>();
            return new Partial<>(processChar(stream), node, node);
        }
    }

    /* Processes a character list */
    private TIntHashSet processCharList(CharStream stream)
	{
        boolean invert = stream.optional('^');
        TIntHashSet base = new TIntHashSet();
        do {
            processCharPartial(base, stream);
        } while (!stream.peek(']'));
        if (invert) {
            TIntHashSet result = new TIntHashSet();
            for (int i = 0; i <= 256; i++) {
                if (!base.contains(i)) result.add(i);
            }
            return result;
        } else {
            return base;
        }
    }

    /* Processes a character partial, which can be a single character or a range
     * of characters. */
    private void processCharPartial(TIntHashSet out, CharStream stream)
	{
        if (stream.optional('.')) {
            for (int i = 0; i <= 256; i++) {
                out.add(i);
            }
        } else {
            int from = processChar(stream);
            if (stream.optional('-')) {
                int to = processChar(stream);
                for (int i = from; i <= to; i++) {
                    out.add(i);
                }
            } else {
                out.add(from);
            }
        }
    }

    /* Processes a single character */
    private int processChar(CharStream stream)
	{
        if (stream.optional('\\')) {
			int c = stream.next();
			switch (c) {
				case 'e': return -1;
				case 'r': return '\r';
				case 'n': return '\n';
				case 't': return '\t';
				case '[': return '[';
				case ']': return ']';
				case '(': return '(';
				case ')': return ')';
				case '.': return '.';
				case '+': return '+';
				case '-': return '-';
				case '\\': return '\\';
				case '{': return '{';
				case '}': return '}';
				case '?': return '?';
				case '*': return '*';
				case '~': return '~';
				case '|': return '|';
				case '^': return '^';
				default:
		            throw new IllegalArgumentException("Invalid character: " + (char) c);
			}
        } else {
			int c = stream.next();
			switch (c) {
				case '[':
				case ']':
				case '(':
				case ')':
				case '{':
				case '}':
				case '.':
				case '?':
				case '*':
	                throw new IllegalArgumentException("Invalid character: " + (char) c);
				default:
					return c;
			}
        }
    }

    /* Processes an optional integer, returns -1 if the next character do not
     * represent an integer */
    private int processInt(CharStream stream)
	{
        int data = stream.optional('0', '9') - '0';
        if (data < 0) return -1;
        char ch = stream.optional('0', '9');
        while (ch != 0) {
            data = data * 10 + (ch - '0');
            ch = stream.optional('0', '9');
        }
        return data;
    }

    // ============================
    // === Public inner classes ===
    // ============================

    /**
     * Represents an NFA state.
	 * 
	 * @param <T> final type
     */
    public static class NFAState<T>
	{
        private static int counter = 1;

        private ArrayList<Transition> transitions;
        private ArrayList<NFAState<T>> closure;
        private int index;
        private T state;

        /**
         * Creates a new state.
         */
        public NFAState()
		{
            transitions = new ArrayList<>();
            index = counter++;
        }

        /**
         * Adds a transition.
         *
         * @param label transition label
         * @param next next state
         */
        public void addTransition(int label, NFAState<T> next)
		{
            transitions.add(new Transition(label, next));
        }

        /**
         * Sets the final state of this state.
         *
         * @param finalCode final code
         */
        public void setFinal(T finalCode)
		{
            if (this.state == finalCode)
				return;
			
            this.state = finalCode;

            for (Transition t : transitions)
                if (t.label == EPSILON)
                    t.next.setFinal(finalCode);
        }

        // =======================
        // === Private methods ===
        // =======================

        /**
         * Determines the (partial) closure of this state.
         *
         * @param output output to store the closure in
         */
        private void closure(HashSet<NFAState<T>> output) {
            for (NFAState<T> node : closure())
				output.add(node);
        }

        /**
         * Calculates the (full) closure of this state.
         *
         * @return this state's closure
         */
        private ArrayList<NFAState<T>> closure()
		{
            if (closure == null) {
                closure = new ArrayList<>();
                HashSet<NFAState<T>> tmp = new HashSet<>();
                tmp.add(this);
                for (Transition transition : transitions) {
                    if (transition.label == EPSILON) {
                        if (!tmp.contains(transition.next)) {
                            tmp.add(transition.next);
                            transition.next.closure(tmp);
                        }
                    }
                }
                for (NFAState<T> node : tmp)
					closure.add(node);
            }
			
            return closure;
        }

        /**
         * Calculates the possible set of states after a transition with the
         * specified value
         *
         * @param label transition label
         * @param output possible set of states (out)
         */
        private void edge(int label, HashSet<NFAState<T>> output)
		{
            for (Transition transition : transitions) {
                if (transition.label == label) {
                    if (!output.contains(transition.next)) {
                        output.add(transition.next);
                        transition.next.closure(output);
                    }
                }
            }
        }

        /**
         * Calculates the alphabet of this state.
         *
         * @param output alphabet (out)
         */
        private void alphabet(HashSet<Integer> output)
		{
            for (NFAState<T> node : closure()) {
                for (Transition t : node.transitions) {
                    if (t.label != EPSILON) {
                        output.add(t.label);
                    }
                }
            }
        }

        // ===================
        // === Inner class ===
        // ===================

        /* Represents a transition */
        private class Transition
		{
            private int label;
            private NFAState<T> next;

            public Transition(int input, NFAState<T> next)
			{
                this.label = input;
                this.next = next;
            }
        }
    }

    /* Hashable set of nodes */
    private class NodeSet
	{
        private NFAState<T>[] nodes;
		
		@SuppressWarnings("unchecked")
        public NodeSet(HashSet<NFAState<T>> nodes)
		{
            this.nodes = new NFAState[nodes.size()];
            int i = 0;
            for (NFAState<T> node : nodes)
				this.nodes[i++] = node;
			
            Arrays.sort(this.nodes, (NFAState<T> o1, NFAState<T> o2) -> o1.index - o2.index);
        }

        @Override
        public int hashCode()
		{
            return Arrays.hashCode(nodes);
        }

        @Override
		@SuppressWarnings("unchecked")
        public boolean equals(Object other)
		{
            if (other.getClass() != NodeSet.class) return false;
            return Arrays.equals(nodes, ((NodeSet)other).nodes);
        }
    }

    /**
     * Contains a partially parsed regular expression. Has a head and a tail.
     *
     * The tail is a transition and thus has a label and a next node. The head
     * is the 'final' state of the partial.
     */
    private static class Partial<T>
	{
        private int tailLabel;
        private NFAState<T> tail;
        private NFAState<T> head;

        /**
         * Creates a new partial
         * @param tailLabel tail label
         * @param tail tail node
         * @param head head node
         */
        public Partial(int tailLabel, NFAState<T> tail, NFAState<T> head)
		{
            this.tailLabel = tailLabel;
            this.tail = tail;
            this.head = head;
        }

        /**
         * Duplicates the NFA of this partial.
         *
         * @return a duplicate of this partial
         */
        public Partial<T> duplicate()
		{
            HashMap<NFAState<T>, NFAState<T>> nodes = new HashMap<>();
			
            Queue<NFAState<T>> todo = new LinkedList<>();
            todo.add(tail);
            nodes.put(tail, new NFAState<>());
            while (!todo.isEmpty()) {
                NFAState<T> node = todo.poll();
                for (NFAState<T>.Transition t : node.transitions) {
                    if (!nodes.containsKey(t.next)) {
                        nodes.put(t.next, new NFAState<>());
                        todo.add(t.next);
                    }
                    nodes.get(node).addTransition(t.label, nodes.get(t.next));
                }
            }

            return new Partial<>(tailLabel, nodes.get(tail), nodes.get(head));
        }
    }
}
