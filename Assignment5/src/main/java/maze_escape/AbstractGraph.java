package maze_escape;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class AbstractGraph<V> {

    /**
     * Graph representation:
     * this class implements graph search algorithms on a graph with abstract vertex type V
     * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
     * this abstraction can be used for both directed and undirected graphs
     **/

    public AbstractGraph() {
    }

    /**
     * retrieves all neighbours of the given fromVertex
     * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
     *
     * @param fromVertex
     * @return
     */
    public abstract Set<V> getNeighbours(V fromVertex);

    /**
     * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
     * if the graph is directed, only outgoing edges shall be traversed
     * firstVertex shall be included in the result as well
     * if the graph is connected, all vertices shall be found
     *
     * @param firstVertex the start vertex for the retrieval
     * @return
     */
    public Set<V> getAllVertices(V firstVertex) {
        // Calculate recursively the set of all connected vertices that can be reached from the given start vertex
        Set<V> visited = new HashSet<>();  // Set to store visited vertices
        Set<V> allVertices = new HashSet<>();  // Set to store all vertices

        // Call the recursive helper method to find all connected vertices
        getAllVerticesHelper(firstVertex, visited, allVertices);

        return allVertices;
    }

    private void getAllVerticesHelper(V currentVertex, Set<V> visited, Set<V> allVertices) {
        // Mark the current vertex as visited and add it to the set of all vertices
        visited.add(currentVertex);
        allVertices.add(currentVertex);

        // Get the neighbors of the current vertex
        Set<V> neighbors = getNeighbours(currentVertex);

        // Iterate through the neighbors
        for (V neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                // If the neighbor is not visited, recursively call the helper method
                getAllVerticesHelper(neighbor, visited, allVertices);
            }
        }
    }


    /**
     * Formats the adjacency list of the subgraph starting at the given firstVertex
     * according to the format:
     * vertex1: [neighbour11,neighbour12,…]
     * vertex2: [neighbour21,neighbour22,…]
     * …
     * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
     * if the graph is directed, only outgoing edges shall be traversed
     * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
     *
     * @param firstVertex
     * @return
     */
    public String formatAdjacencyList(V firstVertex) {
        StringBuilder stringBuilder = new StringBuilder("Graph adjacency list:\n");

        // Recursively build the adjacency list including all vertices that can be reached from firstVertex
        //  following a recursive pre-order traversal of a spanning tree
        //  using the above stringBuilder to format the list

        // Create a set to track visited vertices
        Set<V> visited = new HashSet<>();

        // Call the recursive helper method to build the adjacency list
        formatAdjacencyListHelper(firstVertex, visited, stringBuilder);

        // return the result
        return stringBuilder.toString();
    }

    private void formatAdjacencyListHelper(V currentVertex, Set<V> visited, StringBuilder stringBuilder) {
        // Mark the current vertex as visited
        visited.add(currentVertex);

        // Append the current vertex and its neighbors to the adjacency list
        stringBuilder.append(currentVertex).append(": ").append(getNeighbours(currentVertex).toString().replaceAll("\\s+", "")).append("\n");

        // Get the neighbors of the current vertex
        Set<V> neighbors = getNeighbours(currentVertex);

        // Iterate through the neighbors
        for (V neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                // If the neighbor is not visited, recursively call the helper method
                formatAdjacencyListHelper(neighbor, visited, stringBuilder);
            }
        }
    }

    /**
     * represents a directed path of connected vertices in the graph
     */
    public class GPath {
        private Deque<V> vertices = new LinkedList<>();
        private double totalWeight = 0.0;
        private Set<V> visited = new HashSet<>();

        /**
         * representation invariants:
         * 1. vertices contains a sequence of vertices that are neighbours in the graph,
         * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
         * 2. a path with one vertex equal start and target vertex
         * 3. a path without vertices is empty, does not have a start nor a target
         * totalWeight is a helper attribute to capture total path length from a function on two neighbouring vertices
         * visited is a helper set to be able to track visited vertices in searches, only for analysis purposes
         **/
        private static final int DISPLAY_CUT = 10;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%.2f Length=%d visited=%d (",
                            this.totalWeight, this.vertices.size(), this.visited.size()));
            String separator = "";
            int count = 0;
            final int tailCut = this.vertices.size() - 1 - DISPLAY_CUT;
            for (V v : this.vertices) {
                // limit the length of the text representation for long paths.
                if (count < DISPLAY_CUT || count > tailCut) {
                    sb.append(separator).append(v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator).append("...");
                }
                count++;
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * recalculates the total weight of the path from a given weightMapper that calculates the weight of
         * the path segment between two neighbouring vertices.
         *
         * @param weightMapper
         */
        public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
            this.totalWeight = 0.0;
            V previous = null;
            for (V v : this.vertices) {
                // the first vertex of the iterator has no predecessor and hence no weight contribution
                if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
                previous = v;
            }
        }

        public Queue<V> getVertices() {
            return this.vertices;
        }

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath depthFirstSearch(V startVertex, V targetVertex) {

        if (startVertex == null || targetVertex == null) return null;

        Set<V> visited = new HashSet<>();  // Set to store visited vertices
        GPath path = new GPath();  // Create a new path object
        path.visited = visited;   // Set the visited set in the path object

        // Call the recursive helper method to perform DFS and find the path
        boolean found = depthFirstSearchHelper(startVertex, targetVertex, path);

        if (found) {
            // Path found, return the path object
            return path;
        } else {
            // No path found, return null
            return null;
        }
    }

    private boolean depthFirstSearchHelper(V currentVertex, V targetVertex, GPath path) {
        // Mark the current vertex as visited
        path.visited.add(currentVertex);

        // Add the current vertex to the path
        path.vertices.addLast(currentVertex);

        // Check if the current vertex is the target vertex
        if (currentVertex.equals(targetVertex)) {
            return true;  // Target vertex found, return true
        }

        // Get the neighbors of the current vertex
        Set<V> neighbors = getNeighbours(currentVertex);

        // Iterate through the neighbors
        for (V neighbor : neighbors) {
            if (!path.visited.contains(neighbor)) {
                // If the neighbor is not visited, recursively call the DFS helper method
                boolean found = depthFirstSearchHelper(neighbor, targetVertex, path);
                if (found) {
                    return true;  // Target vertex found in the recursive call, return true
                }
            }
        }

        // If the target vertex is not found, remove the current vertex from the path
        path.vertices.removeLast();

        return false;  // Target vertex not found, return false
    }


    /**
     * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath breadthFirstSearch(V startVertex, V targetVertex) {

        if (startVertex == null || targetVertex == null) return null;

        // Calculate the path from start to target by breadth-first-search

        GPath gPath = new GPath();
        gPath.vertices.addLast(targetVertex);
        if (startVertex == targetVertex) {
            gPath.visited.add(startVertex);
            return gPath;
        }

        Queue<V> fifoQueue = new LinkedList<>();
        Map<V, V> visitedFrom = new HashMap<>();

        fifoQueue.offer(startVertex);
        visitedFrom.put(startVertex, null);
        while (fifoQueue.size() > 0) {
            V current = fifoQueue.poll();
            for (V vertex : getNeighbours(current)) {
                gPath.visited.add(vertex);
                if (vertex.equals(targetVertex)) {
                    while (current != null) {
                        gPath.vertices.addFirst(current);
                        current = visitedFrom.get(current);
                    }
                    return gPath;
                } else if (!visitedFrom.containsKey(vertex)) {
                    visitedFrom.put(vertex, current);
                    fifoQueue.offer(vertex);
                }

            }
        }

        return null;    // replace by a proper outcome, if any
    }

    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean marked = false;  // indicates DSP processing has been marked complete for this vertex
        protected double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path towards this node's vertex

        private MSTNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(MSTNode otherMSTNode) {
            return Double.compare(weightSumTo, otherMSTNode.weightSumTo);
        }
    }

    /**
     * Calculates the edge-weighted shortest path from the startVertex to targetVertex in the subgraph
     * according to Dijkstra's algorithm of a minimum spanning tree
     *
     * @param startVertex
     * @param targetVertex
     * @param weightMapper provides a function(v1,v2) by which the weight of an edge from v1 to v2
     *                     can be retrieved or calculated
     * @return the shortest path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath dijkstraShortestPath(V startVertex, V targetVertex,
                                      BiFunction<V, V, Double> weightMapper) {

        if (startVertex == null || targetVertex == null) return null;

        // initialise the result path of the search
        GPath path = new GPath();
        path.visited.add(startVertex);

        // easy target
        if (startVertex.equals(targetVertex)) {
            path.vertices.add(startVertex);
            return path;
        }

        // a minimum spanning tree which tracks for every visited vertex:
        //   a) its (parent) predecessor in the currently shortest path towards this visited vertex
        //   b) the total weight of the currently shortest path towards this visited vertex
        //   c) a mark, indicating whether the current path towards this visited vertex is the final shortest.
        // (you may choose a different approach of tracking the MST of the algorithm, if you wish)
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>();

        // initialise the minimum spanning tree with the startVertex
        MSTNode nearestMSTNode = new MSTNode(startVertex);
        nearestMSTNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, nearestMSTNode);

        while (nearestMSTNode != null) {
            //  continue Dijkstra's algorithm to process nearestMSTNode
            //  mark nodes as you find their current shortest path to be final
            //  if you hit the target: complete the path and bail out !!!
            //  register all visited vertices for statistical purposes
            if (nearestMSTNode.vertex.equals(targetVertex)){
                path.totalWeight += nearestMSTNode.weightSumTo;
                while (nearestMSTNode != null){
                    path.vertices.addFirst(nearestMSTNode.vertex);
                    nearestMSTNode = minimumSpanningTree.getOrDefault(nearestMSTNode.parentVertex, null);
                }
                return path;
            }

            for (V v : getNeighbours(nearestMSTNode.vertex)){
                path.visited.add(v);
                double weight = weightMapper.apply(nearestMSTNode.vertex, v);
                if (!minimumSpanningTree.containsKey(v)){
                    MSTNode newNode = new MSTNode(v);
                    newNode.parentVertex = nearestMSTNode.vertex;
                    newNode.weightSumTo = weight + nearestMSTNode.weightSumTo;
                    minimumSpanningTree.put(v, newNode);
                }
            }
            nearestMSTNode.marked = true;

            // find the next nearest MSTNode that is not marked yet
            nearestMSTNode = minimumSpanningTree
                    .values().stream()
                    .filter(x -> !x.marked)
                    .min(Comparator.comparingDouble(x -> x.weightSumTo)).orElse(null);      // replace by a proper selection
        }

        return null;        // replace by a proper outcome, if any
    }
}
