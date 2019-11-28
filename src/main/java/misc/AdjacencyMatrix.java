package misc;

import java.util.ArrayList;
import java.util.List;

public class AdjacencyMatrix {

    int nodes;

    int[][] array;

    public AdjacencyMatrix(int nodes) {
         array = new int[nodes][nodes];
         this.nodes = nodes;
    }


    public void addNode(int node , int... outgoingEdges)
    {
        int[][] array1 = new int[nodes+1][nodes+1];
        for (int i=0;i<nodes;i++)
        {
            for (int j=0;j<nodes;j++)
            {
                array1[i][j] = array[i][j];

            }

        }
        array = array1;
        for (int i=0;i<outgoingEdges.length;i++)
        {
            addEdge(node,outgoingEdges[i]);
        }

        nodes++;

    }

    public void addEdge(int src, int tgt) {
        array[src][tgt] = 1;
    }

    public List<Integer> getIncomingEdges(int node)
    {
        List<Integer> edges = new ArrayList<>();
        for (int i=0;i<nodes;i++)
        {
            if (array[i][node]==1)
            {
                edges.add(i);
            }
        }
        return edges;
    }

    public List<Integer> getOutgoingEdges(int node)
    {
        List<Integer> edges = new ArrayList<>();
        for (int i=0;i<nodes;i++)
        {
            if (array[node][i]==1)
            {
                edges.add(i);
            }
        }
        return edges;
    }


    public static void main(String[] args) {

        AdjacencyMatrix matrix = new AdjacencyMatrix(5);
        matrix.addEdge(0,1);
        matrix.addEdge(1,4);
        matrix.addEdge(2,0);
        matrix.addEdge(2,1);
        matrix.addEdge(2,3);
        matrix.addEdge(2,4);
        matrix.addEdge(3,2);
        matrix.addEdge(3,4);
        matrix.addEdge(4,3);

        System.out.println(matrix.getOutgoingEdges(2));
        System.out.println(matrix.getIncomingEdges(4));

        matrix.addNode(5,2,4);

        System.out.println(matrix.getOutgoingEdges(2));
        System.out.println(matrix.getIncomingEdges(4));



    }

}
