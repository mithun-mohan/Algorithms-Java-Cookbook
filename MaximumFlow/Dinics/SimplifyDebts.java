import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.lang.Math.min;

/**
 * Implementation of algorithm to simplify debts using Dinic's network flow algorithm. The algorithm picks edges one at a time and
 * runs the network flow algorithm to generates the residual graph, which is then again fed back to the network flow algorithm
 * until there are no more non visited edges.
 *
 * <p>Time Complexity: O(E²V²)
 *
 * @author Mithun Mohan K, mithunmk93@gmail.com
 */
public class SimplifyDebts {
  private static final long OFFSET = 1000000000L;
  private static Set<Long> visitedEdges;

  public static void main(String[] args) {
    createGraphForDebts();
  }

  /**
   * This example graph is taken from my Medium blog post.
   * Here Alice, Bob, Charlie, David, Ema, Fred and Gabe are represented by vertices from 0 to 6 respectively.
   */
  private static void createGraphForDebts() {
    //  List of all people in the group
    String[] person = { "Alice", "Bob", "Charlie", "David", "Ema", "Fred", "Gabe"};
    int n = person.length;
    //  Creating a graph with n vertices
    Dinics solver = new Dinics(n, person);
    //  Adding edges to the graph
    solver = addAllTransactions(solver);

    System.out.println();
    System.out.println("Simplifying Debts...");
    System.out.println("--------------------");
    System.out.println();

    //  Map to keep track of visited edges
    visitedEdges = new HashSet<>();
    Integer edgePos;

    while((edgePos = getNonVisitedEdge(solver.getEdges())) != null) {
      //  Force recomputation of subsequent flows in the graph
      solver.recompute();
      //  Set source and sink in the flow graph
      Dinics.Edge firstEdge = solver.getEdges().get(edgePos);
      solver.setSource(firstEdge.from);
      solver.setSink(firstEdge.to);
      //  Initialize the residual graph to be same as the given graph
      List<Dinics.Edge>[] residualGraph = solver.getGraph();
      List<Dinics.Edge> newEdges = new ArrayList<>();

      for(List<Dinics.Edge> allEdges : residualGraph) {
        for(Dinics.Edge edge : allEdges) {
          long remainingFlow = ((edge.flow < 0) ? edge.capacity : (edge.capacity - edge.flow));
          //  If there is capacity remaining in the graph, then add the remaining capacity as an edge
          //  so that it can be used for optimizing other debts within the graph
          if(remainingFlow > 0) {
            newEdges.add(new Dinics.Edge(edge.from, edge.to, remainingFlow));
          }
        }
      }

      //  Get the maximum flow between the source and sink
      long maxFlow = solver.getMaxFlow();
      //  Mark the edge from source to sink as visited
      int source = solver.getSource();
      int sink = solver.getSink();
      visitedEdges.add(getHashKeyForEdge(source, sink));
      //  Create a new graph
      solver = new Dinics(n, person);
      //  Add edges having remaining capacity
      solver.addEdges(newEdges);
      //  Add an edge from source to sink in the new graph with obtained maximum flow as it's weight
      solver.addEdge(source, sink, maxFlow);
    }
    solver.reduceLoops();
    //  Print the edges in the graph
    solver.printEdges();
    System.out.println();
  }

  private static Dinics addAllTransactions(Dinics solver) {
    //  Transactions made by Bob
    solver.addEdge(1, 2, 40);
    //  Transactions made by Charlie
    solver.addEdge(2, 3, 20);
    solver.addEdge(2, 1, 3);
    //  Transactions made by David
    solver.addEdge(3, 4, 50);
    //  Transactions made by Fred
    solver.addEdge(5, 1, 10);
    solver.addEdge(5, 2, 30);
    solver.addEdge(5, 3, 10);
    solver.addEdge(5, 4, 10);
    //  Transactions made by Gabe
    solver.addEdge(6, 1, 30);
    solver.addEdge(6, 3, 10);
    return solver;
  }

  /**
  * Get any non visited edge in the graph
  * @param edges list of all edges in the graph
  * @return index of a non visited edge
  */
  private static Integer getNonVisitedEdge(List<Dinics.Edge> edges) {
    Integer edgePos = null;
    int curEdge = 0;
    for(Dinics.Edge edge : edges) {
      if(!visitedEdges.contains(getHashKeyForEdge(edge.from, edge.to))) {
        edgePos = curEdge;
      }
      curEdge++;
    }
    return edgePos;
  }

  /**
  * Get a unique hash key for a given edge
  * @param u the starting vertex in the edge
  * @param v the ending vertex in the edge
  * @return a unique hash key
  */
  private static Long getHashKeyForEdge(int u, int v) {
    return u * OFFSET + v;
  }
}


/**
 * Implementation of Dinic's network flow algorithm. The algorithm works by first constructing a
 * level graph using a BFS and then finding augmenting paths on the level graph using multiple DFSs.
 *
 * <p>Time Complexity: O(EV²)
 *
 * @link https://github.com/williamfiset/Algorithms
 */
