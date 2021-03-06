package tudelft.ti2806.pl3.data.wrapper;

import tudelft.ti2806.pl3.data.Genome;
import tudelft.ti2806.pl3.data.graph.DataNode;
import tudelft.ti2806.pl3.data.label.Label;
import tudelft.ti2806.pl3.data.wrapper.operation.WrapperOperation;
import tudelft.ti2806.pl3.util.DoneDeque;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wrappers are used to wrap the {@link DataNode}s into a smaller graph.
 *
 * @author Sam Smulders
 */
@SuppressWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
public abstract class Wrapper implements Comparable<Wrapper> {

	private float y;
	private float x;

	private List<Wrapper> incoming = new ArrayList<>();
	private List<Wrapper> outgoing = new ArrayList<>();
	private List<Integer> outgoingWeight = new ArrayList<>();

	private Set<Label> labels;
	private Set<DataNode> dataNodeList;

	private int previousNodesCount = -1;
	private float interest = 1f;

	/**
	 * @return the maximum number of base pairs that can be passed when following the graph from the start of this node
	 *         to its end.
	 */
	public abstract long getBasePairCount();
	
	/**
	 * @return the distance between the start x and end x of this node.
	 */
	public abstract int getWidth();

	public abstract String getBasePairString();
	
	public int getPreviousNodesCount() {
		return previousNodesCount;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setY(float y) {
		this.y = y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public abstract void calculateX();
	
	public List<Wrapper> getIncoming() {
		return incoming;
	}
	
	public List<Wrapper> getOutgoing() {
		return outgoing;
	}
	
	public void setIncoming(List<Wrapper> incoming) {
		this.incoming = incoming;
	}
	
	public void setOutgoing(List<Wrapper> outgoing) {
		this.outgoing = outgoing;
	}
	
	public List<Integer> getOutgoingWeight() {
		return outgoingWeight;
	}

	public abstract String getIdString();
	
	public abstract int getId();

	public abstract boolean canUnwrap();

	public abstract Set<Genome> getGenome();
	
	public abstract void calculate(WrapperOperation wrapperSequencer, Wrapper container);
	
	/**
	 * NodeWrapper is only comparable when the {@link #previousNodesCount} is calculated.
	 */
	@Override
	public int compareTo(Wrapper other) {
		int order = this.previousNodesCount - other.previousNodesCount;
		if (order != 0) {
			return order;
		}
		return Integer.compare(getId(), other.getId());
	}
	
	public abstract void collectDataNodes(Set<DataNode> list);
	
	/**
	 * Get all {@link DataNode}s in this node and its children.
	 *
	 * @return set of {@link DataNode}s
	 */
	public Set<DataNode> getDataNodes() {
		if (dataNodeList == null) {
			dataNodeList = new HashSet<>();
			collectDataNodes(dataNodeList);
		}
		return dataNodeList;
	}
	
	public abstract void collectLabels(Set<Label> labels);
	
	/**
	 * Get all Labels in this node and its children.
	 *
	 * @return set of Labels
	 */
	public Set<Label> getLabels() {
		if (dataNodeList == null) {
			labels = new HashSet<>();
			collectLabels(labels);
		}
		return labels;
	}
	
	public float getInterest() {
		return interest;
	}
	
	public void addInterest(float interest) {
		this.interest += interest;
	}
	
	public void multiplyInterest(float interest) {
		this.interest *= interest;
	}
	
	/**
	 * Computes the longest path for each {@link Wrapper} in the list and returns the length of the longest path found.
	 *
	 * @param nodeWrappers
	 *            the list of the {@link Wrapper}s to compute the longest path of
	 * @return the length of the longest path found
	 */
	public static int computeLongestPaths(List<Wrapper> nodeWrappers) {
		DoneDeque<Wrapper> leftToRight = new DoneDeque<>(nodeWrappers.size());
		// Find all first wrappers
		nodeWrappers.stream().filter(wrapper -> wrapper.getIncoming().size() == 0).forEach(leftToRight::add);
		int max = 0;
		DoneDeque<Wrapper> rightToLeft = new DoneDeque<>(nodeWrappers.size());
		// Calculate all previous node counts from left to right
		for (int i = nodeWrappers.size(); i > 0; i--) {
			Wrapper wrapper = leftToRight.poll();
			// Find all last wrappers
			if (wrapper.getOutgoing().size() == 0) {
				rightToLeft.addAll(wrapper.getIncoming());
			} else {
				wrapper.getOutgoing().stream().filter(out -> leftToRight.doneAll(out.getIncoming()))
						.forEach(leftToRight::add);
			}
			max = Math.max(wrapper.calculatePreviousNodesCount(), max);
		}
		// Improve all previous node counts from right to left
		while (!rightToLeft.isEmpty()) {
			Wrapper wrapper = rightToLeft.poll();
			wrapper.getIncoming().stream().filter(in -> rightToLeft.doneAll(in.getOutgoing()))
					.forEach(rightToLeft::add);
			wrapper.shiftPreviousNodeCount();
		}
		return max;
	}
	
	/**
	 * Shifts the previous node count as far to the right as possible.
	 */
	private void shiftPreviousNodeCount() {
		int max = Integer.MAX_VALUE;
		for (Wrapper wrapper : this.outgoing) {
			max = Math.min(wrapper.getPreviousNodesCount(), max);
		}
		this.previousNodesCount = max - 1;
	}
	
	/**
	 * Calculate the number of nodes on the longest path to this node.
	 */
	private int calculatePreviousNodesCount() {
		int max = 0;
		for (Wrapper incomingNode : this.incoming) {
			max = Math.max(max, incomingNode.previousNodesCount + 1);
		}
		this.previousNodesCount = max;
		return max;
	}
	
	public abstract boolean contains(Wrapper originalNode);
	
	@Override
	public String toString() {
		return this.getClass().toString() + ":" + this.getId();
	}
}
