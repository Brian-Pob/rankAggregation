/* ApproxMultiValuedIPF.java
 * 
 * Implements a P-fair gauranteed, Kendall-Tau Distance minimization
 * approxmiation algorithm.
 * 
 * Currently, the reads in a test case from a sample CSV, but is 
 * designed to modifiable for general use. 
 * 
 * The function design is to take in an array/list (sorted from best 
 * to worst) of protected attributes, and return an array containing 
 * the new locations of the candidates' ranking.
 * 
 *     Written By Mark C. Mori
 * 
 */

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileReader;

public class ApproxMultiValuedIPF {
    public static void main(String[] args) throws IOException
    {

        //Reading input for test cases

        String csvFilePath = "movielens_single_v2.csv";

        BufferedReader infile= new BufferedReader(new FileReader(csvFilePath));
        String line = infile.readLine();

        String[] result = line.split(",");

        line = infile.readLine();
        ArrayList<String> OrderedProtectedAttribute = new ArrayList<>();
        for (int i=0; line!=null; i++)
        {
            result=line.split(",");
            OrderedProtectedAttribute.add(result[1]); 
            // list is ordered from lowest to highest rated
            // will flip later
            line=infile.readLine();
        
        }
        infile.close();

        //Reversing the file input to be properly ordered
        for(int i = 0; i < OrderedProtectedAttribute.size()/2; i++)
        {
            String temp = OrderedProtectedAttribute.get(i);
            OrderedProtectedAttribute.set(i,OrderedProtectedAttribute.get(OrderedProtectedAttribute.size()-i-1));
            OrderedProtectedAttribute.set(OrderedProtectedAttribute.size()-i-1,temp);
        }


        List<Integer> final_order = P_fair_order(OrderedProtectedAttribute);

        System.out.println("\nOriginal Ranking:\tAttribute: \t\tNew Ranking:\t\tAttribute:");
        for(int i=0;i<OrderedProtectedAttribute.size();i++)
        {
            System.out.print(i+"\t\t\t "+OrderedProtectedAttribute.get(i)+"   \t\t"+final_order.get(i));
            System.out.print("\t\t\t"+OrderedProtectedAttribute.get(final_order.get(i))+"\n");
        }
    }

    public static ArrayList<Integer> P_fair_order(ArrayList<String> ObjectiveOrder) {
            
            //Counting the number of unique attributes, and their frequency
            HashMap<String,Integer> AttributeStats = new HashMap<>();
            for (String elem: ObjectiveOrder)
            {
                if (AttributeStats.containsKey(elem)==false)
                {
                    AttributeStats.put(elem,1);
                }
                else
                {
                    AttributeStats.put(elem,AttributeStats.get(elem)+1);
                }
            }

            //construct an adjacency matrix where rows represent the starting position
            //and cols represent potential final positions.
            //this will be our bipartite graph
            int[][] matrix = new int[ObjectiveOrder.size()][ObjectiveOrder.size()];

            for (int i=0; i<matrix.length;i++)
            {
                for (int j=0; j<matrix[i].length;j++)
                {
                    matrix[i][j]=-1; //if a weight is negative, it does not exist
                }
            }

            //Calculate the possible final positions of every candidate from their
            //objective order, assign weights accordingly.
            int ObjRank = 0;
            HashMap<String,Integer> AttributeRank = new HashMap<>();
            for (String elem: ObjectiveOrder)
            {
                
                if (AttributeRank.containsKey(elem)==false)
                {
                    AttributeRank.put(elem,1);
                }
                else
                {
                    AttributeRank.put(elem,AttributeRank.get(elem)+1);
                }

                int best_rank =  (int) Math.floor((AttributeRank.get(elem)-1) / ((double)AttributeStats.get(elem)/ObjectiveOrder.size()) );
                int worst_rank = (int) Math.ceil( AttributeRank.get(elem)/(((double)AttributeStats.get(elem))/ObjectiveOrder.size())-1 );
            
                //System.out.println(best_rank);
                //System.out.println(worst_rank);

                for (int PotRank=best_rank; PotRank<=worst_rank;PotRank++)
                {
                    matrix[ObjRank][PotRank]=Math.abs(ObjRank-PotRank);
                }
                
                ObjRank++;
            }

            /*  //For viewing the entire adjacency matrix  
            for (int[] elem: matrix)
            {
                for (int elem2: elem)
                {
                    System.out.print(elem2+" ");
                }
                System.out.print("\n\n");
            }*/

            //Construct our P-fair approximation

            int array[]=new int[matrix.length];
            for (int i=0; i<matrix.length; ++i)
            {
                int options_count = 0;
                for (int j=0; j<matrix.length; j++)
                {
                    if (matrix[i][j]!=-1)
                    {
                        options_count++;
                    }
                }
                array[i]=options_count;
            }
            int[] outputList=new int[matrix.length];
            for (int i = 0; i < matrix.length; ++i)
            {
                outputList[i]=-1;
            }

            for(int i =0; i<matrix.length; i++)
            {
                //find the entry with the least options
                int options_count=10000000;
                int min_row=10000000;
                for (int k=0; k<matrix.length; k++)
                {
                    if (array[k]<options_count)
                    {
                        min_row=k;
                        options_count=array[min_row];
                    }
                }

                array[min_row]=10000000;
                int best_option=10000000;
                int best_index = 10000000;

                //find best option
                for (int k=0; k<matrix.length;k++)
                {
                    if(matrix[i][k]<best_option && matrix[i][k] != -1 && outputList[i]==-1)
                    {
                        best_option=matrix[i][k];
                        best_index=k;
                    }
                }
                
                //Handling approximation issues (finding best available index)
                if (best_index==10000000)
                {
                    int b=0;
                    while (outputList[min_row+b]!=-1 && outputList[i-b]!=-1)
                    {
                        b++;
                    }
                    if (outputList[min_row+b]==-1)
                    {
                        best_index=min_row+b;
                    }
                    else
                    {
                        best_index=min_row-b;
                    }
                }
                for (int k = 0; k <matrix.length; k++)
                {
                    matrix[k][best_index]=-1;
                }     
                outputList[i]=best_index;
            }

            ArrayList<Integer> result =new ArrayList<>();

            for(int i=0;i<outputList.length;i++)
            {
                result.add(outputList[i]);
            }

            //would not print statistics in a real use case
            System.out.println("----------STATISTICS---------");
            System.out.println("Total Candidates: \t"+ObjectiveOrder.size());
            for(String elem:AttributeStats.keySet())
            {
                    System.out.println(elem+":   \t"+AttributeStats.get(elem));
            }

            return result;
    }
}
