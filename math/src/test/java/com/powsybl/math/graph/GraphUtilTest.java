package com.powsybl.math.graph;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class GraphUtilTest {

    @Test
    public void removeIsolatedVertices() {
        UndirectedGraph<Object, Object> graph = new UndirectedGraphImpl<>();

        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(1, 2, null);
        graph.setVertexObject(0, new Object());

        assertEquals(3, graph.getVertexCount());
        // Vertex 0 is isolated but has an associated object: it must not be removed.
        GraphUtil.removeIsolatedVertices(graph);
        assertEquals(3, graph.getVertexCount());
        graph.setVertexObject(0, null);
        // Now vertex 0 must be removed.
        GraphUtil.removeIsolatedVertices(graph);
        assertEquals(2, graph.getVertexCount());
    }

    @Test
    public void vertexCountTest() {
        UndirectedGraph<Object, Object> graph = new UndirectedGraphImpl<>();
        assertEquals(0, graph.getVertexCount());

        graph.addVertex(2);
        assertEquals(0, graph.addVertex());
        assertEquals(1, graph.addVertex());
        assertEquals(3, graph.getVertexCount());

        graph.addEdge(0, 2, null);

        GraphUtil.removeIsolatedVertices(graph);
        assertEquals(2, graph.getVertexCount());
        assertArrayEquals(graph.getVertices(), new int[]{0, 2});
    }

}
