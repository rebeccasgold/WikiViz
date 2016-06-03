htmlTop = '''
<!DOCTYPE HTML>

<html>
    <head>
        <title>WikiBrainMap</title>
        <script type="text/javascript" src="d3/d3.v3.min.js"></script>
    </head>

    <body>
    <script type="text/javascript">
        var w = 2000;
        var h = 2000;

        var dataset = [
'''

htmlBottom = '''
];
    function pastelColors(seed){
        var hue = Math.floor(seed * 36);
        var pastel = 'hsl(' + hue + ', 100%, 87.5%)';
        return pastel
    }

    var seeds = [];
    for (var a=[],i=0;i<100;++i) seeds[i]=i;
	function shuffle(array) {
	  var tmp, current, top = array.length;
	  if(top) while(--top) {
	    current = Math.floor(Math.random() * (top + 1));
	    tmp = array[current];
	    array[current] = array[top];
	    array[top] = tmp;
	  }
	  return array;
	}

	seeds = shuffle(seeds);

    var clusterColors = []
    for (var i =0; i<100; i++){
    	var color = pastelColors()
    	clusterColors.push(pastelColors(seeds[i]))
    }
        //Create SVG element
        // var svg = d3.select("body")
        //         .append("svg")
        //         .attr("width", w)
        //         .attr("height", h);

        var svg = d3.select("body")
		  .append("svg")
		  .attr("width", w)
		  .attr("height", h)
		  .call(d3.behavior.zoom().on("zoom", function () {
		    svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")")
		  }))
		  .append("g")

        svg.selectAll("path")
            .data(d3.geom.voronoi(dataset))
            .enter().append("svg:path")
            .attr("d", function(d) { return "M" + d.join("L") + "Z"; })
            .attr("stroke", "gray")
            .attr("stroke-width", .5)
            .attr("fill", function(d){
            	return clusterColors[d.point[3]];
            });

        svg.selectAll("circle")
                .data(dataset)
                .enter()
                .append("circle")
                .attr("cx", function(d) {
                    return d[0];
                })
                .attr("cy", function(d) {
                    return d[1];
                })
                .attr("r", function(d) {
                    return 1;
                })
                .attr("fill", "gray");

        svg.selectAll("text")
            .data(dataset)
            .enter()
            .append("text")
            .text(function(d) {
                return d[2];
            })
            .attr("x", function(d) {
                return d[0] + 2;
            })
            .attr("y", function(d) {
                return d[1] + 2;
            })
            .attr("font-size", "5px")
            .attr("fill", "black")
            .attr("font-family", "sans-serif")
   //          .attr("visibility", "hidden")
   //          .on("mouseover", function(d)
			//  {
			//      d3.select(this).style("visibility","visible")
			//  })
			// .on("mouseout", function(d)
			//  {
			//      d3.select(this).style("visibility","hidden")
			//  })
			;   
    </script>
    </body>
</html>

'''

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

from mpl_toolkits.mplot3d import Axes3D

from sklearn import manifold, decomposition

json_data = open('articles.json')

i = 0
data = []
categories = []
for line in json_data:
	jsonline = json.loads(line)
	if "Category:" in jsonline['article']:	
		categories.append(jsonline)
	else:
		data.append(jsonline)
	i+=1

vectors = [x['vector'] for x in data]
articles = [x['article'] for x in data]

print len(vectors)

vectors = vectors[:1000]
articles = articles[:1000]

kmeans = KMeans(init='k-means++', n_clusters=100, n_init=5, verbose=1, max_iter=30)
print "Initialized KMeans"
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

from scipy import spatial

def cosineSimilarity(a, b):
    return 1 - spatial.distance.cosine(a, b)

def clusterName(clusters, item, centroids):
	centroid = centroids[item]
	# title, vector, score
	closest = (categories[0]['article'], 0)
	for cat in categories:
		c = cosineSimilarity(centroid, cat['vector'])
		p = cat['pageRank']
		score = - (c / np.log10(p))
		if score > closest[1]:
			closest = (cat['article'], score)
	return closest

# for item in clusters:
#     print "Cluster ", item, " - ", clusterName(clusters, item, kmeans.cluster_centers_)[0]
#     for i in clusters[item]:
#         print "\t",i

def getCluster(article, clusters):
	for item in clusters:
		if article in clusters[item]:
			return str(item)
	return "-1"


labledData = defaultdict(int)

for i in range(len(articles)):
	labledData[articles[i]] = vectors[i]


# Next line to silence pfDataflakes. This import is needed.
Axes3D

n_components = 2

# t-SNE
tsne = manifold.TSNE(n_components=n_components, init='pca', random_state=0, verbose=1)
labels = sorted(labledData.keys())
fData = tsne.fit_transform([labledData[w] for w in labels])

scaleMult = 20
scaleAdd = 500

def scale(value):
	return str(float(value)*scaleMult + scaleAdd)

np.savetxt('rawData.txt', fData, fmt="%.9f", delimiter=' ')
raw = open("rawData.txt")
displayFile = open("display.html", "w")
displayFile.write(htmlTop)
for i, line in enumerate(raw.readlines()):
	x, y = line.split()
	s = "[" + scale(x) + ", " + scale(y) + ", " + '\"' + labels[i] + '\", ' + getCluster(labels[i],clusters) + "],\n"
	print s.encode('utf-8')
	displayFile.write(s.encode('utf-8'))
displayFile.write(htmlBottom)

