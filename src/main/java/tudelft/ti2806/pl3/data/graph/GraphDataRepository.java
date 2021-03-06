package tudelft.ti2806.pl3.data.graph;

import tudelft.ti2806.pl3.data.Genome;
import tudelft.ti2806.pl3.data.gene.Gene;
import tudelft.ti2806.pl3.data.gene.GeneData;
import tudelft.ti2806.pl3.data.meta.MetaParser;
import tudelft.ti2806.pl3.util.observable.LoadingObservable;
import tudelft.ti2806.pl3.util.observers.LoadingObserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphDataRepository extends AbstractGraphData implements LoadingObservable {

	private final ArrayList<LoadingObserver> observers = new ArrayList<>();
	private final List<GraphParsedObserver> graphParsedObserver = new ArrayList<>();

	/**
	 * Construct a empty {@code GraphDataRepository}.
	 */
	public GraphDataRepository() {
	}

	/**
	 * TODO: THIS CONSTRUCTOR IS ONLY USED FOR TESTING.
	 * Construct a instance of {@code GraphDataRepository}.
	 *
	 * @param nodes
	 * 		the nodes of the graph
	 * @param edges
	 * 		the edges of the graph
	 * @param genomes
	 * 		all {@link Genome} that are present in the graph
	 */
	public GraphDataRepository(List<DataNode> nodes, List<Edge> edges,
			List<Genome> genomes) {
		this.nodes = nodes;
		this.edges = edges;
		this.genomes = genomes;
	}

	public void setNodes(List<DataNode> nodes) {
		this.nodes = nodes;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public void setGenomes(List<Genome> genomes) {
		this.genomes = genomes;
	}

	@Override
	public List<DataNode> getNodes() {
		return this.getNodeListClone();
	}

	@Override
	public List<Edge> getEdges() {
		return this.getEdgeListClone();
	}

	@Override
	public List<Gene> getGenes() {
		return this.genes;
	}

	@Override
	public List<Genome> getGenomes() {
		return this.getGenomeClone();
	}

	@Override
	public Map<Gene, DataNode> getGeneToStartNodeMap() {
		return this.geneToStartNodeMap;
	}

	/**
	 * Parse a node and edge file of a graph into a {@code GraphData} without metadata.
	 *
	 * @param nodesFile
	 * 		the file of nodes to be read
	 * @param edgesFile
	 * 		the file of edges to be read
	 * @throws FileNotFoundException
	 * 		if the file is not found
	 */
	public void parseGraph(File nodesFile, File edgesFile, GeneData geneData) throws FileNotFoundException {
		parseGraph(nodesFile, edgesFile, null, geneData);
	}

	/**
	 * Parse a node and edge file of a graph into a {@code GraphData} with metadata.
	 *
	 * @param nodesFile
	 * 		the file of nodes to be read
	 * @param edgesFile
	 * 		the file of edges to be read
	 * @param metaFile
	 * 		the metadata file to be read
	 * @throws FileNotFoundException
	 * 		if the file is not found
	 */
	public void parseGraph(File nodesFile, File edgesFile, File metaFile, GeneData geneData)
			throws FileNotFoundException {
		notifyLoadingObservers(true);
		geneToStartNodeMap = new HashMap<>(geneData.getGenes().size());
		genes = new ArrayList<>();

		Map<String, Genome> genomeMap = new HashMap<>();
		Map<Integer, DataNode> nodeMap = parseNodes(nodesFile, genomeMap, geneData);
		genes.sort(Comparator.<Gene>naturalOrder());
		List<DataNode> nodeList = new ArrayList<>();
		nodeList.addAll(nodeMap.values());
		List<Genome> genomeList = new ArrayList<>();
		genomeList.addAll(genomeMap.values());

		setNodes(nodeList);
		setEdges(parseEdges(edgesFile, nodeMap));
		setGenomes(genomeList);
		if (metaFile != null) {
			MetaParser.parseMeta(metaFile, genomeMap);
		}

		notifyLoadingObservers(false);
		notifyGraphParsedObservers();
	}

	/**
	 * Load metadata into the graph after it has been constructed.
	 *
	 * @param metaFile
	 * 		the metadata file to read
	 * @throws FileNotFoundException
	 * 		if the file cannot be found
	 */
	public void loadMetaData(File metaFile) throws FileNotFoundException {
		Map<String, Genome> genomeMap = new HashMap<>();
		for (Genome g : genomes) {
			genomeMap.put(g.getIdentifier(), g);
		}
		MetaParser.parseMeta(metaFile, genomeMap);
	}

	/**
	 * Parse the nodes file, creating nodes from the file its data.
	 *
	 * @param nodesFile
	 * 		the file of nodes to be read
	 * @param genomeMap
	 * 		{@link Genome} mapped on their identifier
	 * @return a list of all nodes, mapped by their node id
	 * @throws FileNotFoundException
	 * 		if the file is not found
	 */
	public Map<Integer, DataNode> parseNodes(File nodesFile, Map<String, Genome> genomeMap,
			GeneData geneData) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(nodesFile), StandardCharsets.UTF_8));
		Map<Integer, DataNode> nodes = new HashMap<>();
		try {
			while (br.ready()) {
				DataNode node = parseNode(br, genomeMap);
				if (geneData != null) {
					addRefLabels(node, geneData);
				}
				nodes.put(node.getId(), node);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodes;
	}

	/**
	 * Adds gene reference labels to node.
	 *
	 * @param node
	 * 		the node to which labels can be added
	 * @param geneData
	 * 		the gene annotation dataset
	 */
	private void addRefLabels(DataNode node, GeneData geneData) {
		int start = node.getRefStartPoint();
		int end = node.getRefEndPoint();
		Gene g = null;

		boolean started = false;
		for (int i = start; i <= end; i++) {
			if (started) {
				node.addLabel(geneData.getLabel(g.getName()));
			} else if (geneData.getGeneStart().containsKey(i)) {
				g = geneData.getGeneStart().get(i);
				geneToStartNodeMap.put(g, node);
				genes.add(g);
				node.addLabel(geneData.getStartLabel(g.getName()));
				started = true;
			} else if (geneData.getGeneEnd().containsKey(i)) {
				g = geneData.getGeneEnd().get(i);
				node.addLabel(geneData.getEndLabel(g.getName()));
			}
		}
	}

	/**
	 * Parses the next two lines of the scanner into a Node.
	 *
	 * @param br
	 * 		the BufferedReader with two available lines to read
	 * @return the read node
	 */
	protected DataNode parseNode(BufferedReader br,
			Map<String, Genome> genomes) {
		String[] indexData = new String[0];
		try {
			String line = br.readLine();
			if (line != null) {
				indexData = line.replaceAll("[> ]", "").split("\\|");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		DataNode node = null;
		try {
			node = new DataNode(Integer.parseInt(indexData[0]),
					parseGenomeIdentifiers(indexData[1].split(","), genomes),
					Integer.parseInt(indexData[2]),
					Integer.parseInt(indexData[3]),
					br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return node;
	}

	private Set<Genome> parseGenomeIdentifiers(String[] identifiers,
			Map<String, Genome> genomes) {
		Set<Genome> result = new HashSet<>(identifiers.length);
		for (int i = 0; i < identifiers.length; i++) {
			identifiers[i] = identifiers[i].replaceAll("-", "_");
			Genome genome = genomes.get(identifiers[i]);
			if (genome == null) {
				genome = new Genome(identifiers[i]);
				genomes.put(identifiers[i], genome);
			}
			result.add(genome);
		}
		return result;
	}

	/**
	 * Parse the edges file, adding the connections between the nodes.
	 *
	 * @param edgesFile
	 * 		the file of edges to be read
	 * @param nodes
	 * 		a list of nodes mapped by their node id.
	 * @throws FileNotFoundException
	 * 		if the file is not found
	 */
	public List<Edge> parseEdges(File edgesFile, Map<Integer, DataNode> nodes) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(edgesFile), StandardCharsets.UTF_8));
		List<Edge> list = new ArrayList<>();
		try {
			while (br.ready()) {
				String line = br.readLine();
				if (line != null) {
					String[] index = line.split(" ");
					DataNode nodeFrom = nodes.get(Integer.parseInt(index[0]));
					DataNode nodeTo = nodes.get(Integer.parseInt(index[1]));
					list.add(new Edge(nodeFrom, nodeTo));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Search for the node in the graph with the given id.
	 *
	 * @param id
	 * 		the id of the node to search
	 * @return the found node<br>
	 * {@code null} if there is no node with this id in the graph
	 */
	public DataNode getNodeByNodeId(int id) {
		for (DataNode node : nodes) {
			if (node.getId() == id) {
				return node;
			}
		}
		return null;
	}

	@Override
	public AbstractGraphData getOrigin() {
		return this;
	}

	@Override
	public void addLoadingObserver(LoadingObserver loadingObserver) {
		observers.add(loadingObserver);
	}

	@Override
	public void addLoadingObserversList(ArrayList<LoadingObserver> loadingObservers) {
		loadingObservers.forEach(this::addLoadingObserver);
	}

	@Override
	public void deleteLoadingObserver(LoadingObserver loadingObserver) {
		observers.remove(loadingObserver);
	}

	@Override
	public void notifyLoadingObservers(Object loading) {
		for (LoadingObserver observer : observers) {
			observer.update(this, loading);
		}
	}

	public void addGraphParsedObserver(GraphParsedObserver o) {
		graphParsedObserver.add(o);
	}

	public void removeGraphParsedObserver(GraphParsedObserver o) {
		graphParsedObserver.remove(o);
	}

	private void notifyGraphParsedObservers() {
		graphParsedObserver.forEach(GraphParsedObserver::graphParsed);
	}
}
