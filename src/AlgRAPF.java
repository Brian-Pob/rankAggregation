import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class AlgRAPF {
    public static void main(String[] args){
        String csvFile = "top25_dfs.csv";
        LinkedHashMap<String, LinkedHashMap<String, Integer>> playerMap = new LinkedHashMap<>();
        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile));
            ArrayList<String> header = new ArrayList<>(Arrays.asList(reader.readNext()));
            header.remove(0); // remove first empty string
//            System.out.println(header);
            String[] line;
            while ((line = reader.readNext()) != null){
                LinkedHashMap<String, Integer> playerRank = new LinkedHashMap<>();
                for (int i = 1; i < line.length; i++) {
                    playerRank.put(header.get(i - 1), Integer.parseInt(line[i]));
                }
                playerMap.put(line[0], playerRank);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
//        for (Map.Entry<String, Map<String, Integer>> entry : playerMap.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }

        // Get group information from playerMap
        LinkedHashMap<String, Integer> groupInfo = playerMap.get("Division");
        int numOfPlayers = 30;
        List<Integer> rank = new ArrayList<>();

        List<Pair<List<Integer>, List<Integer>>> result = new ArrayList<>();
        for (int rankIds = 1; rankIds <= 25; rankIds++) {
            rank = new ArrayList<>(); // need to create a new rank list for each rankIds iteration
            // cant just use clear() because we need a new object
            LinkedHashMap<String, Integer> rankInfo = playerMap.get("Rank" + (rankIds == 25 ? "" : rankIds));
//            Since rankInfo should only have 30 entries, we need to remove entries until it has 30 entries
            while (rankInfo.size() > numOfPlayers) {
                rankInfo.remove(rankInfo.keySet().toArray()[rankInfo.size() - 1]);
            }


//            System.out.println("rankInfo " + rankInfo);

            List<Pair<Integer, Integer>> rankTuples = new ArrayList<>();
            int j = 0;
            for (Map.Entry<String, Integer> entry : rankInfo.entrySet()) {
                rankTuples.add(new Pair<>(entry.getValue(), j));
                j++;
            }
            rankTuples.sort(Comparator.comparing(Pair::getFirst));

//            for (Pair<Integer, Integer> pair : rankTuples) {
//                System.out.println(pair.getFirst() + " " + pair.getSecond());
//            }

//            System.out.println("------------------");

            for (Pair<Integer, Integer> pair : rankTuples) {
                rank.add(pair.getSecond());
            }

//            for (int i = 0; i < rank.size(); i++) {
//                System.out.print(rank.get(i) + " ");
//            }
//            System.out.println();

            Map<Integer, Integer> group = new HashMap<>();
            for (int i = 0; i < rank.size(); i++) {
                group.put(i, groupInfo.get(rankInfo.keySet().toArray()[i]));
            }

//            for (Map.Entry<Integer, Integer> entry : group.entrySet()) {
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }

            List<Integer> rout = computeGrBinaryIPFDelta(rank, group);
//            System.out.println("rout " + rout);
//            System.out.print("rank ");
            for (int i = 0; i < rank.size(); i++) {
//                System.out.print(rank.get(i) + " ");
            }
//            System.out.println();
            result.add(new Pair<>(rank, rout));
//            print result
            for (Pair<List<Integer>, List<Integer>> pair: result){
//                System.out.println(pair.getFirst() + " " + pair.getSecond());
            }
        }


//        for (Pair<List<Integer>, List<Integer>> pair : result) {
//            System.out.println(pair.getFirst() + " " + pair.getSecond());
//        }

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

//        for (Pair<Integer, Integer> pair : combinations) {
//            System.out.println(pair.getFirst() + " " + pair.getSecond());
//        }
        System.out.println("combinations " + combinations.size());

        float minAvg = 10000000;
        for (Pair<List<Integer>, List<Integer>> pair1 : result) {
            List<Integer> fairRankPicked = pair1.getSecond();
            float distance = 0;
            for (Pair<List<Integer>, List<Integer>> pair2 : result) {
                List<Integer> origRank = pair2.getFirst();
                LinkedHashMap<Integer, Integer> P = new LinkedHashMap<>();
                LinkedHashMap<Integer, Integer> Q = new LinkedHashMap<>();
                for (int i = 0; i < origRank.size(); i++) {
                    P.put(origRank.get(i), i);
                    Q.put(fairRankPicked.get(i), i);
                }
                distance += kendallTau(P, Q, combinations);
//                System.out.println("distance " + distance);
            }

            float avgDistance = distance / (float) result.size();
//            System.out.println("distance " + distance);
//            System.out.println("result.size() " + result.size());
//            System.out.println("avgDistance " + avgDistance);
//            System.out.println("minAvg " + minAvg);
            if (avgDistance < minAvg) {
                minAvg = avgDistance;
            }
        }

        System.out.println("minAvg " + minAvg);

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