
#Fair ILP


object = pd.read_pickle(r"data/top25_dfs.pickle")
data = object[1]
num_of_player = 35
data = data[0:num_of_player]
data = data.transpose()
players = data.keys()
nrow = len(data)
ncol = int(data.size/len(data))
l = ncol
rankDic = {}
rankList = []
for i in range(0,25):
    row = data.iloc[i, :]
    rank = {}
    for j in range(0,l):
        rankDic[(i,players[j])] = row[j]
        rank[players[j]] = row[j]
    rankList.append(rank)

itemList = data.keys()
itemList = itemList[0:l]
itemList

G1 = []
G2 = []
row = data.iloc[25, :]
group = row
for i in range(0,l):
    if(row[i] == 0):
        G1.append(players[i])
    else:
        G2.append(players[i])
        

p1 = len(G1)/len(itemList)
p2 = len(G2)/len(itemList)

d = 0.9999



G = []
G.append(G1)
G.append(G2)



p = []
p.append(p1)
p.append(p2)



combinations = [p for p in itertools.product(itemList, repeat=2)]
len(combinations)



scores = []
for tup in combinations:
    x,y = tup
    v = 0
    for i in range(0,25):
        if rankDic[(i,x)] < rankDic[(i,y)]:
            v = v +  1
    scores.append(v)




res = dict(zip(combinations, scores)) 

combinations, scores = gp.multidict(res)


# Declare and initialize model
m = gp.Model('RAP')

# Create decision variables for the RAP model
x = m.addVars(combinations,vtype=GRB.BINARY,name='x')


# Create  constraints
con = m.addConstrs((x[i,i] == 0  for i in itemList ), name='c0')


# Create  constraints
con = m.addConstrs((x[i,j] + x[j,i]  == 1   for j in itemList for i in itemList if i != j), name='c1')


# Create  constraints
con = m.addConstrs((x[i,j] + x[j,k] + x[k,i]  >= 1 for j in itemList for i in itemList for k in itemList if (i!= j and j!= k and k != i)), name='c2')
#con = m.addConstrs( (gp.quicksum(p[u]*x[i, j] for j in itemList) - gp.quicksum(x[i, k] for k in G[u]) <= d  for i in itemList  for u in  range(0,len(G)) ) ,name='cfair2')



# Objective: maximize total matching score of all assignments
m.setObjective(x.prod(scores), GRB.MINIMIZE)


# Save model for inspection
m.write('RAP.lp')


# Run optimization engine
m.optimize()


row = data.iloc[25, :]
len(m.getVars())


# Display optimal values of decision variables
i = 0
s = 0
scoredict = {}
for v in m.getVars():
   
    i = i + 1
    s = s + v.x
    #print(v.varName, v.x)
    if i % len(itemList) == 0:
        #print(i / len(itemList))
        g = 'g1'
        if (row[int(i / len(itemList)) - 1] == 1):
            g = 'g2'
        scoredict[itemList[int(i / len(itemList)) - 1]] = (s,g)
        s = 0 

#Display optimal total matching score
print('Total matching score: ', m.objVal)



RoutFairIlp = []

for w in sorted(scoredict, key=scoredict.get, reverse=False):
    print(w, scoredict[w])
    RoutFairIlp.append(w)
    

resRank = {}
for p in scoredict.keys():
    resRank[p] = scoredict[p][0]

print("output rank is = ",resRank)

def KendallTau(P,Q):
    combinations = [p for p in itertools.product(players[0:l], repeat=2)]
    distance = 0
    for tup in combinations:
        if P[tup[0]] < P[tup[1]] and  Q[tup[1]] < Q[tup[0]]:
            distance = distance + 1
    return distance




sumKTau = 0 
for i in range(0,len(rankList)):
    rankQ = rankList[i]
    ktau = KendallTau(resRank,rankQ)
    #print(ktau)
    sumKTau = sumKTau + ktau
avgKTau = sumKTau/len(rankList)
print(avgKTau)

