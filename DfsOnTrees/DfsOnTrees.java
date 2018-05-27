/*  Kickstart Round C 2018 : Problem A. Planet Distance
 *  @author : Mithun Mohan K
 *  handle@codechef : umbreon
 */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.math.*;
 
import java.util.*;
import java.lang.*;
import java.io.*;
 
@SuppressWarnings("unchecked")
public class DfsOnTrees implements Runnable {

  static int[] from, to;

  static int nodeInCycle, nodeInCycleParent, qSize;
  static int[] parent, depth, q;
  static boolean[] visited;
  static int[][] adj;

  public static int[][] generateAdjList(int n, int[] from, int[] to)
  {
    int[][] g = new int[n][];
    int[] p = new int[n];
    for (int f : from)
      p[f]++;
    for (int t : to)
      p[t]++;
    for (int i = 0; i < n; i++)
      g[i] = new int[p[i]];
    for (int i = 0; i < from.length; i++)
    {
      g[from[i]][--p[from[i]]] = to[i];
      g[to[i]][--p[to[i]]] = from[i];
    }
    return g;
  }

  public static void dfs(int cur, int parentVal)
  { 
    if(parent[cur] == -1)
    {
      parent[cur] = parentVal;
    }
    else
    {
      if(nodeInCycle == -1)
      {
        nodeInCycle = cur;
        nodeInCycleParent = parentVal;
      }
      return;
    }

    for (int next : adj[cur])
    {
      if(parent[cur] != next)
      {
          dfs(next, cur);
      }
    }
  }

  public static void handleCycle(int n)
  {
    q = new int[n];
    visited = new boolean[n];
    int node = nodeInCycle;
    qSize = 0;
    while(parent[node] != nodeInCycle)
    {
      q[qSize++] = node;
      depth[node] = 0;
      visited[node] = true;
      node = parent[node];
    }
    q[qSize++] = node;
    depth[node] = 0;
    visited[node] = true;
  }

  public static void computeDepths()
  {
    for (int p = 0; p < qSize; p++) {
      int cur = q[p];
      for (int next : adj[cur]) {
        if (!visited[next]) 
        {
          depth[next] = depth[cur] + 1;
          visited[next] = true;
          q[qSize++] = next;
        }
      }
    }
  }

  public static void printArr(int test, int[] arr, PrintWriter out)
  {
    int n = arr.length;

    out.print("Case #" + (test + 1) + ": ");

    out.print(arr[0]);
    for(int i = 1; i < n; i++)
      out.print(" " + arr[i]);
    out.println();
  }
 
  public static void main(String[] args) {
      new Thread(null, new DfsOnTrees(), "whatever", 1<<26).start();
  }
 
  public void run() {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out = new PrintWriter(System.out);
 
    try
    { 
      int t,x1,n,m,i,j,k,ans,best,cur,sum;
      String str;
      String[] token;
 
      t=Integer.parseInt(in.readLine().trim());
 
      for(x1=0;x1<t;x1++) 
      {
        n=Integer.parseInt(in.readLine().trim());

        from=new int[n];
        to=new int[n];
        depth = new int[n];
        parent = new int[n];
        visited = new boolean[n];

        for(i = 0; i < n; i++)
        {
          str = in.readLine().trim();
          token = str.split(" ");

          from[i] = Integer.parseInt(token[0]) - 1;
          to[i] = Integer.parseInt(token[1]) - 1;
        }

        adj = generateAdjList(n, from, to);

        Arrays.fill(parent, -1);

        nodeInCycle = -1;
        nodeInCycleParent = -1;
        dfs(0, 0);

        parent[nodeInCycle] = nodeInCycleParent;

        handleCycle(n);
        computeDepths();

        printArr(x1, depth, out);
      }

 
      out.flush();
      out.close();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
 
  }
}