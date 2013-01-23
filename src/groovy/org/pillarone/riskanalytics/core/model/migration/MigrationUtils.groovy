package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber


class MigrationUtils {

    public static List<AbstractMigration> getMigrationChain(List<AbstractMigration> allMigrations, VersionNumber from, VersionNumber to) {
        Graph graph = new Graph()

        for (AbstractMigration migration in allMigrations) {
            Node f = graph.getNode(migration.from.toString())
            Node t = graph.getNode(migration.to.toString())

            f.createEdge(t)
            t.createEdge(f)
        }

        Node start = graph.getNode(from.toString())
        start.distance = 0
        List<Node> allNodes = graph.nodes.clone().sort { it.distance }
        while (!allNodes.empty) {
            Node current = allNodes[0]
            if (current.distance == Integer.MAX_VALUE) break;

            allNodes.remove(current)
            for (Edge neighbour in current.edges) {
                if (allNodes.contains(neighbour.to)) {
                    int newDistance = current.distance + neighbour.distance
                    if (newDistance < neighbour.to.distance) {
                        neighbour.to.distance = newDistance
                        neighbour.to.previous = current
                    }
                }
            }
            allNodes = allNodes.clone().sort { it.distance }
        }

        Node end = graph.getNode(to.toString())
        if (end.distance == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("No migration path found from ${from.toString()} to ${to.toString()}")
        }

        List<AbstractMigration> migrations = []
        Node current = end
        while (current.previous != null) {
            Node prev = current.previous
            AbstractMigration migration = allMigrations.find { it.from.toString() == prev.name && it.to.toString() == current.name}
            migrations.add(0, migration)
            current = prev
        }
        return migrations
    }


    private static class Graph {
        ArrayList<Node> nodes = []

        Node getNode(String name) {
            Node node = nodes.find { it.name == name }
            if (node == null) {
                node = new Node(name: name)
                nodes << node
            }
            return node
        }
    }

    private static class Node {

        String name

        int distance = Integer.MAX_VALUE
        Node previous
        boolean visited = false

        List<Edge> edges = []

        Edge createEdge(Node to) {
            Edge edge = edges.find { it.to == to }
            if (edge == null) {
                edge = new Edge(from: this, to: to)
                edges << edge
            }
            return edge
        }

    }

    private static class Edge {
        int distance = 1
        Node from
        Node to
    }
}
