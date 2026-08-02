import os
from sys import exit
try:
	from graphviz import Digraph
	from tqdm import tqdm
except Exception as e:
	print("Failed to import required third-party libraries due to {0}. ".format(repr(e)))
	print("Press the enter key to exit. ")
	input()
	exit(-1)
os.chdir(os.path.abspath(os.path.dirname(__file__)))
EXIT_SUCCESS = 0
EXIT_FAILURE = 1
EOF = -1
tree = {7:[83.5, 9.5, 74.0, {(7, 1):[41.0, 11.5, 29.5, {}], (7, 5):[20.0, 4.5, 15.5, {}], (7, 4):[22.5, 10.5, 12.0, {}], (7, 3):[66.5, 38.5, 28.0, {}], (7, 6):[30.5, 17.0, 13.5, {}], (7, 2):[24.0, 24.0, 0.0, {}]}], 1:[144.0, 27.5, 116.5, {(1, 5):[106.5, 34.5, 72.0, {(1, 5, 4):[90.0, 48.0, 42.0, {(1, 5, 4, 3):[52.0, 32.5, 19.5, {}], (1, 5, 4, 6):[85.5, 66.0, 19.5, {}], (1, 5, 4, 2):[67.5, 67.5, 0.0, {}]}], (1, 5, 3):[56.0, 33.0, 23.0, {}], (1, 5, 6):[75.5, 54.5, 21.0, {(1, 5, 6, 2):[75.5, 75.5, 0.0, {}]}], (1, 5, 2):[55.5, 55.5, 0.0, {}]}], (1, 4):[78.0, 36.0, 42.0, {(1, 4, 3):[45.0, 25.5, 19.5, {}], (1, 4, 6):[73.5, 54.0, 19.5, {}], (1, 4, 2):[55.5, 55.5, 0.0, {}]}], (1, 3):[83.5, 39.5, 44.0, {(1, 3, 6):[83.5, 62.0, 21.5, {(1, 3, 6, 2):[83.5, 83.5, 0.0, {}]}], (1, 3, 2):[61.0, 61.0, 0.0, {}]}], (1, 6):[89.5, 55.0, 34.5, {(1, 6, 2):[89.5, 89.5, 0.0, {}]}], (1, 2):[62.0, 62.0, 0.0, {}]}], 5:[141.0, 24.5, 116.5, {(5, 4):[112.0, 51.0, 61.0, {(5, 4, 3):[79.0, 45.0, 34.0, {(5, 4, 3, 6):[79.0, 64.0, 15.0, {}], (5, 4, 3, 2):[60.0, 60.0, 0.0, {}]}], (5, 4, 6):[103.0, 75.0, 28.0, {(5, 4, 6, 2):[103.0, 103.0, 0.0, {}]}], (5, 4, 2):[79.0, 79.0, 0.0, {}]}], (5, 3):[85.5, 43.5, 42.0, {(5, 3, 6):[85.5, 69.0, 16.5, {(5, 3, 6, 2):[67.0, 67.0, 0.0, {}]}], (5, 3, 2):[46.0, 46.0, 0.0, {}]}], (5, 6):[84.5, 55.0, 29.5, {(5, 6, 2):[77.0, 77.0, 0.0, {}]}], (5, 2):[51.0, 51.0, 0.0, {}]}], 4:[152.0, 52.5, 99.5, {(4, 3):[124.0, 64.5, 59.5, {(4, 3, 6):[106.0, 72.5, 33.5, {(4, 3, 6, 2):[106.0, 106.0, 0.0, {}]}], (4, 3, 2):[100.5, 100.5, 0.0, {}]}], (4, 6):[121.5, 75.0, 46.5, {(4, 6, 2):[121.5, 121.5, 0.0, {}]}], (4, 2):[101.5, 101.5, 0.0, {}]}], 3:[134.0, 45.5, 88.5, {(3, 6):[122.0, 73.5, 48.5, {(3, 6, 2):[106.5, 106.5, 0.0, {}]}], (3, 2):[85.5, 85.5, 0.0, {}]}], 6:[104.0, 42.5, 61.5, {(6, 2):[99.5, 99.5, 0.0, {}]}], 2:[64.0, 64.0, 0.0, {}]}
order = (7, 1, 5, 4, 3, 6, 2)
rootNodeStyle = "<<table border=\"1\" cellspacing=\"0\"><tr><td port=\"f0\">{0}</td><td port=\"f1\">{1}</td></tr></table>>"
nodeStyle = "<<table border=\"1\" cellspacing=\"0\"><tr><td port=\"f0\"><i>{0}</i></td></tr><tr><td port=\"f1\">{1}</td></tr><tr><td port=\"f2\">{2}</td></tr><tr><td port=\"f3\">{3}</td></tr><tr><td port=\"f4\">{4}</td></tr></table>>"


def intTuple2str(sequence:int|tuple|list) -> str:
	if isinstance(sequence, int):
		return chr(sequence + 96)
	elif isinstance(sequence, (tuple, list)):
		return "".join([intTuple2str(target) for target in sequence])
	else:
		return str(sequence)

def buildTree(g:Digraph, node:int|tuple|list, currentLocation:list|dict) -> None:
	for event in order:
		if node is None:
			sequence = event
		elif isinstance(node, int):
			sequence = (node, event)
			if event == node:
				continue
		elif isinstance(node, tuple):
			sequence = node + (event, )
			if event in node:
				continue
		elif isinstance(node, list):
			sequence = node + [event]
			if event in node:
				continue
		else:
			continue
		if isinstance(currentLocation, dict):
			values = currentLocation[sequence]
			g.node(intTuple2str(sequence), label = nodeStyle.format(intTuple2str(sequence), values[0], values[1], values[2], len(values[3])))
			g.edge("Root:f1", "{0}:f0".format(intTuple2str(sequence)))
			if values[-1]:
				buildTree(g, sequence, values)
		elif sequence in currentLocation[-1]:
			values = currentLocation[-1][sequence]
			g.node(intTuple2str(sequence), label = nodeStyle.format(intTuple2str(sequence), values[0], values[1], values[2], len(values[3])))
			g.edge("{0}:f4".format(intTuple2str(node)), "{0}:f0".format(intTuple2str(sequence)))
			if values[-1]:
				buildTree(g, sequence, values)

def main() -> int:
	successCount = 0
	totalCount = 1
	
	# Sub #
	for event in tqdm([event for event in order if event in tree], desc = "Building sub trees"):
		totalCount += 1
		try:
			g = Digraph(chr(event + 96), filename = "tree{0}.gv".format(event), node_attr = {"shape":"plain"})
			g.node(intTuple2str(event), label = nodeStyle.format(intTuple2str(event), tree[event][0], tree[event][1], tree[event][2], len(tree[event][3])))
			buildTree(g, event, tree[event])
			g.view()
			successCount += 1
		except:
			pass
	
	# Main #
	try:
		g = Digraph("Root", filename = "tree.gv", node_attr = {"shape":"plain"})
		g.node("Root", label = rootNodeStyle.format("Root", len(tree)))
		buildTree(g, None, tree)
		g.view()
		successCount += 1
	except Exception as e:
		print(e)
	
	if totalCount == 0:
		print("Nothing was handled. ")
	else:
		print("Success rate: {0} / {1} = {2}%".format(successCount, totalCount, successCount * 100 / totalCount))
	return EXIT_SUCCESS if successCount == totalCount else EXIT_FAILURE



if __name__ == "__main__":
	exit(main())