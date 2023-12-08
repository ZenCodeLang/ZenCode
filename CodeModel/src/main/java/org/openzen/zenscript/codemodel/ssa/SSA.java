package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;

public final class SSA {
	private final CodeBlock start;
	private final Map<CodeBlock, Node> blocks = new HashMap<>();
	private final List<Node> vertex = new ArrayList<>();

	public SSA(CodeBlock start) {
		this.start = start;
	}

	public void compute() {
		computeDominatorTree();
		calculateDominanceFrontier(vertex.get(0));
		placeSSAVariables();
		compileNodes();
	}

	private void calculateDominanceFrontier(Node n) {
		Set<Node> s = new HashSet<>();
		for (CodeBlock successor : n.block.getSuccessors()) {
			Node y = blocks.get(successor);
			if (y.idom != n)
				s.add(y);
		}
		for (Node c : n.children) {
			calculateDominanceFrontier(c);
			for (Node w : c.dominanceFrontier) {
				if (!w.isDominatedBy(n))
					s.add(w);
			}
		}
		n.dominanceFrontier = s;
	}

	private void placeSSAVariables() {
		Map<VariableID, List<VariableAssignedInstance>> definitions = new HashMap<>();
		for (Node n : vertex) {
			SSAVariableCollector collector = new Collector(n, definitions);
			for (CodeBlockStatement s : n.block.getStatements()) {
				s.collect(collector);
			}
		}

		for (VariableID variable : definitions.keySet()) {
			Queue<Node> w = definitions.get(variable).stream().map(n -> n.node).collect(Collectors.toCollection(LinkedList::new));
			while (!w.isEmpty()) {
				Node node = w.poll();
				for (Node y : node.dominanceFrontier) {
					VariableInitialInstance current = y.getInitial(variable);
					boolean changed = false;
					for (CodeBlock predecessorBlock : node.block.getPredecessors()) {
						Node predecessor = blocks.get(predecessorBlock);
						changed |= current.predecessors.add(predecessor.getVariable(variable));
					}

					if (changed) {
						w.add(y);
					}
				}
			}
		}
	}

	private void compileNodes() {
		Map<VariableID, TypeID> variableTypes = new HashMap<>();
		for (int i = vertex.size() - 1; i >= 0; i--) {
			Node node = vertex.get(i);
			List<CodeBlockStatement> statements = node.block.getStatements();
			LocalVariableLinker localVariableLinker = new LocalVariableLinker(node);
			for (int j = statements.size() - 1; j >= 0; j--) {
				CodeBlockStatement statement = statements.get(j);
				statement.linkVariables(localVariableLinker);
			}

			for (Map.Entry<VariableID, List<VariableInstance>> variableEntry : node.instances.entrySet()) {
				VariableID variable = variableEntry.getKey();
				for (VariableInstance instance : variableEntry.getValue()) {
					for (SSAVariableUsage usage : instance.usages) {
						usage.set(localVariableLinker.get(variable));
					}
				}
			}
		}
	}

	private void computeDominatorTree() {
		dfs(null, start);

		for (int i = vertex.size() - 1; i > 0; i--) {
			Node n = vertex.get(i);
			Node p = n.parent;
			Node s = p;

			for (CodeBlock predecessor : n.block.getPredecessors()) {
				Node v = blocks.get(predecessor);
				Node s2 = v.dfnum <= n.dfnum ? v : ancestorWithLowestSemi(v).semi;
				if (s2.dfnum < s.dfnum)
					s = s2;
			}

			n.semi = s;
			s.bucket.add(n);
			link(p, n);

			for (Node v : p.bucket) {
				Node y = ancestorWithLowestSemi(v);
				if (y.semi == v.semi)
					v.idom = p;
				else
					v.samedom = y;
			}

			p.bucket.clear();
		}

		for (int i = 1; i < vertex.size(); i++) {
			Node n = vertex.get(i);
			if (n.samedom != null)
				n.samedom.idom = n.idom;
		}
	}

	private Node ancestorWithLowestSemi(Node v) {
		Node a = v.ancestor;
		if (a.ancestor != null) {
			Node b = ancestorWithLowestSemi(a);
			v.ancestor = a.ancestor;
			if (b.samedom != null && b.samedom.dfnum <= v.best.semi.dfnum)
				v.best = b;
		}

		return v.best;
	}

	private void link(Node p, Node n) {
		n.ancestor = p;
		n.best = n;
	}

	private void dfs(Node parent, CodeBlock node) {
		if (blocks.containsKey(node))
			return;

		Node n = new Node(node, vertex.size(), parent);
		blocks.put(node, n);
		vertex.add(n);

		for (CodeBlock successor : node.getSuccessors())
			dfs(n, successor);
	}

	private static class Node {
		private final CodeBlock block;
		private final int dfnum;
		private final Node parent;

