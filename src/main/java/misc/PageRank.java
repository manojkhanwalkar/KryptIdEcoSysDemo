package misc;

import java.util.Arrays;
import java.util.List;

public class PageRank {

    AdjacencyMatrix matrix ;

    double[] weights;  // 1/(number of outgoing nodes)
    double[] prevRank ;
    double[]currRank;

    public PageRank(AdjacencyMatrix matrix)
    {
        this.matrix = matrix;
        populateWeights();
        populateInitialRank();
    }


    private void populateWeights()
    {
        weights = new double[matrix.nodes];
        for (int i=0;i<matrix.nodes;i++) {
            int size = matrix.getOutgoingEdges(i).size();
            if (size == 0) {
                weights[i] = 1.0;
            } else {
                weights[i] = 1.0 / size;
            }
        }

    }

    private void populateInitialRank()
    {
        prevRank = new double[matrix.nodes];
        currRank = new double[matrix.nodes];
        double rank = 1.0/matrix.nodes;

        Arrays.fill(currRank,rank);


    }

    public void calculateRank(int numRounds)
    {
        printRank();
        for (int i=0;i<numRounds;i++)
        {
            calculate();
            printRank();
        }
    }

    private void printRank()
    {
        for (double d : currRank)
            System.out.print(d + " ");
        System.out.println();
    }

    public double getRank(int node)
    {
        return currRank[node];
    }

    private void initRound()
    {
        System.arraycopy(currRank,0,prevRank,0,currRank.length);
    }


    private void calculate()
    {
        initRound();
        for (int i=0;i<matrix.nodes;i++)
        {
            currRank[i]=0.0;
            List<Integer> inEdges = matrix.getIncomingEdges(i);
            for (int j=0;j<inEdges.size();j++)
            {
                int index = inEdges.get(j);
                double weight = weights[index];
                double rank = prevRank[index];
                currRank[i] = currRank[i]+ rank*weight;
            }
        }
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

        PageRank pageRank = new PageRank(matrix);
        pageRank.calculateRank(5);


    }



}