class Dinics extends NetworkFlowSolverBase {

  private int[] level;

  /**
   * Creates an instance of a flow network solver. Use the {@link #addEdge} method to add edges to
   * the graph.
   *
   * @param n - The number of nodes in the graph including source and sink nodes.
   */
  public Dinics(int n, String[] vertexLabels) {
    super(n, vertexLabels);
    level = new int[n];
  }

  @Override
  public void solve() {
    // next[i] indicates the next unused edge index in the adjacency list for node i. This is part
    // of the Shimon Even and Alon Itai optimization of pruning deads ends as part of the DFS phase.
    int[] next = new int[n];

    while (bfs()) {
      Arrays.fill(next, 0);
      // Find max flow by adding all augmenting path flows.
      for (long f = dfs(s, next, INF); f != 0; f = dfs(s, next, INF)) {
        maxFlow += f;
      }
    }

    for (int i = 0; i < n; i++) if (level[i] != -1) minCut[i] = true;
  }

  // Do a BFS from source to sink and compute the depth/level of each node
  // which is the minimum number of edges from that node to the source.
  private boolean bfs() {
    Arrays.fill(level, -1);
    level[s] = 0;
    Deque<Integer> q = new ArrayDeque<>(n);
    q.offer(s);
    while (!q.isEmpty()) {
      int node = q.poll();
      for (Edge edge : graph[node]) {
        long cap = edge.remainingCapacity();
        if (cap > 0 && level[edge.to] == -1) {
          level[edge.to] = level[node] + 1;
          q.offer(edge.to);
        }
      }
    }
    return level[t] != -1;
  }

  private long dfs(int at, int[] next, long flow) {
    if (at == t) return flow;
    final int numEdges = graph[at].size();

    for (; next[at] < numEdges; next[at]++) {
      Edge edge = graph[at].get(next[at]);
      long cap = edge.remainingCapacity();
      if (cap > 0 && level[edge.to] == level[at] + 1) {

        long bottleNeck = dfs(edge.to, next, min(flow, cap));
        if (bottleNeck > 0) {
          edge.augment(bottleNeck);
          return bottleNeck;
        }
      }
    }
    return 0;
  }
}


abstract class NetworkFlowSolverBase {

  // To avoid overflow, set infinity to a value less than Long.MAX_VALUE;
  protected static final long INF = Long.MAX_VALUE / 2;

  public static class Edge {
    public int from, to;
    public String fromLabel, toLabel;
    public Edge residual;
    public long flow, cost;
    public final long capacity, originalCost;

    public Edge(int from, int to, long capacity) {
      this(from, to, capacity, 0 /* unused */);
    }

    public Edge(int from, int to, long capacity, long cost) {
      this.from = from;
      this.to = to;
      this.capacity = capacity;
      this.originalCost = this.cost = cost;
    }

    public boolean isResidual() {
      return capacity == 0;
    }

    public long remainingCapacity() {
      return capacity - flow;
    }

    public void augment(long bottleNeck) {
      flow += bottleNeck;
      residual.flow -= bottleNeck;
    }

    public String toString(int s, int t) {
      String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
      String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
      return String.format(
          "Edge %s -> %s | flow = %d | capacity = %d | is residual: %s",
          u, v, flow, capacity, isResidual());
    }
  }

  // Inputs: n = number of nodes, s = source, t = sink
  protected int n, s, t;

  protected long maxFlow;
  protected long minCost;

  protected boolean[] minCut;
  protected List<Edge>[] graph;
  protected String[] vertexLabels;
  protected List<Edge> edges;

  // 'visited' and 'visitedToken' are variables used for graph sub-routines to
  // track whether a node has been visited or not. In particular, node 'i' was
  // recently visited if visited[i] == visitedToken is true. This is handy
  // because to mark all nodes as unvisited simply increment the visitedToken.
  private int visitedToken = 1;
  private int[] visited;

  // Indicates whether the network flow algorithm has ran. We should not need to
  // run the solver multiple times, because it always yields the same result.
  protected boolean solved;

  /**
   * Creates an instance of a flow network solver. Use the {@link #addEdge} method to add edges to
   * the graph.
   *
   * @param n - The number of nodes in the graph including source and sink nodes.
   */
  public NetworkFlowSolverBase(int n, String[] vertexLabels) {
    this.n = n;
    initializeGraph();
    assignLabelsToVertices(vertexLabels);
    minCut = new boolean[n];
    visited = new int[n];
    edges = new ArrayList<>();
  }

  // Construct an empty graph with n nodes including the source and sink nodes.
  private void initializeGraph() {
    graph = new List[n];
    for (int i = 0; i < n; i++) graph[i] = new ArrayList<Edge>();
  }

  // Add labels to vertices in the graph.
  private void assignLabelsToVertices(String[] vertexLabels) {
    if(vertexLabels.length != n)
      throw new IllegalArgumentException(String.format("You must pass %s number of labels", n));
    this.vertexLabels = vertexLabels;
  }

