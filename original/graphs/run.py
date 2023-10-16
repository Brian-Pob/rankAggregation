numberOfItem = 50
numberOfGroup = 4

#input

rank = []
group = []

for i in range(1,numberOfItem+1):
    rank.append(i)
    group.append(random.randint(1,numberOfGroup))