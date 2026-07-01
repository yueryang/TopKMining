from os import makedirs, walk
from os.path import abspath, dirname, isdir, isfile, islink, join, relpath, sep, split, splitext
from sys import argv, exit
from subprocess import TimeoutExpired, run
try:
	chdir(abspath(dirname(__file__)))
except:
	pass
EXIT_SUCCESS = 0
EXIT_FAILURE = 1
EOF = (-1)


class Drawer:
	def __init__(self:object) -> object:
		self.__read_csv = None
		self.__read_excel = None
		self.__to_numeric = None
		self.__dictionary = None
		self.__escapeTEX = lambda x:"\\textbackslash{}".join(
			string.replace("#", "\\#").replace("$", "\\$").replace("%", "\\%").replace("&", "\\&").replace("_", "\\_").replace("{", "\\{").replace("}", "\\}")
			.replace("<", "\\textless{}").replace(">", "\\textgreater{}").replace("^", "\\textasciicircum{}").replace("~", "\\textasciitilde{}")
			for string in "".join(character for character in str(x) if ' ' <= character <= '~').split("\\")
		)
	def load(self:object, inputFilePath:str, preprocess:object, controlledVariableColumnNames:object, independentVariableColumnName:str, dependentVariableColumnNames:object) -> int:
		try:
			# inputFilePath -> dataFrame #
			extension = splitext(inputFilePath)[1]
			if ".csv" == extension:
				if self.__read_csv is None:
					self.__read_csv = __import__("pandas").read_csv
				dataFrame = self.__read_csv(inputFilePath)
			elif ".xlsx" == extension:
				if self.__read_excel is None:
					self.__read_excel = __import__("pandas").read_excel
				dataFrame = self.__read_excel(inputFilePath)
			else:
				print("Failed to load data from {0} due to the invalid extension. ".format(inputFilePath))
				return EOF
			
			# dataFrame -> self.__dictionary #
			dataFrame = dataFrame.copy()
			if isinstance(preprocess, dict):
				for key, value in preprocess.items():
					if isinstance(key, str) and key in dataFrame.columns:
						dataFrame[key] = value(dataFrame[key])
			self.__dictionary = {
				controlledVariables:{
					independentVariable:innerDataFrame[dependentVariableColumnNames].mean().to_dict()
					for independentVariable, innerDataFrame in outerDataFrame.groupby(independentVariableColumnName, sort = False)
				} for controlledVariables, outerDataFrame in dataFrame.groupby(controlledVariableColumnNames, sort = False)
			}
			if self.__dictionary:
				print("Successfully loaded {0} groups of controlled variables from {1}. ".format(len(self.__dictionary), repr(inputFilePath)))
			else:
				print("No groups of controlled variables were loaded from {0}. ".format(repr(inputFilePath)))
			return len(self.__dictionary)
		except BaseException as e:
			print("Failed to load data from {0} due to {1}. ".format(repr(inputFilePath), repr(e)))
			return EOF
	def draw(
		self:object, outputFilePath:str, dependentVariableColumnName:str, plt:object, preprocessX:object, preprocessY:object, getMarker:object, 
		getColor:object, getLabel:object, xLabelName:str, yLabelName:str, xFontSize:int = 14, yFontSize:int = 14, legendFontSize:int = 10
	) -> bool:
		if isinstance(self.__dictionary, dict):
			if self.__dictionary:
				if isinstance(outputFilePath, str) and isinstance(dependentVariableColumnName, str):
					try:
						for controlledVariables, experimentalVariables in self.__dictionary.items():
							items = []
							for independentVariable, dependentVariables in experimentalVariables.items():
								if dependentVariableColumnName in dependentVariables:
									items.append((independentVariable, dependentVariables[dependentVariableColumnName]))
							items.sort(key = lambda x:x[0])
							xValues, yValues = [], []
							for index in range(len(items)):
								xValues.append(preprocessX(items[index][0]))
								yValues.append(preprocessY(items[index][1]))
							plt.plot(
								xValues, yValues, marker = getMarker(controlledVariables), 
								color = getColor(controlledVariables), linestyle = '-', label = getLabel(controlledVariables)
							)
						plt.xlabel(xLabelName, fontsize = xFontSize)
						plt.ylabel(yLabelName, fontsize = yFontSize)
						plt.legend(loc = "best", frameon = True, fontsize = legendFontSize)
						plt.tight_layout()
						try:
							directoryPath = dirname(outputFilePath)
							if directoryPath:
								makedirs(directoryPath, exist_ok = True)
							plt.savefig(outputFilePath, bbox_inches = "tight")
							print("Successfully saved the drawing to {0}. ".format(repr(outputFilePath)))
							return True
						except BaseException as innerBaseException:
							plt.show()
							print("Displayed instead of saving the drawing to {0} due to {1}. ".format(repr(outputFilePath), repr(innerBaseException)))
							return False
						finally:
							plt.close()
					except BaseException as outerBaseException:
						print("Failed to save the drawing to {0} due to {1}. ".format(repr(outputFilePath), repr(outerBaseException)))
						return False
				else:
					print("Please pass the output file path and the dependent variable column name as strings for drawing. ")
					return False
			else:
				print("No data were loaded for drawing. ")
		else:
			print("Please load data before drawing. ")
			return False
	def summarize(self:object, getAlgorithm:object) -> dict:
		summary = {}
		for controlledVariables, experimentalVariables in self.__dictionary.items():
			outerKey = getAlgorithm(controlledVariables)
			if outerKey is not None:
				for independentVariable, dependentVariables in experimentalVariables.items():
					innerKeys = tuple(dependentVariables.keys())
					for innerKey in innerKeys:
						summary.setdefault(innerKey, {})
						summary[innerKey].setdefault(outerKey, {})
						summary[innerKey][outerKey][independentVariable] = dependentVariables[innerKey]
		return summary

