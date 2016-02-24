import json
from collections import defaultdict
from random import randint
import numpy as np
from scipy import spatial

from sklearn import metrics
from sklearn.cluster import KMeans
from sklearn.datasets import load_digits
from sklearn.decomposition import PCA
from sklearn.preprocessing import scale

json_data = open('articles.json')

i = 0
data = []
for line in json_data:
	jsonline = json.loads(line)
	data.append(jsonline)
	i+=1

vectors = [x['vector'] for x in data]
articles = [(x['article'], x['pageRank']) for x in data]

vectors = vectors[:1000]
articles = articles[:1000]

kmeans = KMeans(init='k-means++', n_clusters=100, n_init=10)
kmeans.fit(vectors)
model = kmeans.predict(vectors)

clusters = {}
n = 0
for item in model:
    if item in clusters:
        clusters[item].append(articles[n])
    else:
        clusters[item] = [articles[n]]
    n +=1

def clusterName(clusters, item):
	top = clusters[item][0]
	for i in clusters[item]:
		if i[1] > top[1]:
			top = i
	return top

for item in clusters:
    print "Cluster ", item, " - ", clusterName(clusters, item)[0]
    for i in clusters[item]:
        print "\t",i[0]



# def randomClusters(items, k):
#     # clusterNum -> item
#     clusters = defaultdict(list)
#     for item in items:
#         cluster = str(randint(1,k))
#         clusters[cluster].append(item)
#     return clusters

# import numpy as np
# from scipy import spatial

# def cosineSimilarity(a, b):
#     return 1 - spatial.distance.cosine(a, b)

# def computeCentroid(items,cluster):
#     centroidList = defaultdict(list)
#     for item in cluster:
#         elements = item[1] # the vector part of the tuple
#         for element in elements:
#             centroidList[element].append(item[1])
#     centroid = defaultdict(int)
#     for element in centroidList:
#         # zeroedList = centroidList[element] + [0]*(len(centroidList) - len(centroidList[element]))
#         # centroid[element] = np.mean(zeroedList)
#         centroid[element] = np.mean(centroidList[element])
#     return centroid

# clusters = randomClusters(testVectors, 3)
# one = clusters.get('1')
# print one
# print
# print computeCentroid(testVectors, one)

# def kmeans(items, k):
#     clusters = randomClusters(items, k)
#     for i in range(10):
#         # cluster -> (tag -> mean freq)
#         print "Starting iteration", i, "..."
#         clusterCentroids = defaultdict(lambda : defaultdict(int))
#         for cluster in clusters:
#             clusterCentroids[cluster] = computeCentroid(items, clusters[cluster])
#         newClusters = defaultdict(list)
#         iterationSimilarity = 0
#         for item in items:
#             mostSimilarCluster = (0, "0")
#             for centroid in clusterCentroids:
#                 itemVector = item[1]
#                 centroidVector = []
#                 for element in clusterCentroids[centroid]:
#                     centroidVector.append(clusterCentroids[centroid].get(element))
#                 similarity = cosineSimilarity(itemVector, centroidVector)
#                 if similarity > mostSimilarCluster[0]:
#                     mostSimilarCluster = (similarity, centroid)
#             newClusters[mostSimilarCluster[1]].append(item)
#             iterationSimilarity += mostSimilarCluster[0]
#         print "The iteration similarity is", iterationSimilarity
#         clusters = newClusters
#     return clusters

# print kmeans(testVectors, 3)
