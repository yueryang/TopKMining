from os import chdir, linesep, makedirs, path
try:
	chdir(path.abspath(path.dirname(__file__)))
except:
	pass
EXIT_SUCCESS = 0
EXIT_FAILURE = 1
EOF = (-1)


class Extender:
	def __init__(self:object, inputFilePaths:tuple|list) -> object:
		self.__inputFilePaths = tuple(inputFilePath for inputFilePath in inputFilePaths if isinstance(inputFilePath, str))
	def extend(self:object, outputFilePath:str, encoding:str = "utf-8", weights:tuple|list = (0.5, 0.5), prefix:str = "", suffix:str = "") -> int|BaseException:
		if Extender.__handleDirectory(path.split(outputFilePath)[0]):
			try:
				with open(outputFilePath, "w", encoding = encoding) as outputFile:
					inputFiles = []
					try:
						for inputFilePath in self.__inputFilePaths:
							inputFiles.append(open(inputFilePath, "r", encoding = encoding))
						outputFile.write(prefix)
						lineCount = 0
						while True:
							lines = tuple(inputFile.readline().rstrip("\n\r") for inputFile in inputFiles)
							if lines and all(lines):
								firstColonIndexes = tuple(line.find(':') for line in lines)
								if all(firstColonIndex >= 1 for firstColonIndex in firstColonIndexes):
									indexes = tuple(range(len(lines)))
									keys = tuple(lines[index][:firstColonIndexes[index]] for index in indexes)
									if all(key == keys[0] for key in keys):
										keyLength = keys[0].count(" ")
										lastColonIndexes = tuple(line.rfind(':') for line in lines)
										if all(lastColonIndex >= 1 for lastColonIndex in lastColonIndexes):
											values = tuple(lines[index][lastColonIndexes[index] + 1:] for index in indexes)
											if all(value.count(" ") == keyLength for value in values):
												try:
													matrix = tuple(value.split(" ") for value in values)
													average = sum(weights[index] * sum(float(row[index]) for row in matrix) for index in indexes)
												except:
													continue
											outputFile.write(keys[0] + ":" + ":".join(values) + ":" + str(average) + linesep)
											lineCount += 1
									else:
										continue
							else:
								break
						outputFile.write(suffix)
						for inputFile in inputFiles:
							inputFile.close()
						return lineCount
					except BaseException as innerBaseException:
						for inputFile in inputFiles:
							inputFile.close()
						return innerBaseException
			except BaseException as outerBaseException:
				return outerBaseException
		else:
			return OSError("Failed to prepare the parent directory. ")
	@staticmethod
	def __handleDirectory(p:str) -> bool:
		try:
			directoryPath = str(p)
		except:
			return False
		if not directoryPath:
			return True
		elif path.exists(directoryPath):
			return path.isdir(directoryPath)
		else:
			try:
				makedirs(directoryPath)
				return True
			except:
				return False


def main():
	# Parameters #
	datasetNames = ("accidents", "chess", "kosarak", "mushroom")
	positiveFilePathFormatter = "./{0}_positive.txt"
	negativeFilePathFormatter = "./{0}_negative.txt"
	outputFilePathFormatter = "./{0}.txt"
	prefix = "# Event : Threat : Frequency : TTF" + linesep
	
	# Extension #
	flag = True
	for datasetName in datasetNames:
		extender = Extender((positiveFilePathFormatter.format(datasetName), negativeFilePathFormatter.format(datasetName)))
		outputFilePath = outputFilePathFormatter.format(datasetName)
		result = extender.extend(outputFilePath)
		if isinstance(result, int):
			print("{0} -> {1}".format(repr(outputFilePath), result))
		else:
			print("{0} -> {1}".format(repr(outputFilePath), repr(result)))
			flag = False
	return EXIT_SUCCESS if flag else EXIT_FAILURE



if __name__ == "__main__":
	exit(main())