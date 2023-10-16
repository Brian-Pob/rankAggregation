import networkx as nx
import networkx as nx
from networkx.algorithms import bipartite
import random
import math

import pandas as pd
import numpy as np
import timeit
import csv


def ApproxMultiValuedIPF(rank,group):
    rankGrp = {}
    for i in range(0,len(rank)):
        rankGrp[rank[i]] = group[i]

    grpCount = {}
    for i in group:
        grpCount[i] = 0

    rankGrpPos = {}
    for i in rank:
        grpCount[rankGrp[i]] = grpCount[rankGrp[i]] + 1
        rankGrpPos[i] = grpCount[rankGrp[i]]
    numberOfItem = len(rank)
    rankRange = {}
    for item in rank:
        i = rankGrpPos[item]
        n = numberOfItem
        fp = grpCount[rankGrp[item]]
        r1 = math.floor(i*n/fp)
        r2 = math.ceil((i+1)*n/fp) - 1
        if r2 > numberOfItem:
            r2 = numberOfItem
        rankRange[item] = (r1,r2)


    B = nx.Graph()
    top_nodes = []
    bottom_nodes = []

    for i in rank:
        top_nodes.append(i)
        bottom_nodes.append(str(i))
    B.add_nodes_from(top_nodes, bipartite=0)
    B.add_nodes_from(bottom_nodes, bipartite=1)

    for i in rank:
        r1,r2 = rankRange[i]
        #print(r1,r2)
        for j in range(1,numberOfItem+1):
            if j >= r1 and j <= r2:
                print(i,j)
                B.add_edge(i, str(j), weight = abs(i-j))
            else:
                B.add_edge(i, str(j), weight=1000000000)
                print(i,j)

    my_matching = nx.algorithms.bipartite.minimum_weight_full_matching(B, top_nodes, "weight")

    print(my_matching)



n_list = [1000]
m_list = [10000000]
m = m_list[0]

with open('fig_5b_result.csv', 'w', encoding='UTF8') as f:
    writer = csv.writer(f)
    for n in n_list:
        file_lc = r'Data\single_multi\normal_data_Lc_n='+str(n)+'_m='+str(m)+'.csv'
        df_lc = pd.read_csv(file_lc,header = None)
        Lc = df_lc.to_numpy()[0]

        file_lv = r'Data\single_multi\normal_data_Lv_n=' + str(n) + '_m='+str(m)+'.csv'
        df_lv = pd.read_csv(file_lv, header=None)
        Lv = df_lv.to_numpy()[0]

        file_a = r'Data\single_multi\normal_data_a_n=' + str(n) + '_m='+str(m)+'.csv'
        df_group = pd.read_csv(file_a, header=None)
        group = df_group.to_numpy()[0]

        rank = np.argsort(Lv)
        start = timeit.default_timer()
        out = ApproxMultiValuedIPF(rank,group)
        end = timeit.default_timer()
        print("n = ",n," time = ",end - start)
        result = [n,end-start]
        writer.writerow(result)