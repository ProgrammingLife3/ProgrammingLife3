package tudelft.ti2806.pl3.visualization.wrapper.operation.interest;

import tudelft.ti2806.pl3.data.Genome;
import tudelft.ti2806.pl3.visualization.wrapper.HorizontalWrapper;
import tudelft.ti2806.pl3.visualization.wrapper.NodePosition;
import tudelft.ti2806.pl3.visualization.wrapper.NodeWrapper;
import tudelft.ti2806.pl3.visualization.wrapper.SingleWrapper;
import tudelft.ti2806.pl3.visualization.wrapper.SpaceWrapper;
import tudelft.ti2806.pl3.visualization.wrapper.VerticalWrapper;
import tudelft.ti2806.pl3.visualization.wrapper.operation.WrapperOperation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Adds interest to try to split the different groups.
 * 
 * <p>
 * If a node only contains nodes out one group, it gets no extra interest.<br>
 * If a node contains nodes from multiple groups, we want to try to split this
 * node and add extra interest. <br>
 * {@link NodePosition} is ignored because this node can't be split.
 * 
 * @author Sam Smulders
 *
 */
public class CalculateGroupInterest extends WrapperOperation {
	
	private final List<Set<Genome>> groups;
	private final int interest;
	
	public CalculateGroupInterest(List<Set<Genome>> groups, int groupInterest) {
		this.groups = groups;
		this.interest = groupInterest;
	}
	
	@Override
	public void calculate(HorizontalWrapper wrapper, NodeWrapper container) {
		super.calculate(wrapper, container);
		if (isIntresting(wrapper)) {
			wrapper.addInterest(interest);
		}
	}
	
	@Override
	public void calculate(VerticalWrapper wrapper, NodeWrapper container) {
		super.calculate(wrapper, container);
		if (isIntresting(wrapper)) {
			wrapper.addInterest(interest);
		}
	}
	
	@Override
	public void calculate(SpaceWrapper wrapper, NodeWrapper container) {
		super.calculate(wrapper, container);
		if (isIntresting(wrapper)) {
			wrapper.addInterest(interest);
		}
	}
	
	@Override
	public void calculate(SingleWrapper wrapper, NodeWrapper container) {
		super.calculate(wrapper, container);
		if (isIntresting(wrapper)) {
			wrapper.addInterest(interest);
		}
	}
	
	boolean isIntresting(NodeWrapper wrapper) {
		Set<Genome> genome = wrapper.getGenome();
		boolean found = false;
		for (Set<Genome> group : groups) {
			if (!Collections.disjoint(group, genome)) {
				if (found) {
					return true;
				} else {
					found = true;
				}
			}
		}
		return false;
	}
}
