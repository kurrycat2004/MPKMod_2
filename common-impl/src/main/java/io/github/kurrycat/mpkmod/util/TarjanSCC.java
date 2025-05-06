package io.github.kurrycat.mpkmod.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic implementation of Tarjan's algorithm to find strongly connected components in a directed graph.
 *
 * @param <T> type of the graph's nodes
 */
public class TarjanSCC<T> {
    private final Map<T, List<T>> graph;
    private final Map<T, Integer> indexMap = new HashMap<>();
    private final Map<T, Integer> lowlinkMap = new HashMap<>();
    private final Deque<T> stack = new ArrayDeque<>();
    private final Set<T> onStack = new HashSet<>();
    private final List<Set<T>> sccs = new ArrayList<>();
    private int index = 0;

    /**
     * Initializes the algorithm and computes SCCs for the given graph.
     *
     * @param graph adjacency list representation of the directed graph
     */
    public TarjanSCC(Map<T, List<T>> graph) {
        this.graph = graph;
        for (T v : graph.keySet()) {
            if (!indexMap.containsKey(v)) {
                dfs(v);
            }
        }
    }

    private void dfs(T v) {
        indexMap.put(v, index);
        lowlinkMap.put(v, index);
        index++;
        stack.push(v);
        onStack.add(v);

        for (T w : graph.getOrDefault(v, Collections.emptyList())) {
            if (!indexMap.containsKey(w)) {
                dfs(w);
                lowlinkMap.put(v, Math.min(lowlinkMap.get(v), lowlinkMap.get(w)));
            } else if (onStack.contains(w)) {
                lowlinkMap.put(v, Math.min(lowlinkMap.get(v), indexMap.get(w)));
            }
        }

        if (lowlinkMap.get(v).equals(indexMap.get(v))) {
            Set<T> component = new LinkedHashSet<>();
            T w;
            do {
                w = stack.pop();
                onStack.remove(w);
                component.add(w);
            } while (!w.equals(v));
            sccs.add(component);
        }
    }

    /**
     * Returns the list of strongly connected components, each as a set of nodes.
     */
    public List<Set<T>> getSCCs() {
        return Collections.unmodifiableList(sccs);
    }
}