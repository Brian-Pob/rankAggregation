import os
import pandas as pd
import random
import math
import networkx as nx
from networkx.algorithms import bipartite
import random
import math

#allows to read from relative path 
script_directory = os.path.dirname(os.getcwd())
fpath = 'data/unique_200.csv'
fpath = os.path.join(script_directory, fpath)
df = pd.read_csv(fpath) #df = pd.read_csv('data/unique_200.csv')

#allGenre = []
group = []
genre = df['genre']
result = []
numberOfItem = 200#len(df.iloc[:, 1])
delta = 0

allGenre = []
allgroup = []
for g in genre:
    if g not in allGenre:
        allGenre.append(g)
    allgroup.append(allGenre.index(g))

for colnum in range(1,2):


    movieIds = df.iloc[:, colnum]
    movieDic = {}
    rank = []

    movieIdarray = []
    for i in movieIds:
        if i <= numberOfItem:
            movieIdarray.append(i)

    for i in range(1,len(movieIdarray)+1):
        movieId = movieIdarray[i-1]
        groupId = allgroup[movieId-1]
        movieDic[i] = movieId
        rank.append(i)
        group.append(groupId)



    rank = rank[0:numberOfItem]
    group = group[0:numberOfItem]




    rankGrp = {}
    for i in range(0, len(rank)):
        rankGrp[rank[i]] = group[i]

    grpCount = {}
    for i in group:
        grpCount[i] = 0

    rankGrpPos = {}
    for i in rank:
        grpCount[rankGrp[i]] = grpCount[rankGrp[i]] + 1
        rankGrpPos[i] = grpCount[rankGrp[i]]

    rankRange = {}
    for item in rank:
        i = rankGrpPos[item]
        n = numberOfItem
        fp = grpCount[rankGrp[item]]
        r1 = math.floor((i-1 - delta) * n / fp)+1
        r2 = math.ceil((i + delta) * n / fp)
        if r2 > numberOfItem:
            r2 = numberOfItem
        rankRange[item] = (r1, r2)

    B = nx.Graph()
    top_nodes = []
    bottom_nodes = []

    for i in rank:
        top_nodes.append(i)
        bottom_nodes.append(str(i))
    B.add_nodes_from(top_nodes, bipartite=0)
    B.add_nodes_from(bottom_nodes, bipartite=1)

    for i in rank:
        r1, r2 = rankRange[i]
        # print(r1,r2)
        for j in range(1, numberOfItem+1):
            if j >= r1 and j <= r2:
                #print(i,j)
                B.add_edge(i, str(j), weight=abs(i - j))
            # else:
            #     B.add_edge(i, str(j), weight=100000000000)
            #     # print(i,j)

    my_matching = nx.algorithms.bipartite.minimum_weight_full_matching(B, top_nodes, "weight")

    #print(my_matching)

    rank1 = []
    rank2 = []
    for i in range(0,numberOfItem):
        rank2.append(0)

    for i in range(1,numberOfItem+1):
        #print(my_matching[i])
        rank1.append(i)
        rank2[int(my_matching[i]) -1 ] = i


    inputRank = []
    fairRank = []

    for i in range(0,numberOfItem):
        rn1 = rank1[i]
        rn2 = rank2[i]
        inputRank.append(movieDic[rn1])
        fairRank.append(movieDic[rn2])



    result.append((inputRank,fairRank))

#print(result)


import itertools

items = []
for i in result[0][0]:
    items.append(i)
combinations = [p for p in itertools.product(items, repeat=2)]
print(len(combinations))

def KendallTau(P,Q,combinations):
    distance = 0
    for tup in combinations:
        if int(P[tup[0]]) < int(P[tup[1]]) and  int(Q[tup[1]]) < int(Q[tup[0]]):
            distance = distance + 1
    return distance


#rand rapf result


minAvg = 10000000
fairRankOutput = []
for rankPicked,fairRankPicked in result:
    distance = 0
    for rank, fairRank in result:
        P = {}
        Q = {}
        for i in range(0,len(rank)):
            P[rank[i]] = i
            Q[fairRankPicked[i]] = i

        distance = distance + KendallTau(P,Q,combinations)
    avgDistance = distance/len(result)
    print(len(result))
    if avgDistance < minAvg:
        minAvg = avgDistance
        fairRankOutput = Q

print(minAvg)
#print(Q)


