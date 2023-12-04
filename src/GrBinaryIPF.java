//GrBinaryIPF implementation

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.*;

public class GrBinaryIPF 
{
    public static void main(String args[])
    {

    // Specify the relative file path
    String filePath = "../top25_dfs.csv";

    LinkedHashMap<String, LinkedHashMap<String, Integer>> playerMap = new LinkedHashMap<>();
    try {
        CSVReader reader = new CSVReader(new FileReader(filePath));
        ArrayList<String> header = new ArrayList<>(Arrays.asList(reader.readNext()));
        header.remove(0); // remove first empty string
        
        String[] line;
        while ((line = reader.readNext()) != null){
            LinkedHashMap<String, Integer> rank = new LinkedHashMap<>();
            for (int i = 1; i < line.length; i++) {
                rank.put(header.get(i - 1), Integer.parseInt(line[i]));
            }
            playerMap.put(line[0], rank);
        }
    } catch (IOException e) {
        e.printStackTrace();
    } catch (CsvValidationException e) {
        throw new RuntimeException(e);
    }


        // Get group information from playerMap
        LinkedHashMap<String, Integer> groupInfo = playerMap.get("Division");
        int numOfPlayers = 50;
        List<Integer> rank = new ArrayList<>();

        List<Pair<List<Integer>, List<Integer>>> result = new ArrayList<>();
        for (int rankIds = 1; rankIds <= 1; rankIds++) {
            rank = new ArrayList<>(); // need to create a new rank list for each rankIds iteration

            LinkedHashMap<String, Integer> rankInfo = playerMap.get("Rank" + (rankIds == 25 ? "" : rankIds));
                        while (rankInfo.size() > numOfPlayers) 
            {
                rankInfo.remove(rankInfo.keySet().toArray()[rankInfo.size() - 1]);
            }

    List<Pair<Integer, Integer>> rankTuples = new ArrayList<>();
    int j = 0;
    for (Map.Entry<String, Integer> entry : rankInfo.entrySet()) {
        rankTuples.add(new Pair<>(entry.getValue(), j));
        j++;
    }
    rankTuples.sort(Comparator.comparing(Pair::getFirst));

    for (Pair<Integer, Integer> pair : rankTuples) {
        rank.add(pair.getSecond());
    }

    Map<Integer, Integer> group = new HashMap<>();
    for (int i = 0; i < rank.size(); i++) {
        group.put(i, groupInfo.get(rankInfo.keySet().toArray()[i]));
    }

    //retreival of rout variable
    List<Integer> rout = computeGrBinaryIPFDelta(rank, group);

    System.out.print("rank ");
    for (int i = 0; i < rank.size(); i++) {
        System.out.print(rank.get(i) + " ");
    }
    System.out.println();
    result.add(new Pair<>(rank, rout));

    //            print result
    for (Pair<List<Integer>, List<Integer>> pair: result){
        //System.out.println(pair.getFirst() + " " + pair.getSecond());
    }
    }


    for (Pair<List<Integer>, List<Integer>> pair : result) {
        System.out.println(pair.getFirst() + " " + pair.getSecond());
    }

    List<Integer> items = new ArrayList<>();
    for (int i = 0; i < rank.size(); i++) {
    items.add(i);
    }

    List<Pair<Integer, Integer>> combinations = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
    for (int j = 0; j < items.size(); j++) {
        combinations.add(new Pair<>(items.get(i), items.get(j)));
    }
    }

    System.out.println("combinations " + combinations.size());

    int pick = 0;
    float distance = 0;
    LinkedHashMap<Integer, Integer> P = new LinkedHashMap<>();
    LinkedHashMap<Integer, Integer> Q = new LinkedHashMap<>();
    List<Integer> rankpicked = new ArrayList<Integer>();
    List<Integer> fairRankPicked = new ArrayList<Integer>();
    Pair<List<Integer>, List<Integer>> pickedPair = new Pair<>(new ArrayList<>(), new ArrayList<>());

    
    // Randomly pick a combination as long as it exists
    if (pick >= 0 && pick < result.size()) {
        pickedPair = result.get(pick);
        rankpicked = pickedPair.getFirst();
        fairRankPicked = pickedPair.getSecond();

        // Now you can use rankpicked and fairRankPicked as needed in your code...
    } else {
        System.out.println("Invalid pick index");
    }
    List<Pair<List<Integer>, List<Integer>>> result2 = new ArrayList<>();
    result2.add(pickedPair);

        for (Pair<List<Integer>, List<Integer>> pair1 : result)
        {
            for (int i = 0; i < pair1.getFirst().size(); i++) {
                P.put(pair1.getFirst().get(i), i);
                Q.put(fairRankPicked.get(i), i);
            }
        distance += kendallTau(P, Q, combinations);
    }
    
    System.out.println( distance/result.size() );
}
    

    /**
     * Description: Compute the binary IPF delta
     * @param rank
     * @param group
     * @return
     */
    public static List<Integer> computeGrBinaryIPFDelta(List<Integer> rank, Map<Integer, Integer> group) {
        List<Integer> Rho0 = new ArrayList<>();
        List<Integer> Rho1 = new ArrayList<>();
        for (Integer i : rank) {
            if (group.get(i) == 1) {
                Rho0.add(i);
            } else {
                Rho1.add(i);
            }
        }

        int j = 1;
        Map<Integer, Integer> rankDic = new HashMap<>();
        for (Integer itm : rank) {
            rankDic.put(itm, j);
            j++;
        }

        List<String> urgent = new ArrayList<>();
        List<Integer> Rout = new ArrayList<>();
        int P1count = 0;
        int P0count = 0;

        double Fp0 = (double) Rho0.size() / rank.size();
        double Fp1 = (double) Rho1.size() / rank.size();

        int i = 1;
        while (!Rho0.isEmpty() || !Rho1.isEmpty()) {
            if (P1count >= Rho1.size()) {
                Rout.addAll(Rho0.subList(P0count, Rho0.size()));
                return Rout;
            }
            if (P0count >= Rho0.size()) {
                Rout.addAll(Rho1.subList(P1count, Rho1.size()));
                return Rout;
            }

            if (P1count < Rho1.size() && P0count < Rho0.size()) {
                if (urgent.isEmpty()) {
                    if (rankDic.get(Rho1.get(P1count)) < rankDic.get(Rho0.get(P0count))) {
                        Rout.add(Rho1.get(P1count));
                        P1count++;
                    } else {
                        Rout.add(Rho0.get(P0count));
                        P0count++;
                    }
                } else {
                    if (urgent.get(0).equals("P1")) {
                        Rout.add(Rho1.get(P1count));
                        P1count++;
                    } else {
                        Rout.add(Rho0.get(P0count));
                        P0count++;
                    }
                    urgent.clear();
                }
            } else {
                break;
            }

            // Update urgent
            int delta = 0;

            if (Fp1 * (i + 1) - P1count >= delta) {
                urgent.add("P1");
            }

            if (Fp0 * (i + 1) - P0count >= delta) {
                urgent.add("P0");
            }
            i++;
        }

        return Rout;
    }

        /**
     * @param P
     * @param Q
     * @param combinations
     * @return
     */
    public static int kendallTau(
            Map<Integer, Integer> P,
            Map<Integer, Integer> Q,
            List<Pair<Integer, Integer>> combinations
    ) {
        int distance = 0;
        for (Pair<Integer, Integer> tup : combinations) {
            if (P.get(tup.getFirst()) < P.get(tup.getSecond()) && Q.get(tup.getSecond()) < Q.get(tup.getFirst())) {
                distance++;
            }
        }
        return distance;
    }
}
