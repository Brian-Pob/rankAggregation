import pandas as pd
import itertools
import warnings
from pprint import pprint
from typing import Dict, List, Tuple

# suppress future warnings
warnings.simplefilter(action="ignore", category=FutureWarning)


def GrBinaryIPFDelta(rank, group):
    # print(type(rank[0]))
    # pprint(group)
    Rho0 = []
    Rho1 = []
    for i in rank:
        if group[i] == 1:
            Rho0.append(i)
        else:
            Rho1.append(i)

    j = 1
    rankDic = {}
    for itm in rank:
        rankDic[itm] = j
        j = j + 1

    urgent = []
    Rout = []
    P1count = 0
    P0count = 0

    Fp0 = len(Rho0) / len(rank)
    Fp1 = len(Rho1) / len(rank)

    i = 1
    while len(Rho0) != 0 or len(Rho1) != 0:
        if P1count >= len(Rho1):
            Rout.extend(Rho0[P0count : len(Rho0)])
            # print("=============================")
            # print(type(Rout[0]))
            return Rout
        if P0count >= len(Rho0):
            Rout.extend(Rho1[P1count : len(Rho1)])
            # print("=============================")
            # print(type(Rout[0]))
            return Rout

        if P1count < len(Rho1) and P0count < len(Rho0):
            if len(urgent) == 0:
                if rankDic[Rho1[P1count]] < rankDic[Rho0[P0count]]:
                    Rout.append(Rho1[P1count])
                    P1count = P1count + 1
                else:
                    Rout.append(Rho0[P0count])
                    P0count = P0count + 1
            else:
                if urgent[0] == "P1":
                    Rout.append(Rho1[P1count])
                    P1count = P1count + 1
                else:
                    Rout.append(Rho0[P0count])
                    P0count = P0count + 1
                urgent = []
        else:
            break
        # update urgent
        delta = 0

        if Fp1 * (i + 1) - P1count >= delta:
            urgent.append("P1")

        if Fp0 * (i + 1) - P0count >= delta:
            urgent.append("P0")
        i = i + 1
        # print(i)

    return Rout


def KendallTau(
    P: Dict[int, int], Q: Dict[int, int], combinations: List[Tuple[int, int]]
) -> int:
    distance: int = 0
    for tup in combinations:
        if P[tup[0]] < P[tup[1]] and Q[tup[1]] < Q[tup[0]]:
            distance += 1
    return distance


data = pd.read_pickle(r"./top25_dfs.pickle")[1]

num_of_player = 30

data = data.transpose()
# Result: rows are ranks and columns are players

# Layout of the dataframe after transpose:
#          player1  player2 player3 ...
# Rank1    10       9       8
# Rank2    9        10      8
# ...
# Rank24   1        2       3
# Rank     1        1       1
# Division 1        0       0

# Note for conversion to Java:
# We can convert the pickle file to a csv in the desired format so we don't
# need to transpose the data in Java. We can read the CSV and store it as a
# Map<String, Map<String, Integer>> where the outer map is the rank and the
# inner map is the player name and their rank.
data.to_csv(r"./top25_dfs.csv")

# Example:
# playerMap = new HashMap<String, Map<String, Integer>>();
# playerMap.put("Rank1", new HashMap<String, Integer>());
# playerMap.get("Rank1").put("Adam Thielen", 10);
# playerMap.put("Rank", new HashMap<String, Integer>());
# playerMap.get("Rank").put("Adam Thielen", 9);
# playerMap.put("Division", new HashMap<String, Integer>());
# playerMap.get("Division").put("Adam Thielen", 1);


# get the 26th row, get all columns
# In the dataframe, the 26th row is Division.
groupInfo = data.iloc[25, :]
# Result: groupInfo contains the division of each player

# To do this in Java, we can use our playerMap and get the "Division" key and
# get all the inner maps (player names and their ranks).


result = []
for rankIds in range(0, 25):
    rankinfo = data.iloc[rankIds, :num_of_player]
    # To do this iloc call, we can get the rank from the playerMap and get the
    # inner map for that rank. Then we can iterate through the inner map and
    # get the player names and their ranks. We need to consider the max number
    # of columns that we want to get.

    # rankInfo contains the rank of each player in the current rank
    # print(rankinfo)
    ranktup = []
    j = 0
    for i in rankinfo:
        ranktup.append((i, j))
        j = j + 1
    ranktup.sort()
    # ranktup i is the rank of the player, j is the player id

    # print(ranktup)
    # print("-----")

    rank = []

    for i, j in ranktup:
        rank.append(j)

    # print(rank)

    group = {}
    for i in range(0, len(rank)):
        group[i] = groupInfo[i]

    # print(len(group))
    # print(group)
    # print(len(groupInfo))
    # print(groupInfo)

    rout = GrBinaryIPFDelta(rank, group)
    # print("rout", rout)
    # print("rank", rank)
    result.append((rank, rout))

print(result)

items = []
for i in range(0, len(rank)):
    items.append(i)
combinations = [p for p in itertools.product(items, repeat=2)]
print("combinations", len(combinations))

# rand rapf result

minAvg = 10000000

for rankPicked, fairRankPicked in result:
    distance = 0
    for origrank, fairRank in result:
        P = {}
        Q = {}
        for i in range(0, len(rank)):
            P[origrank[i]] = i
            Q[fairRankPicked[i]] = i

        distance = distance + KendallTau(P, Q, combinations)
        # print("distance", distance)

    avgDistance = distance / len(result)
    # print("len(result)", len(result))
    # print("distance", distance)
    # print("avgDistance", avgDistance)
    # print("minAvg", minAvg)
    # print(avgDistance)
    if avgDistance < minAvg:
        minAvg = avgDistance

print("minAvg", minAvg)