		private Node semi = null;
		private Node idom = null;
		private Node samedom = null;
		private Node ancestor = null;
		private Node best = null;
		private final Set<Node> bucket = new HashSet<>();
		private Set<Node> dominanceFrontier = Collections.emptySet();
		private final List<Node> children = new ArrayList<>();
		private final Map<VariableID, VariableInitialInstance> initials = new HashMap<>();
		private final Map<VariableID, VariableInstance> variables = new HashMap<>();
		private final Map<VariableID, List<VariableInstance>> instances = new HashMap<>();

		public Node(CodeBlock block, int dfnum, Node parent) {
			this.block = block;
			this.dfnum = dfnum;
			this.parent = parent;
			if (parent != null)
				parent.children.add(this);
		}

		public boolean isDominatedBy(Node other) {
			return this == other || (idom != null && idom.isDominatedBy(other));
		}

		public VariableInitialInstance getInitial(VariableID variable) {
			return initials.computeIfAbsent(variable, this::createInitial);
		}

		public VariableInstance getVariable(VariableID variable) {
			return variables.computeIfAbsent(variable, v -> getInitial(variable));
		}

		public void putVariable(VariableID variable, VariableInstance instance) {
			variables.put(variable, instance);
			instances.computeIfAbsent(variable, v -> new ArrayList<>()).add(instance);
		}

		private VariableInitialInstance createInitial(VariableID variable) {
			VariableInitialInstance initialInstance = new VariableInitialInstance(variable);
			instances.computeIfAbsent(variable, v -> new ArrayList<>()).add(initialInstance);
			return initialInstance;
		}
	}

	private static abstract class VariableInstance {
		protected final VariableID variable;
		private final List<SSAVariableUsage> usages = new ArrayList<>();

		public VariableInstance(VariableID variable) {
			this.variable = variable;
		}

		public abstract SSAVariable toSSAVariable(TypeID type);
	}

	private static class VariableAssignedInstance extends VariableInstance {
		private final Node node;
		private final SSAVariableAssignment assignment;

		public VariableAssignedInstance(Node node, VariableID variable, SSAVariableAssignment assignment) {
			super(variable);

			this.node = node;
			this.assignment = assignment;
		}

		@Override
		public SSAVariable toSSAVariable(TypeID type) {
			return new SSAValue(variable, assignment.get().as(type));
		}
	}

	private static class VariableInitialInstance extends VariableInstance {
		private final Set<VariableInstance> predecessors = new HashSet<>();

		public VariableInitialInstance(VariableID variable) {
			super(variable);
		}

		@Override
		public SSAVariable toSSAVariable(TypeID type) {
			return new SSAUnassigned(variable);
		}
	}

	private static class Collector implements SSAVariableCollector {
		private final Node node;
		private final Map<VariableID, List<VariableAssignedInstance>> definitions;
		private final ConditionalCollector conditional = new ConditionalCollector();

		public Collector(Node node, Map<VariableID, List<VariableAssignedInstance>> definitions) {
			this.node = node;
			this.definitions = definitions;
		}

		@Override
		public void assign(VariableID variable, SSAVariableAssignment assignment) {
			VariableAssignedInstance definition = new VariableAssignedInstance(node, variable, assignment);
			node.putVariable(variable, definition);
			definitions.computeIfAbsent(variable, v -> new ArrayList<>()).add(definition);
		}

		@Override
		public void usage(VariableID variable, SSAVariableUsage usage) {
			node.getVariable(variable).usages.add(usage);
		}

		@Override
		public SSAVariableCollector conditional() {
			return conditional;
		}

		private class ConditionalCollector implements SSAVariableCollector {
			@Override
			public void assign(VariableID variable, SSAVariableAssignment value) {
				// TODO - not allowed
			}

			@Override
			public void usage(VariableID variable, SSAVariableUsage usage) {
				Collector.this.usage(variable, usage);
			}

			@Override
			public SSAVariableCollector conditional() {
				return this;
			}
		}
	}

	private static class LocalVariableLinker implements CodeBlockStatement.VariableLinker {
		private final Node node;
		private final Map<VariableID, TypeID> types = new HashMap<>();

		public LocalVariableLinker(Node node) {
			this.node = node;
		}

		@Override
		public SSACompilingVariable get(VariableID variable) {
			return type -> {
				if (!types.containsKey(variable)) {
					types.put(variable, type);
				}

				List<VariableInstance> instances = node.instances.get(variable);
				if (instances == null || instances.isEmpty()) {
					return new SSAUnassigned(variable);
				} else if (instances.size() == 1) {
					return instances.get(0).toSSAVariable(type);
				} else {
					SSAVariable[] variables = instances.stream()
							.map(i -> i.toSSAVariable(type))
							.toArray(SSAVariable[]::new);
					return new SSAJoin(variable, variables);
				}
			};
		}
	}
}
