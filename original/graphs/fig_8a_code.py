#input: original rank, group information
#output: an alternative P-Fair rank  where the Kendall Tau distance is minimized.

import pandas as pd
import numpy as np
import timeit
import csv
import random as rnd

def GrBinaryIPF(rank,group):
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
        #print(Rout)
        if P1count >= len(Rho1):
            Rout.extend(Rho0[P0count:len(Rho0)])
            return Rout
        if P1count >= len(Rho0):
            Rout.extend(Rho1[P1count:len(Rho1)])
            return Rout

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
        # update urgent
        if Fp1 * (i + 1) - P1count >= 1:
            urgent.append('P1')

        if Fp0 * (i + 1) - P0count >= 1:
            urgent.append('P0')
        i = i + 1
        #print(i)
    return  Rout




n_list = ['1k','5k','10k','50k','100k','1m']
nlistNum = [1000,5000,10000,50000,100000,1000000]
with open('fig_8a_result1.csv', 'w', encoding='UTF8',newline='') as f:
    writer = csv.writer(f)
    counter = 0
    for n in n_list:
        filename1 = r'data/Mallows/rn_100_n_' + n + '.csv'
        filename2 = r'data/Mallows/' + n + '_attribute.csv'
        df1 = pd.read_csv(filename1)
        df2 = pd.read_csv(filename2)
        pick = rnd.randint(0,100)
        rank = df1.loc[pick, :].values.tolist()
        rank = [int(i) for i in rank]
        group = df2['protected attribute'].to_list()
        start = timeit.default_timer()
        out = GrBinaryIPF(rank,group)
        end = timeit.default_timer()
        print("n = ",nlistNum[counter]," time = ",end - start)
        result = (nlistNum[counter],end-start)
        writer.writerow(result)
        counter = counter + 1