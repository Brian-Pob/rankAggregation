import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class AlgRAPF {
    public static void main(String[] args){
//        CSV layout
//          ,player1,  player2,  player3, ...
//  Rank1,    10,       9,       8,
//  Rank2,    9,        10,      8,
//  ...
//  Rank24,   1,        2,       3,
//  Rank,     1,        1,       1,
//  Division, 1,        0,       0,
//        Read csv file. Then store as Map<String, Map<String, Integer>>
        String csvFile = "top25_dfs.csv";
        Map<String, Map<String, Integer>> playerMap = new HashMap<>();
//      {"Rank": {"player name": rank}}
        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile));
            ArrayList<String> header = new ArrayList<>(Arrays.asList(reader.readNext()));
            header.remove(0); // remove first empty string
            System.out.println(header);
            String[] line;
            while ((line = reader.readNext()) != null){
                Map<String, Integer> playerRank = new HashMap<>();
                for (int i = 1; i < line.length; i++) {
                    playerRank.put(header.get(i - 1), Integer.parseInt(line[i]));
                }
                playerMap.put(line[0], playerRank);
            }
            for (Map.Entry<String, Map<String, Integer>> entry : playerMap.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }


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
