package tudelft.ti2806.pl3.sidebar.phylotree;

import newick.NewickParser;
import tudelft.ti2806.pl3.ScreenSize;
import tudelft.ti2806.pl3.View;
import tudelft.ti2806.pl3.visualization.GraphController;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Phyloview is a view for the phylogenetic file. The user can select multiple
 * genomes or common ancestors.
 * 
 * <p>
 * Created by Kasper on 20-5-2015.
 */
public class PhyloView extends JPanel implements View {
	private JTree jTree;
	private List<String> selected = new ArrayList<>();
	private PhyloController phyloController;

	/**
	 * Phylo view constructs a Jtree object with our .nwk tree file.
	 *
	 * @param tree
	 * 		Phylogenetic tree parsed by the NewickParser
	 * @param graphController
	 * 		Controller of the graph view
	 */
	public PhyloView(NewickParser.TreeNode tree, GraphController graphController) {
		this.phyloController = new PhyloController(this, graphController);

		jTree = new JTree(phyloController.parseTree(tree));
		int width = ScreenSize.getInstance().getSidebarWidth() - 10;
		int height = ScreenSize.getInstance().getHeight() - 100;

		setUI(width, height);
		setUpLook();
		phyloController.expandTree();
		setListener();
	}

	/**
	 * Setup the UI of the view.
	 *
	 * @param width
	 * 		the width of the panel
	 * @param height
	 * 		the height of the panel
	 */
	private void setUI(int width, int height) {
		JLabel header = new JLabel("Select Genomes");
		header.setPreferredSize(new Dimension(width, 50));

		JScrollPane scroller = new JScrollPane(jTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(width, (int) (height / 1.1)));
		scroller.setMinimumSize(new Dimension(width, height));

		this.add(Box.createHorizontalGlue());
		this.add(header);
		add(Box.createVerticalGlue());
		this.add(scroller);
		add(Box.createVerticalGlue());

		JButton button = createButton();
		this.add(button);
		button.setPreferredSize(new Dimension(200, 50));
		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * Set the icons of the JTree.
	 */
	private void setUpLook() {
		ImageIcon childIcon = new ImageIcon("pictures/bacteria_small.jpg");
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(childIcon);
		jTree.setCellRenderer(renderer);
	}


	/**
	 * Set up the listener for clicking on the phylogentic tree.
	 */
	public void setListener() {
		jTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		jTree.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener() {
					@Override
					public void valueChanged(
							TreeSelectionEvent treeSelectionEvent) {
						TreePath[] treePath = jTree.getSelectionPaths();
						for (TreePath path : treePath) {
							DefaultMutableTreeNode select = (DefaultMutableTreeNode) path
									.getLastPathComponent();
							if (select.toString().equals("Common ancestor")) {
								selected.addAll(getChildsOfAncestor(select));
							} else {
								selected.add(select.toString());
							}
						}
					}
				});
	}

	/**
	 * Return the genomes that are selected.
	 *
	 * @return selected genomes
	 */
	public List<String> getSelected() {
		return selected;
	}

	/**
	 * Resets the selected genomes.
	 */
	public void resetSelected() {
		selected = new ArrayList<>();
	}
	
	/**
	 * Get the genomes of a Common ancestor.
	 *
	 * @param name
	 * 		of node that is selected.
	 * @return All the genomes of the common ancestor.
	 */
	private List<String> getChildsOfAncestor(DefaultMutableTreeNode name) {
		List<String> selected = new ArrayList<>();
		Enumeration<TreeNode> children = name.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode next = (DefaultMutableTreeNode) children
					.nextElement();
			if (next.toString().equals("Common ancestor")) {
				selected.addAll(getChildsOfAncestor(next));
			} else {
				selected.add(next.toString());
			}
		}
		return selected;
	}

	/**
	 * Create the submit button.
	 *
	 * @return the submit button
	 */
	private JButton createButton() {
		JButton button = new JButton("Update");
		button.addActionListener(phyloController);
		return button;
	}

	/**
	 * Return the panel that displays the JTree.
	 *
	 * @return the panel of the view
	 */
	@Override
	public Component getPanel() {
		return this;
	}

	@Override
	public PhyloController getController() {
		return phyloController;
	}

	public JTree getjTree() {
		return jTree;
	}
}
