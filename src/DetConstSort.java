//GrBinaryIPF implementation

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.*;

public class DetConstSort 
{
    public static void main(String args[])
    {

        // Specify the relative file path
        String filePath = "../top25_dfs.csv";

        LinkedHashMap<String, LinkedHashMap<String, Integer>> data = new LinkedHashMap<>();
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath));
            ArrayList<String> header = new ArrayList<>(Arrays.asList(reader.readNext()));
            header.remove(0); // remove first empty string

            String[] line;
            while ((line = reader.readNext()) != null){
            LinkedHashMap<String, Integer> rank = new LinkedHashMap<>();
            for (int i = 1; i < line.length; i++) 
            {
                rank.put(header.get(i - 1), Integer.parseInt(line[i]));
            }
            data.put(line[0], rank);
        }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        //set max number of players to 30
        int numOfPlayers = 30;
        int count = 0;
        LinkedHashMap<String, LinkedHashMap<String, Integer>> playerMap = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, Integer>> entry : data.entrySet()) {
            String outerKey = entry.getKey();
            LinkedHashMap<String, Integer> innerMap = entry.getValue();
            // Iterate over the inner LinkedHashMap
            for (Map.Entry<String, Integer> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                Integer innerValue = innerEntry.getValue();
                playerMap.put(outerKey,sliceMap(innerMap, numOfPlayers));
            }
        }

        
        LinkedHashMap<String, Integer> row = new LinkedHashMap<>(playerMap.get(playerMap.keySet().toArray()[25]));

        List<String> players = new ArrayList<>(row.keySet()); //initialize players
         List<String> itemList = new ArrayList<>(row.keySet()); //initialize itemlist

        List<String> G1 = new ArrayList<>();
        List<String> G2 = new ArrayList<>();

        for (int i = 0; i <numOfPlayers ; i++) {
            if (row.get(players.get(i)) == 0) {
                G1.add(players.get(i));
            } else
            {
                G2.add(players.get(i));
            }
        }

        double p1 = (double) G1.size() / players.size();
        double p2 = (double) G2.size() / players.size();

        System.out.println("Value of p1 is " + p1 + "\nValue of p2 is " + p2);


        LinkedHashMap<String, Integer> rankRow = new LinkedHashMap<>(playerMap.get(playerMap.keySet().toArray()[1]));

        List<Map.Entry<String, Integer>> rankTup = new ArrayList<>();
        int j = 0;

        // Populate rankTup
        for (Map.Entry<String, Integer> entry : rankRow.entrySet()) {
            rankTup.add(Map.entry(entry.getKey(), j));
            j++;
        }

        // Sort rankTup based on the keys (i)
        Collections.sort(rankTup, Map.Entry.comparingByKey());

        List<Integer> rank = new ArrayList<>();

        // Populate rank based on the values (j)
        for (Map.Entry<String, Integer> entry : rankTup) {
            rank.add(entry.getValue());
        }

        List<Map.Entry<Integer, Integer>> tup = new ArrayList<>();

        // Populate tup
        for (int i = 0; i < rank.size(); i++) {
            tup.add(Map.entry(rank.get(i), i));
        }

        // Sort tup based on the keys (rank[i])
        Collections.sort(tup, Map.Entry.comparingByKey());

        System.out.println(tup);

        rank.clear();

        // Populate rank based on the values (j)
        for (Map.Entry<Integer, Integer> entry : tup) {
            rank.add(entry.getValue());
        }

        // Assuming 'row' is an existing LinkedHashMap<String, Integer> in your code
        LinkedHashMap<String, Integer> group = new LinkedHashMap<>(row);

        List<String> A = List.of("0", "1");


        Map<String, Double> P = Map.of("0", p1, "1", p2);
        Map<String, List<Map.Entry<Integer, Integer>>> S = new LinkedHashMap<>();

        for (String a : A) {
            List<Map.Entry<Integer, Integer>> scoreList = new ArrayList<>();
            S.put(a, scoreList);
        }

        int score = rank.size();
        for (int i : rank) {
            if (group.get(Integer.toString(i)) == 0) {
                S.get(A.get(0)).add(Map.entry(score, i));
            } else {
                S.get(A.get(1)).add(Map.entry(score, i));
            }
            score--;
        }

    }

    private static LinkedHashMap<String, Integer> sliceMap(LinkedHashMap<String, Integer> inputMap, int size) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        int count = 0;

        for (Map.Entry<String, Integer> entry : inputMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
            count++;

            if (count >= size) {
                break;
            }
        }

        return result;
    }

}