  /**
   * Adds a list of directed edges (and residual edges) to the flow graph.
   *
   * @param edges - A list of all edges to be added to the flow graph.
   */
  public void addEdges(List<Edge> edges) {
    if (edges == null) throw new IllegalArgumentException("Edges cannot be null");
    for(Edge edge : edges) {
      addEdge(edge.from, edge.to, edge.capacity);
    }
  }

  /**
   * Adds a directed edge (and residual edge) to the flow graph.
   *
   * @param from - The index of the node the directed edge starts at.
   * @param to - The index of the node the directed edge ends at.
   * @param capacity - The capacity of the edge.
   */
  public void addEdge(int from, int to, long capacity) {
    if (capacity < 0) throw new IllegalArgumentException("Capacity < 0");
    Edge e1 = new Edge(from, to, capacity);
    Edge e2 = new Edge(to, from, 0);
    e1.residual = e2;
    e2.residual = e1;
    graph[from].add(e1);
    graph[to].add(e2);
    edges.add(e1);
  }

  /** Cost variant of {@link #addEdge(int, int, int)} for min-cost max-flow */
  public void addEdge(int from, int to, long capacity, long cost) {
    Edge e1 = new Edge(from, to, capacity, cost);
    Edge e2 = new Edge(to, from, 0, -cost);
    e1.residual = e2;
    e2.residual = e1;
    graph[from].add(e1);
    graph[to].add(e2);
    edges.add(e1);
  }

  // Marks node 'i' as visited.
  public void visit(int i) {
    visited[i] = visitedToken;
  }

  // Returns whether or not node 'i' has been visited.
  public boolean visited(int i) {
    return visited[i] == visitedToken;
  }

  // Resets all nodes as unvisited. This is especially useful to do
  // between iterations of finding augmenting paths, O(1)
  public void markAllNodesAsUnvisited() {
    visitedToken++;
  }

  /**
   * Returns the graph after the solver has been executed. This allow you to inspect the {@link
   * Edge#flow} compared to the {@link Edge#capacity} in each edge. This is useful if you want to
   * figure out which edges were used during the max flow.
   */
  public List<Edge>[] getGraph() {
    execute();
    return graph;
  }

  /**
   * Returns all edges in this flow network
   */
  public List<Edge> getEdges() {
    return edges;
  }

  // Returns the maximum flow from the source to the sink.
  public long getMaxFlow() {
    execute();
    return maxFlow;
  }

  // Returns the min cost from the source to the sink.
  // NOTE: This method only applies to min-cost max-flow algorithms.
  public long getMinCost() {
    execute();
    return minCost;
  }

  // Returns the min-cut of this flow network in which the nodes on the "left side"
  // of the cut with the source are marked as true and those on the "right side"
  // of the cut with the sink are marked as false.
  public boolean[] getMinCut() {
    execute();
    return minCut;
  }

  /**
   * Used to set the source for this flow network 
   */
  public void setSource(int s) {
    this.s = s;
  }

  /**
   * Used to set the sink for this flow network 
   */
  public void setSink(int t) {
    this.t = t;
  }

  /**
   * Get source for this flow network 
   */
  public int getSource() {
    return s;
  }

  /**
   * Get sink for this flow network 
   */
  public int getSink() {
    return t;
  }

  /**
   * Set 'solved' flag to false to force recomputation of subsequent flows.
   */
  public void recompute() {
    solved = false;
  }

  /**
   * reduce loops (bidirectional edges) in resulting graph
   */
  public void reduceLoops() {
    Predicate<Edge> isDirected = (e) -> e.from > e.to; // constant arbitrary order
    UnaryOperator<Edge> reverse = e -> new Edge(e.to, e.from, -e.capacity);
    // keyMapper could be Identity (it->it) if proper hashCode() and equals() implemented in Edge
    Function<Edge, List<Integer>> keyMapper = e -> Arrays.asList(e.from, e.to);
    // sum(capacity) group by keyMapper implementation
    Map<List<Integer>, Long> groupedMap = edges.stream()
            .map(e -> isDirected.test(e) ? e : reverse.apply(e))
            .collect(Collectors.toMap(
                    keyMapper,
                    e -> e.capacity,
                    Long::sum
            ));
    // reverse negative edges
    Predicate<Edge> isNegative = (e) -> e.capacity < 0;
    edges = groupedMap.entrySet().stream()
            .map(it -> new Edge(it.getKey().get(0), it.getKey().get(1), it.getValue()))
            .filter(it -> it.capacity != 0)
            .map(e -> isNegative.test(e) ? reverse.apply(e) : e)
            .collect(Collectors.toList());
  }
  /**
   * Print all edges.
   */
  public void printEdges() {
    for(Edge edge : edges) {
      System.out.println(String.format("%s ----%s----> %s", vertexLabels[edge.from], edge.capacity, vertexLabels[edge.to]));
    }
  }

  // Wrapper method that ensures we only call solve() once
  private void execute() {
    if (solved) return;
    solved = true;
    solve();
  }

  // Method to implement which solves the network flow problem.
  public abstract void solve();
}
