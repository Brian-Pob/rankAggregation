#input: original rank, group information
#output: an alternative P-Fair rank  where the Kendall Tau distance is minimized.

import pandas as pd
import numpy as np
import timeit
import csv


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


n_list = [1000,250000,500000,750000,1000000]
m_list = [10000000]
m = m_list[0]

with open('fig_5a_result.csv', 'w', encoding='UTF8') as f:
    writer = csv.writer(f)
    for n in n_list:
        file_lc = r'Data\single_binary\normal_data_Lc_n='+str(n)+'_m='+str(m)+'.csv'
        df_lc = pd.read_csv(file_lc,header = None)
        Lc = df_lc.to_numpy()[0]

        file_lv = r'Data\single_binary\normal_data_Lv_n=' + str(n) + '_m='+str(m)+'.csv'
        df_lv = pd.read_csv(file_lv, header=None)
        Lv = df_lv.to_numpy()[0]

        file_a = r'Data\single_binary\normal_data_a_n=' + str(n) + '_m='+str(m)+'.csv'
        df_group = pd.read_csv(file_a, header=None)
        group = df_group.to_numpy()[0]

        rank = np.argsort(Lv)
        start = timeit.default_timer()
        out = GrBinaryIPF(rank,group)
        end = timeit.default_timer()
        print("n = ",n," time = ",end - start)
        result = [n,end-start]
        writer.writerow(result)