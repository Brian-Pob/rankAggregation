def DetConstSort(S,A,P,kmax):
    counts = {}
    minCounts = {}
    for ai in A:
        counts[ai] = 0
        minCounts[ai] = 0
    
    rankedAttList = {}
    rankedScoreList = {}
    maxIndices = {}
    lastEmpty = 0
    k = 0
    
    while lastEmpty <= kmax:
        k = k + 1
        tempMinCounts = {}
        for ai in A:
            tempMinCounts[ai] = math.floor(k * P[ai])
        changedMins = []
        for ai in A:
            if minCounts[ai ] < tempMinCounts[ai]:
                changedMins.append((S[ai][counts[ai]],ai))
        if len(changedMins) != 0 :
            changedMins.sort(reverse=True)
            #ordChangedMins = changedMins
        for sai,ai in  changedMins:
            rankedAttList[lastEmpty] = ai
            
            rankedScoreList[lastEmpty] = S[ai][counts[ai]]
            maxIndices[lastEmpty] = k
            start = lastEmpty
            while start > 0 and maxIndices[start - 1] >= start and rankedScoreList[start-1][0] < rankedScoreList[start][0]:
                swap(maxIndices,start - 1,start)
                swap(rankedAttList,start - 1, start)
                swap(rankedScoreList,start - 1, start)
                start = start - 1
            counts[ai] = counts[ai] + 1 
            lastEmpty = lastEmpty + 1
        minCounts = tempMinCounts
    return (rankedAttList, rankedScoreList)





import pandas as pd
import os
import pandas as pd
from GrBinaryIPF import GrBinaryIPF
import math
import matplotlib.pyplot as plt

fpath =  "data/top25_dfs.pickle"



object = pd.read_pickle(fpath)

data = object[1]
num_of_player = 30
data = data[0:num_of_player]
data = data.transpose()
players = data.keys()
data



G1 = []
G2 = []
row = data.iloc[25, :]
for i in range(0,num_of_player):
    if(row[i] == 0):
        G1.append(players[i])
    else:
        G2.append(players[i])

p1 = len(G1)/len(itemList)
p2 = len(G2)/len(itemList)


rank = data.iloc[1, :]

ranktup = []
j = 0
for i in rank:
    ranktup.append((i,j))
    j = j + 1
ranktup.sort()
rank = []

for i,j in ranktup:
    rank.append(j) 


tup = []
for i in range(0,len(rank)):
    tup.append((rank[i],i))

tup.sort()
rank = []
for i,j in tup:
    rank.append(j)
group = row


A = ['0','1']

P = {'0':p1,'1':p2}
S={}
for a in A:
   
    scoreList = []
    
    S[a] = scoreList

S = {A[0]:[],A[1]:[]}
score = len(rank)
for i in rank:
    if group[i] == 0:
        S[A[0]].append((score,i))
    else:
        S[A[1]].append((score,i)) 
    score = score - 1


def swap(dict,i,j):
    item = dict[i]
    dict[i] = dict[j]
    dict[j] = item 


kmax = len(rank) - 1
rankedAttList,rankedScoreList = DetConstSort(S,A,P,kmax)


detOut = []
for key in rankedScoreList.keys():
    detOut.append(rankedScoreList[key][1])