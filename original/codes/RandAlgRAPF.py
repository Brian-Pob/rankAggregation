import os
import pandas as pd
import random
import math
import itertools
from timeit import default_timer as timer
import warnings #used to remove warnings from creator's source code file
#supresses furture warnigns from source code
warnings.simplefilter(action='ignore', category=FutureWarning)

def GrBinaryIPFDelta(rank,group):
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

    Fp0 = len(Rho0)/len(rank)
    Fp1 = len(Rho1)/len(rank)

    i = 1
    while len(Rho0) != 0 or len(Rho1) != 0:
        if P1count >= len(Rho1):
            Rout.extend(Rho0[P0count:len(Rho0)])
            return Rout
        if P0count >= len(Rho0):
            Rout.extend(Rho1[P1count:len(Rho1)])
            return Rout

        if P1count < len(Rho1) and  P0count < len(Rho0):

            if len(urgent) == 0:
                if rankDic[Rho1[P1count]] < rankDic[Rho0[P0count]]:
                    Rout.append(Rho1[P1count])
                    P1count = P1count + 1
                else:
                    Rout.append(Rho0[P0count])
                    P0count = P0count + 1
            else:
                if urgent[0] == 'P1':
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
            urgent.append('P1')

        if Fp0 * (i + 1) - P0count >= delta:
            urgent.append('P0')
        i = i + 1
        #print(i)

    return  Rout


def kendalTau(P,Q):
    qInv = {}
    pInv = {}
    for key in P:
        #print(key, P[key])
        val = P[key]
        pInv[val] = key
    for key in Q:
        #print(key, Q[key])
        val = Q[key]
        qInv[val] = key

    qTrans = {}
    #qTransInv = {}
    for key in Q:
        #print(key, Q[key])
        value = Q[key]
        newVal = pInv[value]
        qTrans[key] = newVal
        #qTransInv[newVal] = key

    dis = 0
    for key in qTrans:
        dis = dis + abs(key - qTrans[key])

    return dis



#allows to read from relative path 
script_directory = os.path.dirname(os.getcwd())
fpath = 'data/top25_dfs.pickle'
fpath = os.path.join(script_directory, fpath)

#original is a relative read
#object = pd.read_pickle(r'data/top25_dfs.pickle')
object = pd.read_pickle(fpath)

data = object[1]
num_of_player = 50
#data = data[0:num_of_player]
data = data.transpose()
players = data.keys()

itemList = data.keys()
G1 = []
G2 = []
row = data.iloc[25, :num_of_player]
for i in range(0,num_of_player):
    if(row[i] == 0):
        G1.append(players[i])
    else:
        G2.append(players[i])

p1 = len(G1)/len(itemList)
p2 = len(G2)/len(itemList)


groupInfo = data.iloc[25, :]

playeridDic = {}
j = 0
for p in players :
    playeridDic[p] = j
    j = j + 1

# group = {}
# j = 0
# for i in groupInfo:
#     group[j] = i
#     j = j + 1

inputRankList = []

start = timer()



result = []
for rankIds in range(0,1):

    rankinfo = data.iloc[rankIds, :num_of_player]
    ranktup = []
    j = 0
    for i in rankinfo:
        ranktup.append((i, j))
        j = j + 1
    ranktup.sort()
    rank = []

    for i, j in ranktup:
        rank.append(j)


    group = {}
    for i in range(0,len(rank)):
        group[i] = groupInfo[i]


    rout = GrBinaryIPFDelta(rank, group)
    result.append((rank,rout))
    #print(rout)

items = []
for i in range(0,len(rank)):
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
pick =  0 #random.randint(0,len(result) - 1)
#print(pick)
rankpicked,fairRankPicked =  result[pick]

distance  = 0
for rank, fairRank in result:

    P = {}
    Q = {}
    for i in range(0,len(rank)):
        P[rank[i]] = i
        Q[fairRankPicked[i]] = i

    distance = distance  + KendallTau(P,Q,combinations)

print(distance/len(result))