class Drawers:
	__to_numeric = None
	__plt = None
	__ln = None
	__DefaultCompilationTimeout = 10
	def __init__(self:object) -> object:
		self.__filePaths = None
		self.__summaries = None
	def collect(self:object, fp:str|tuple|list, ext:str|tuple|list|set, extensionCaseSensitive:bool = False) -> int:
		stack, extensions = [ext], set()
		if extensionCaseSensitive is True:
			while stack:
				element = stack.pop()
				if isinstance(element, (tuple, list)):
					stack.extend(reversed(element))
				elif isinstance(element, set):
					stack.extend(sorted(element, reverse = True))
				elif isinstance(element, str):
					extensions.add(element)
		else:
			while stack:
				element = stack.pop()
				if isinstance(element, (tuple, list)):
					stack.extend(reversed(element))
				elif isinstance(element, set):
					stack.extend(sorted(element, reverse = True))
				elif isinstance(element, str):
					extensions.add(element.lower())
		stack, self.__filePaths, baseExceptionDict = [fp], [], {}
		if extensionCaseSensitive is True:
			while stack:
				element = stack.pop()
				if isinstance(element, (tuple, list)):
					stack.extend(reversed(element))
				elif isinstance(element, set):
					stack.extend(sorted(element, reverse = True))
				elif isinstance(element, str) and not element in baseExceptionDict:
					try:
						if not islink(element):
							if isdir(element):
								for root, directoryNames, fileNames in walk(element):
									for fileName in fileNames:
										relativeFilePath = relpath(join(root, fileName))
										if splitext(fileName)[1] in extensions and relativeFilePath not in self.__filePaths:
											self.__filePaths.append(relativeFilePath)
							elif isfile(element) and splitext(fileName)[1] in extensions:
								relativeFilePath = relpath(element)
								if relativeFilePath not in self.__filePaths:
									self.__filePaths.append(relativeFilePath)
					except BaseException as e:
						baseExceptionDict[element] = e
		else:
			while stack:
				element = stack.pop()
				if isinstance(element, (tuple, list)):
					stack.extend(reversed(element))
				elif isinstance(element, set):
					stack.extend(sorted(element, reverse = True))
				elif isinstance(element, str) and not element in baseExceptionDict:
					try:
						if not islink(element):
							if isdir(element):
								filePaths = []
								for root, directoryNames, fileNames in walk(element):
									for fileName in fileNames:
										relativeFilePath = relpath(join(root, fileName))
										if splitext(fileName)[1].lower() in extensions and relativeFilePath not in self.__filePaths:
											filePaths.append(relativeFilePath)
								filePaths.sort()
								self.__filePaths.extend(filePaths)
								del filePaths
							elif isfile(element) and splitext(element)[1].lower() in extensions:
								relativeFilePath = relpath(element)
								if relativeFilePath not in self.__filePaths:
									self.__filePaths.append(relativeFilePath)
					except BaseException as e:
						baseExceptionDict[element] = e
		if baseExceptionDict:
			print("Collected {0} file path(s) in {1} file type(s) with {2} base exception(s) {3}. ".format(len(self.__filePaths), len(extensions), len(baseExceptionDict), baseExceptionDict))
		else:
			print("Successfully collected {0} file path(s) in {1} file type(s). ".format(len(self.__filePaths), len(extensions)))
		self.__summarizes = None
		return len(self.__filePaths)
	@staticmethod
	def configure() -> bool:
		try:
			if Drawers.__to_numeric is None:
				Drawers.__to_numeric = __import__("pandas").to_numeric
			if Drawers.__plt is None:
				from matplotlib import pyplot as plt
				Drawers.__plt = plt
				Drawers.__plt.rcParams["font.family"] = "Times New Roman"
				Drawers.__plt.rcParams["font.size"] = 12
				Drawers.__plt.rcParams["mathtext.fontset"] = "custom"
				Drawers.__plt.rcParams["mathtext.rm"] = "Times New Roman"
				Drawers.__plt.rcParams["mathtext.it"] = "Times New Roman:italic"
				Drawers.__plt.rcParams["mathtext.bf"] = "Times New Roman:bold"
			if Drawers.__ln is None:
				Drawers.__ln = __import__("numpy").log
			print("Successfully configured the drawers. ")
			return True
		except KeyError as e:
			print("Failed to configure the drawers due to {0}. ".format(repr(e)))
			return False
	@staticmethod
	def __format(_formatter:str = "", _m:str = "", _n:str = "", _p:str = "", _s:str = sep, _x:str = ".py") -> str:
		formatter = _formatter if isinstance(_formatter, str) else "%p%s%m.pdf"
		m, n, p = _m if isinstance(_m, str) else "", _n if isinstance(_n, str) else "", _p if isinstance(_p, str) else ""
		s, x = _s if isinstance(_s, str) else sep, _x if isinstance(_x, str) else ".py"
		buffer, index, length = [], 0, len(formatter)
		while index < length:
			if '%' == formatter[index]:
				index += 1
				if index < length:
					if '%' == formatter[index]:
						buffer.append("%")
					elif 'm' == formatter[index]:
						buffer.append(m)
					elif 'n' == formatter[index]:
						buffer.append(n)
					elif 'p' == formatter[index]:
						buffer.append(p)
					elif 's' == formatter[index]:
						buffer.append(s)
					elif 'x' == formatter[index]:
						buffer.append(x)
					else:
						buffer.append("%" + formatter[index])
					index += 1
				else:
					buffer.append("%")
					break
			else:
				buffer.append(formatter[index])
				index += 1
		return "".join(buffer)
	@staticmethod
	def __tuple2str(items:tuple|list, itemPrefix:str = "", itemSuffix:str = "") -> str:
		try:
			if len(items) >= 3:
				return ", ".join("{0}{1}{2}".format(itemPrefix, item, itemSuffix) for item in items[:-1]) + ", and " + "{0}{1}{2}".format(itemPrefix, items[-1], itemSuffix)
			elif len(items) >= 2:
				return "{0}{1}{2} and {0}{3}{2}".format(itemPrefix, items[0], itemSuffix, items[1])
			elif len(items) == 1:
				return "{0}{1}{2}".format(itemPrefix, items[0], itemSuffix)
			else:
				return ""
		except:
			return ""
	@staticmethod
	def __summarize(summary:dict) -> str:
		strings = []
		for metric in summary:
			if "consumption" in metric.lower():
				pairs = [(key, sum(value.values())) for key, value in summary[metric].items() if isinstance(value, dict)]
				pairs.sort(key = lambda x:x[1])
				if len(pairs) >= 2:
					strings.append("In terms of {0}, the {1} algorithm outperforms {2} by {3}{4}. ".format(
						metric.lower()[:metric.lower().index("consumption")].strip(), pairs[0][0], Drawers.__tuple2str(tuple(pair[0] for pair in pairs[1:])), 
						Drawers.__tuple2str(tuple("{0:.2f}%".format((pair[1] - pairs[0][1]) * 100 / pair[1]) for pair in pairs[1:])), ", respectively" if len(pairs) >= 3 else ""
					))
			else:
				pairs = [(key, sum(value.values())) for key, value in summary[metric].items() if isinstance(value, dict)]
				pairs.sort(key = lambda x:x[1], reverse = True)
				if len(pairs) >= 2:
					strings.append("In terms of {0}, the {1} algorithm outperforms {2} by {3}{4}. ".format(
						metric.lower().strip(), pairs[0][0], Drawers.__tuple2str(tuple(pair[0] for pair in pairs[1:])), 
						Drawers.__tuple2str(tuple("{0:.2f}%".format((pairs[0][1] - pair[1]) * 100 / pair[1]) for pair in pairs[1:])), ", respectively" if len(pairs) >= 3 else ""
					))
		return "".join(strings)
	def draw(
		self:object, ext:str = ".pdf", getMarker:object = lambda x:None, getColor:object = lambda x:None, 
		getLabel:object = lambda x:None, getAlgorithm:object = lambda x:(x[0] if 0.5 == x[1] and 0.5 == x[2] else None)
	) -> int: # ("%p", "%s", "%n", "%m", "%x") = ("directoryPath", "/", "mainFileName.extension", "mainFileName", ".extension")
		if Drawers.__to_numeric is None or Drawers.__ln is None or Drawers.__plt is None:
			print("Please configure successfully before drawing in any instance. ")
			return EOF
		else:
			extension = "." + "".join(
				character for character in ext[1:] if '0' <= character <= '9' or 'A' <= character <= 'Z' or 'a' <= character <= 'z'
			) if isinstance(ext, str) and ext.startswith(".") else ".pdf"
			successCount, self.__summaries, conclusion = 0, {}, {}
			for filePath in self.__filePaths:
				p, n = split(filePath)
				if not p:
					p = "."
				m, x = splitext(n)
				drawer = Drawer()
				if (
					drawer.load(
						filePath, {
							"$\\alpha$":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), "$\\beta$":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), 
							"$\\delta_0$":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), "$k$":(lambda x:(Drawers.__to_numeric(x, errors = "coerce"))), 
							"runCount":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), "Time consumption (ns)":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), 
							"Memory consumption (B)":(lambda x:Drawers.__to_numeric(x, errors = "coerce")), "$\\delta^*$":(lambda x:Drawers.__to_numeric(x, errors = "coerce"))
						}, ["Algorithm", "$\\alpha$", "$\\beta$"], "$k$", ["Time consumption (ns)", "Memory consumption (B)", "$\\delta^*$"]
					) >= 1 and drawer.draw(
						Drawers.__format("%p%s%m-time{0}".format(extension), _m = m, _n = n, _p = p, _x = x), "Time consumption (ns)", 
						Drawers.__plt, lambda x:Drawers.__ln(x), lambda x:x / 1000000000, getMarker, getColor, getLabel, "$\\ln k$", "Time consumption (s)"
					) and drawer.draw(
						Drawers.__format("%p%s%m-space{0}".format(extension), _m = m, _n = n, _p = p, _x = x), "Memory consumption (B)", 
						Drawers.__plt, lambda x:Drawers.__ln(x), lambda x:x / 1048576, getMarker, getColor, getLabel, "$\\ln k$", "Memory consumption (MB)"
					) and drawer.draw(
						Drawers.__format("%p%s%m-delta{0}".format(extension), _m = m, _n = n, _p = p, _x = x), "$\\delta^*$", 
						Drawers.__plt, lambda x:Drawers.__ln(x), lambda x:x, getMarker, getColor, getLabel, "$\\ln k$", "$\\delta^*$"
					)
				):
					successCount += 1
				Drawers.__plt.close()
				summary = drawer.summarize(getAlgorithm)
				datasetNameBuffer = []
				for character in m:
					if '0' <= character <= '9' or 'A' <= character <= 'Z'  or '_' == character or 'a' <= character <= 'z':
						datasetNameBuffer.append(character)
					else:
						break
				datasetName = "".join(datasetNameBuffer)
				for outerKey, outerValue in summary.items():
					self.__summaries.setdefault(outerKey, {})
					self.__summaries[outerKey].setdefault(datasetName, {})
					conclusion.setdefault(outerKey, {})
					for middleKey, middleValue in outerValue.items():
						self.__summaries[outerKey][datasetName].setdefault(middleKey, {})
						conclusion[outerKey].setdefault(middleKey, {})
						for innerKey, innerValue in middleValue.items():
							if innerKey in self.__summaries[outerKey][datasetName][middleKey]:
								self.__summaries[outerKey][datasetName][middleKey][innerKey] += innerValue
							else:
								self.__summaries[outerKey][datasetName][middleKey][innerKey] = innerValue
							if innerKey in conclusion[outerKey][middleKey]:
								conclusion[outerKey][middleKey][innerKey] += innerValue
							else:
								conclusion[outerKey][middleKey][innerKey] = innerValue
				print("{0} -> \"{1}\"".format(repr(filePath), Drawers.__summarize(summary)))
				print()
			print(Drawers.__summarize(conclusion))
			return successCount
	@staticmethod
	def __getCaption(metric:str) -> str: # this static method function must align to the following static method function
		try:
			if "time" in metric.lower():
				return (
					"The time consumption comparison results (in seconds) for different $k$ values when $\\alpha = \\beta = 0.5$. "
					+ "The TTFE algorithm achieves the lowest time consumption in most cases. "
				)
			elif "memory" in metric.lower():
				return (
					"The memory consumption comparison results (in seconds) for different $k$ values when $\\alpha = \\beta = 0.5$. "
					+ "The TTFE algorithm achieves the lowest memory consumption in most cases. "
				)
			elif "$\\delta^*$" == metric:
				return (
					"The minimum threshold $\\delta^*$ value comparison results for different $k$ values when $\\alpha = \\beta = 0.5$. "
					+ "The TTFE algorithm achieves the highest minimum threshold $\\delta^*$ values in most cases. "
				)
			else:
				return "The comparison resutls. "
		except BaseException as e:
			return "The comparison resutls. "
	@staticmethod
	def __formatValue(x:str, metric:str, decimalPlace:int = 3) -> str: # this static method function must align to the above static method function
		try:
			if "time" in metric.lower():
				return "{{0:.{0}f}}".format(decimalPlace).format(x / 1000000000)
			elif "memory" in metric.lower():
				return "{{0:.{0}f}}".format(decimalPlace).format(x / 1048576)
			elif "$\\delta^*$" == metric:
				return "{{0:.{0}f}}".format(decimalPlace).format(x)
			else:
				return str(x)
		except BaseException as e:
			return "~"
	def toLaTeX(
		self:object, outputFilePath:str, getCaption:object = __getCaption, columnDataset:str = "Dataset", columnAlgorithm:str = "Algorithm", 
		columnFormatter:str = "$k = {0}$", formatValue:object = __formatValue, encoding:str = "utf-8"
	) -> bool:
		if self.__summaries is None:
			print("Please draw before converting to LaTeX. ")
			return False
		else:
			try:
				directoryPath = dirname(outputFilePath)
				if directoryPath:
					makedirs(directoryPath, exist_ok = True)
				with open(outputFilePath, "w", encoding = encoding) as f:
					f.write("\\documentclass[a4paper]{article}\n\\setlength{\\parindent}{0pt}\n\\usepackage{graphicx}\n")
					f.write("\\usepackage{booktabs}\n\\usepackage{multirow}\n\n\\begin{document}\n\n")
					for outerKey, outerValue in self.__summaries.items():
						datasetNames, independentVariableValues, algorithmNames = tuple(outerValue.keys()), tuple(), tuple()
						for middleKey, middleValue in outerValue.items():
							for innerKey, innerValue in middleValue.items():
								independentVariableValues = tuple(innerValue.keys())
								break
							algorithmNames = tuple(middleValue.keys())
							break
						independentVariableIndexes, algorithmCount = tuple(range(len(independentVariableValues))), len(algorithmNames)
						f.write("\\begin{table*}[htbp]\n")
						f.write("\t\\caption{{{0}. }}\n".format(getCaption(outerKey)))
						tableLabel = []
						for character in outerKey:
							if '0' <= character <= '9' or 'A' <= character <= 'Z' or '_' == character or 'a' <= character <= 'z':
								tableLabel.append(character)
							else:
								break
						f.write("\t\\label{{tab:{0}}}\n\t\\centering\n".format("".join(tableLabel).lower()))
						f.write("\t\\resizebox{\\textwidth}{!}{\n\t\t\\begin{tabular}{" + "c" * (2 + len(independentVariableValues)) + "}\n\t\t\t\\toprule\n")
						f.write("\t\t\t\\textbf{{{0}}} & \\textbf{{{1}}}".format(columnDataset, columnAlgorithm))
						for independentVariableValue in independentVariableValues:
							f.write(" & \\textbf{{{0}}}".format(columnFormatter.format(independentVariableValue)))
						f.write(" \\\\\n")
						compare = (lambda a, b:a < b) if "consumption" in outerKey.lower() else (lambda a, b:a > b)
						for middleKey, middleValue in outerValue.items():
							matrix = []
							for innerKey, innerValue in middleValue.items():
								matrix.append(list(innerValue.values()))
							for independentVariableIndex in independentVariableIndexes:
								optimalValue = None
								for rowIndex in range(algorithmCount):
									if optimalValue is None or compare(matrix[rowIndex][independentVariableIndex], optimalValue):
										optimalValue = matrix[rowIndex][independentVariableIndex]
								for rowIndex in range(algorithmCount):
									if optimalValue is None or not compare(optimalValue, matrix[rowIndex][independentVariableIndex]):
										matrix[rowIndex][independentVariableIndex] = "\\textbf{{{0}}}".format(
											formatValue(matrix[rowIndex][independentVariableIndex], outerKey)
										)
									else:
										matrix[rowIndex][independentVariableIndex] = formatValue(matrix[rowIndex][independentVariableIndex], outerKey)
							f.write("\t\t\t\\midrule\n")
							emptyCell = False
							for rowIndex, innerKey in enumerate(middleValue.keys()):
								if emptyCell:
									f.write("\t\t\t~ & ")
								else:
									f.write("\t\t\t\\multirow{{{0}}}{{*}}{{{1}}} & ".format(algorithmCount, middleKey))
									emptyCell = True # mark the following cells before each of the following algorithms within this dataset as empty
								f.write(str(innerKey))
								for independentVariableIndex in independentVariableIndexes:
									f.write(" & " + matrix[rowIndex][independentVariableIndex])
								f.write(" \\\\\n")
						f.write("\t\t\t\\bottomrule\n\t\t\\end{tabular}\n\t}\n\\end{table*}\n\n")
					f.write("\\end{document}")
				print("Successfully wrote to {0}. ".format(repr(outputFilePath)))
				return True
			except BaseException as e:
				print("Failed to write to {0} due to {1}. ".format(repr(outputFilePath), repr(e)))
	@staticmethod
	def toPDF(inputFilePath:str) -> bool:
		try:
			directoryPath, fileName = split(inputFilePath)
			outputFilePath = splitext(inputFilePath)[0] + ".pdf"
			result = run(("pdflatex", fileName), capture_output = True, text = True, timeout = Drawers.__DefaultCompilationTimeout, cwd = directoryPath)
			if EXIT_SUCCESS == result.returncode:
				print("Successfully compiled {0} to {1}. ".format(repr(inputFilePath), repr(outputFilePath)))
				return True
			else:
				print("Failed to compile {0} to {1} due to {2}. ".format(repr(inputFilePath), repr(outputFilePath), result))
				return False
		except TimeoutExpired as e:
			print("Failed to compile {0} to {1} due to {2}. ".format(
				repr(inputFilePath), repr(outputFilePath), {"cmd":e.cmd, "stderr":e.stderr, "stdout":e.stdout, "timeout":e.timeout}
			))
			return False
		except BaseException as e:
			print("Failed to compile {0} to {1} due to {2}. ".format(repr(inputFilePath), repr(outputFilePath), repr(e)))
			return False


def main() -> int:
	drawers = Drawers()
	totalCount = drawers.collect(argv[1:], {".csv", ".xlsx"})
	if totalCount >= 1 and Drawers.configure():
		print()
		successCount = drawers.draw(
			getMarker = lambda x:{"THUFI":'^', "GUMM":'s', "TTFE":'o'}.get(x[0]), 
			getColor = lambda x:{0:"purple", 0.25:"blue", 0.5:"cyan", 0.75:"green", 1:"brown"}.get(x[1]) if "TTFE" == x[0] else {"THUFI":"red", "GUMM":"orange"}.get(x[0]), 
			getLabel = lambda x:"{0} ($\\alpha = {1}$)".format(x[0], x[1])
		)
		resultLaTeXFilePath = "Outputs/summaries.tex"
		flag = drawers.toLaTeX(resultLaTeXFilePath) and drawers.toPDF(resultLaTeXFilePath)
		print()
		errorLevel = EXIT_SUCCESS if successCount == totalCount and flag else EXIT_FAILURE
	else:
		errorLevel = EOF
	try:
		print("Please press the enter key to exit ({0}). ".format(errorLevel))
		input()
	except:
		print()
	return errorLevel



if "__main__" == __name__:
	exit(main())