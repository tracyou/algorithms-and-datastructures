package maze_escape;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ExtraAbstractGraphTest {

    @Test
    public void testGetAllVertices() {
        // Create a sample graph
        AbstractGraph<Integer> graph = new AbstractGraph<Integer>() {
            @Override
            public Set<Integer> getNeighbours(Integer fromVertex) {
                // Define the neighbors for each vertex
                Set<Integer> neighbors = Set.of(2, 3, 4);
                return neighbors;
            }
        };

        // Test case 1: Start vertex is 1
        Set<Integer> allVertices = graph.getAllVertices(1);
        Set<Integer> expectedVertices = Set.of(1, 2, 3, 4);
        Assertions.assertEquals(expectedVertices, allVertices);

        // Test case 2: Start vertex is 3
        allVertices = graph.getAllVertices(3);
        expectedVertices = Set.of(3, 2, 4);
        Assertions.assertEquals(expectedVertices, allVertices);
    }

    @Test
    public void testFormatAdjacencyList() {
        // Create a sample graph
        AbstractGraph<Integer> graph = new AbstractGraph<>() {
            @Override
            public Set<Integer> getNeighbours(Integer fromVertex) {
                // Define the neighbors for each vertex
                Set<Integer> neighbors;
                switch (fromVertex) {
                    case 1:
                        neighbors = Set.of(2, 3);
                        break;
                    case 2:
                        neighbors = Set.of(3, 4);
                        break;
                    case 3:
                        neighbors = Set.of(4);
                        break;
                    default:
                        neighbors = Set.of();
                }
                return neighbors;
            }
        };

        // Test case: Start vertex is 1
        String adjacencyList = graph.formatAdjacencyList(1);
        String expectedList = "Graph adjacency list:\n" +
                "1: [2,3]\n" +
                "2: [3,4]\n" +
                "3: [4]\n" +
                "4: []\n";
        Assertions.assertEquals(expectedList, adjacencyList);
    }

    @Test
    public void testDepthFirstSearch() {
        // Create a sample graph
        AbstractGraph<Integer> graph = new AbstractGraph<Integer>() {
            @Override
            public Set<Integer> getNeighbours(Integer fromVertex) {
                // Define the neighbors for each vertex
                Set<Integer> neighbors;
                switch (fromVertex) {
                    case 1:
                        neighbors = Set.of(2, 3);
                        break;
                    case 2:
                        neighbors = Set.of(3, 4);
                        break;
                    case 3:
                        neighbors = Set.of(4);
                        break;
                    default:
                        neighbors = Set.of();
                }
                return neighbors;
            }
        };

        // Test case 1: Start vertex is 1, target vertex is 4
        AbstractGraph<Integer>.GPath path = graph.depthFirstSearch(1, 4);
        Assertions.assertNotNull(path);
        Assertions.assertEquals(Set.of(1, 2, 3, 4), path.getVisited());

        // Test case 2: Start vertex is 2, target vertex is 3
        path = graph.depthFirstSearch(2, 3);
        Assertions.assertNotNull(path);
        Assertions.assertEquals(Set.of(2, 3), path.getVisited());

        // Test case 3: Start vertex is 1, target vertex is 5 (nonexistent)
        path = graph.depthFirstSearch(1, 5);
        Assertions.assertNull(path);
    }
}
