import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;


enum LogLevel
{
	All((byte)0), 
	Trace((byte)1), 
	Debug((byte)2), 
	Info((byte)3), 
	Warning((byte)4), 
	Error((byte)5), 
	Fatal((byte)6), 
	Off((byte)7);
	
	private final byte value;
	
	private LogLevel(final byte level) 
	{
		this.value = level;
	}
	public byte getValue()
	{
		return this.value;
	}
	@Override
	public String toString()
	{
		switch (this)
		{
			case All:
				return "All (" + this.value + ")";
			case Trace:
				return "Trace (" + this.value + ")";
			case Debug:
				return "Debug (" + this.value + ")";
			case Info:
				return "Info (" + this.value + ")";
			case Warning:
				return "Warning (" + this.value + ")";
			case Error:
				return "Error (" + this.value + ")";
			case Fatal:
				return "Fatal (" + this.value + ")";
			case Off:
				return "Off (" + this.value + ")";
			default:
				return "Unknown (" + this.value + ")";
		}
	}
}


class Formatter
{
	private static final String HexadecimalCharacterSet = "0123456789ABCDEF";
	
	public static String array2String(final Object[] arguments, final String prefix, final String separator, final String suffix, final Function<Object, String> escaper)
	{
		StringBuilder sb = new StringBuilder(null == prefix ? "" : String.valueOf(prefix));
		if (arguments != null && arguments.length >= 1)
			if (null == escaper)
			{
				for (int index = 0; index < arguments.length; ++index)
					if (arguments[index] != null)
					{
						sb.append(arguments[index]);
						final String realSeparator = null == separator ? "" : String.valueOf(separator);
						for (++index; index < arguments.length; ++index)
							if (arguments[index] != null)
								sb.append(realSeparator).append(arguments[index]);
						break;
					}
			}
			else
			{
				sb.append(escaper.apply(arguments[0]));
				final String realSeparator = null == separator ? "" : String.valueOf(separator);
				for (int index = 1; index < arguments.length; ++index)
					sb.append(realSeparator).append(escaper.apply(arguments[index]));
			}
		sb.append(null == suffix ? "" : String.valueOf(suffix));
		return sb.toString();
	}
	public static String array2String(final Object[] arguments, final String prefix, final String separator, final String suffix) { return array2String(arguments, prefix, separator, suffix, null); }
	public static String array2String(final Object[] arguments) { return array2String(arguments, "[", "|", "]", null); }
	public static String arrayList2String(final ArrayList<?> arrayList, final String itemPrefix, final String itemSuffix)
	{
		if (null == arrayList || arrayList.isEmpty())
			return "";
		else
		{
			final int size = arrayList.size();
			StringBuilder sb = new StringBuilder();
			final String realItemPrefix = null == itemPrefix ? "" : itemPrefix, realItemSuffix = null == itemSuffix ? "" : itemSuffix;
			switch (size)
			{
			case 0:
				break;
			case 1:
				sb.append(realItemPrefix).append(arrayList.get(0)).append(realItemSuffix);
				break;
			case 2:
				sb.append(realItemPrefix).append(arrayList.get(0)).append(realItemSuffix).append(" and ").append(realItemPrefix).append(arrayList.get(1)).append(realItemSuffix);
				break;
			default:
				final int lastIndex = size - 1;
				for (int i = 0; i < lastIndex; ++i)
					sb.append(realItemPrefix).append(arrayList.get(i)).append(realItemSuffix).append(", ");
				sb.append("and ").append(realItemPrefix).append(arrayList.get(lastIndex)).append(realItemSuffix);
			}
			return sb.toString();
		}
	}
	public static String arrayList2String(final ArrayList<?> arrayList) { return arrayList2String(arrayList, "", ""); }
	public static String escapeString(final Object object)
	{
		StringBuilder sb = new StringBuilder("\"");
		for (final char character : String.valueOf(object).toCharArray())
			switch (character)
			{
			case '\b': // 0x08
				sb.append("\\b");
				break;
			case '\t': // \x09
				sb.append("\\t");
				break;
			case '\n': // \x0A
				sb.append("\\n");
				break;
			case '\f': // \x0C
				sb.append("\\f");
				break;
			case '\r': // \x0D
				sb.append("\\r");
				break;
			case '\"':
			case '\'':
			case '\\':
				sb.append("\\").append(character);
				break;
			default:
				if (32 <= character && character <= 126)
					sb.append(character);
				else
					sb.append("\\u").append(HexadecimalCharacterSet.charAt(character >> 12)).append(HexadecimalCharacterSet.charAt(character >> 8 & 0b1111))
						.append(HexadecimalCharacterSet.charAt(character >> 4 & 0b1111)).append(HexadecimalCharacterSet.charAt(character & 0b1111));
				break;
			}
		sb.append("\"");
		return sb.toString();
	}
	public static String filterPrintableAsciiCharacters(final Object object)
	{
		StringBuilder sb = new StringBuilder();
		for (final char character : String.valueOf(object).toCharArray())
			if (32 <= character && character <= 126)
				sb.append(character);
		return sb.toString();
	}
	public static String escapeCSV(final Object object)
	{
		return String.valueOf(object).replace("\n", "").replace("\r", "").replace("\"", "").replace(",", "");
	}
	public static String escapeHTML(final Object object)
	{
		return String.valueOf(object).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}
	public static String escapeJSON(final Object object)
	{
		return String.valueOf(object).replace("\\", "\\\\").replace("\"", "\\\"");
	}
	public static String escapeXMLTag(final Object object)
	{
		final String s = String.valueOf(object);
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray())
			sb.append(Character.isLetterOrDigit(c) || '_' == c || '-' == c ? c : '_');
		if (sb.length() == 0 || !Character.isLetter(sb.charAt(0)))
			sb.insert(0, '_');
		return sb.toString();
	}
	public static String escapeXMLContent(final Object object)
	{
		return String.valueOf(object).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
	public static String escapeTEX(final Object object)
	{
		return String.valueOf(object).replace("\\", "\\textbackslash{}").replace("&", "\\&").replace("%", "\\%").replace("$", "\\$").replace("#", "\\#")
			.replace("_", "\\_").replace("{", "\\{").replace("}", "\\}").replace("~", "\\textasciitilde{}").replace("^", "\\textasciicircum{}");
	}
}

class Parser
{
	private static final LogLevel DefaultLogLevel = LogLevel.Info;
	private static final int DefaultRunCount = 10;
	private static final String[] HelpArguments = { "?", "/?", "-?", "h", "/h", "-h", "help", "/help", "--help" };
	private static final String[] DatasetArguments = { "d", "/d", "-d", "dataset", "/dataset", "--dataset" };
	private static final String[] LogLevelArguments = { "l", "/l", "-l", "logLevel", "/logLevel", "--logLevel" };
	private static final String[] OutputFilePathArguments = { "o", "/o", "-o", "output", "/output", "--output" };
	private static final String[] RunCountArguments = { "r", "/r", "-r", "runCount", "/runCount", "--runCount" };
	
	private String warnings = null;
	private String logs = null;
	private boolean exitFlag = false;
	private String dataset = null;
	private LogLevel logLevel = LogLevel.Info;
	private String outputFilePath = null;
	private int runCount = DefaultRunCount;
	
	public Parser()
	{
		
	}
	private static boolean contains(final String[] array, final String target)
	{
		if (null == array || 0 == array.length || null == target)
			return false;
		else
		{
			for (final String element : array)
				if (element != null && element.equalsIgnoreCase(target))
					return true;
			return false;
		}
	}
	private static void printHelp()
	{
		System.out.println("This is a runCountner for multiple top-$k$ mining algorithms. ");
		System.out.println();
		System.out.println("Options:");
		System.out.println("\t" + Formatter.array2String(HelpArguments) + "\t\tPrint the help warnings.");
		System.out.println("\t" + Formatter.array2String(DatasetArguments) + " <dataset>\t\tSpecify the dataset. ");
		System.out.println(
			"\t" + Formatter.array2String(LogLevelArguments) + " <level>\t\tSpecify the log level from "
			+ LogLevel.All + " to " + LogLevel.Off + ". The default value is " + DefaultLogLevel + ". "
		);
		System.out.println("\t" + Formatter.array2String(OutputFilePathArguments) + " <output>\t\tSpecify the output. ");
		System.out.println("\t" + Formatter.array2String(RunCountArguments) + " <runCount>\t\tSpecify the runCount count to repeat. The default value is " + DefaultRunCount + ". ");
		System.out.println();
		System.out.println("Notes:");
		System.out.println(
			"\t1) All arguments are optional and processed sequentially. If the same argument is provided multiple times, "
			+ "the last valid one will overwrite previous ones. Unrecognized or invalid arguments will be skipped with a warning. "
		);
		System.out.println("\t2) Each unrecognized line in the dataset will be skipped with a warning. ");
		System.out.println(
			"\t3) If the output is not provided, the result will be printed to the console. "
			+ "The parent directory for the output will be automatically created if the output is a file path and its parent directory does not exist. "
		);
		System.out.println();
		return;
	}
	private boolean parseLogLevel(final String string)
	{
		if (null == string || string.isEmpty())
			return false;
		else
			switch ((byte)string.charAt(0))
			{
			case 'A':
			case 'a':
				this.logLevel = LogLevel.All;
				return true;
			case 'T':
			case 't':
				this.logLevel = LogLevel.Trace;
				return true;
			case 'D':
			case 'd':
				this.logLevel = LogLevel.Debug;
				return true;
			case 'I':
			case 'i':
				this.logLevel = LogLevel.Info;
				return true;
			case 'W':
			case 'w':
				this.logLevel = LogLevel.Warning;
				return true;
			case 'E':
			case 'e':
				this.logLevel = LogLevel.Error;
				return true;
			case 'F':
			case 'f':
				this.logLevel = LogLevel.Fatal;
				return true;
			case 'O':
			case 'o':
				this.logLevel = LogLevel.Off;
				return true;
			default:
				final byte lowerBound = LogLevel.All.getValue(), upperBound = LogLevel.Off.getValue();
				if (0 <= lowerBound && lowerBound <= upperBound && upperBound <= 9)
				{
					final byte x = (byte)(string.charAt(0) >= '0' ? string.charAt(0) - '0' : string.charAt(0));
					switch (x)
					{
					case 0:
						this.logLevel = LogLevel.All;
						return true;
					case 1:
						this.logLevel = LogLevel.Trace;
						return true;
					case 2:
						this.logLevel = LogLevel.Debug;
						return true;
					case 3:
						this.logLevel = LogLevel.Info;
						return true;
					case 4:
						this.logLevel = LogLevel.Warning;
						return true;
					case 5:
						this.logLevel = LogLevel.Error;
						return true;
					case 6:
						this.logLevel = LogLevel.Fatal;
						return true;
					case 7:
						this.logLevel = LogLevel.Off;
						return true;
					default:
						return false;
					}
				}
				else
					return false;
			}
	}
	private boolean parseRunCount(final String string)
	{
		if (null == string || string.isEmpty())
			return false;
		else
		{
			int frontIndex = 0, radix = 0, endIndex = string.length() - 1, value = 0;
			boolean isNegative = false;
			for (boolean breakFlag = false; frontIndex < string.length(); ++frontIndex)
			{
				switch (string.charAt(frontIndex))
				{
				case '\t':
				case ' ':
				case '+':
				case '_':
					continue;
				case '-':
					isNegative = !isNegative;
					break;
				default:
					breakFlag = true;
					break;
				}
				if (breakFlag)
					break;
			}
			for (boolean breakFlag = false; frontIndex < string.length(); ++frontIndex) // make ``frontIndex`` point to the first effective digit
			{
				switch (string.charAt(frontIndex))
				{
				case '\t':
				case ' ':
				case '0':
				case '_':
					continue;
				case 'X':
				case 'x':
					radix = 16;
					++frontIndex;
					breakFlag = true;
					break;
				case 'D':
				case 'd':
					radix = 10;
					++frontIndex;
					breakFlag = true;
					break;
				case 'O':
				case 'o':
					radix = 8;
					++frontIndex;
					breakFlag = true;
					break;
				case 'Q':
				case 'q':
					radix = 4;
					++frontIndex;
					breakFlag = true;
					break;
				case 'B':
				case 'b':
					radix = 2;
					++frontIndex;
					breakFlag = true;
					break;
				default:
					breakFlag = true;
					break;
				}
				if (breakFlag)
					break;
			}
			if (0 == radix) // prefix is prior to suffix
				for (boolean breakFlag = false; endIndex > frontIndex; --endIndex) // make ``endIndex`` point to the first effective digit
				{
					switch (string.charAt(endIndex))
					{
					case '\t':
					case ' ':
					case '_':
						continue;
					case 'X':
					case 'x':
						radix = 16;
						--endIndex;
						breakFlag = true;
						break;
					case 'D':
					case 'd':
						radix = 10;
						--endIndex;
						breakFlag = true;
						break;
					case 'O':
					case 'o':
						radix = 8;
						--endIndex;
						breakFlag = true;
						break;
					case 'Q':
					case 'q':
						radix = 4;
						--endIndex;
						breakFlag = true;
						break;
					case 'B':
					case 'b':
						radix = 2;
						--endIndex;
						breakFlag = true;
						break;
					default:
						breakFlag = true;
						break;
					}
					if (breakFlag)
						break;
				}
			if (0 == radix)
				radix = 10;
			boolean isOverflowed = false, isIllegalDigitDetected = false;
			for (int index = frontIndex; index <= endIndex; ++index)
			{
				final int digit = Character.digit(string.charAt(index), radix);
				if (0 <= digit && digit < radix)
				{
					long testValue = (long)value * radix + digit; // test whether it is overflowed
					if (testValue > Integer.MAX_VALUE)
					{
						value = Integer.MAX_VALUE;
						isOverflowed = true;
						break;
					}
					else
						value = (int)testValue;
				}
				else
					isIllegalDigitDetected = true;
			}
			this.runCount = value;
			ArrayList<String> issues = new ArrayList<String>();
			if (isNegative)
				issues.add("the negative sign removed");
			if (isOverflowed)
				issues.add("an overflow signal captured");
			if (isIllegalDigitDetected)
				issues.add("at least an illegal digit detected");
			if (!issues.isEmpty())
				this.warnings = "Parser: Parsed the runCount to " + this.runCount + " with " + Formatter.arrayList2String(issues) + ". ";
			else
				this.logs = "Parser: Parsed the runCount to " + this.runCount + ". ";
			return true;
		}
	}
	public boolean parseArguments(final String[] arguments, final boolean resetBeforeParsing)
	{
		this.warnings = null;
		this.logs = null;
		this.exitFlag = false;
		if (resetBeforeParsing)
		{
			this.dataset = null;
			this.logLevel = DefaultLogLevel;
			this.outputFilePath = null;
			this.runCount = DefaultRunCount;
		}
		boolean missingArgument = false;
		ArrayList<Integer> invalidArgumentIndexes = new ArrayList<Integer>();
		for (int i = 0; i < arguments.length; ++i)
		{
			if (null == arguments[i])
				invalidArgumentIndexes.add(i);
			else if (this.contains(HelpArguments, arguments[i]))
			{
				this.printHelp();
				this.exitFlag = true;
				return true;
			}
			else if (this.contains(DatasetArguments, arguments[i]))
				if (++i < arguments.length)
					this.dataset = arguments[i];
				else
					missingArgument = true;
			else if (this.contains(OutputFilePathArguments, arguments[i]))
				if (++i < arguments.length)
					this.outputFilePath = arguments[i];
				else
					missingArgument = true;
			else if (this.contains(RunCountArguments, arguments[i]))
				if (++i < arguments.length)
				{
					if (!this.parseRunCount(arguments[i]))
						invalidArgumentIndexes.add(i);
				}
				else
					missingArgument = true;
			else if (this.contains(LogLevelArguments, arguments[i]))
				if (++i < arguments.length)
				{
					if (!this.parseLogLevel(arguments[i]))
						invalidArgumentIndexes.add(i);
				}
				else
					missingArgument = true;
			else
				invalidArgumentIndexes.add(i);
		}
		StringBuilder sb = new StringBuilder();
		if (missingArgument)
			sb.append("The corresponding value for the last argument is missing. ");
		final int invalidArgumentCount = invalidArgumentIndexes.size();
		if (1 == invalidArgumentCount)
			sb.append("The argument whose index is [").append(invalidArgumentIndexes.get(0))
				.append("] could not be recognized, which has been skipped. Please note that [0] is the first user argument, not the executable, JAR, or Java source path. ");
		else if (invalidArgumentCount >= 2)
			sb.append(invalidArgumentCount).append(" arguments, whose indexes are ").append(Formatter.arrayList2String(invalidArgumentIndexes, "[", "]"))
				.append(", could not be recognized, which have been skipped. Please note that [0] is the first user argument, not the executable, JAR, or Java source path. ");
		if (sb.length() >= 1)
			this.warnings = null == this.warnings || this.warnings.isEmpty() ? "Parser: " + sb.toString() : this.warnings + sb.toString();
		sb.setLength(0);
		sb.append("Parsed the dataset as ").append(Formatter.escapeString(this.dataset)).append(". Parsed the output file path as ")
			.append(Formatter.escapeString(this.outputFilePath)).append(". Parsed the run count as ").append(this.runCount).append(". ");
		if (sb.length() >= 1)
			this.logs = null == this.logs || this.logs.isEmpty() ? "Parser: " + sb.toString() : this.logs + sb.toString();
		return this.dataset != null && !this.dataset.isEmpty();
	}
	public boolean parseArguments(String[] arguments) { return this.parseArguments(arguments, true); }
	public boolean getExitFlag()
	{
		return this.exitFlag;
	}
	public String getWarnings()
	{
		return this.warnings;
	}
	public String getLogs()
	{
		return this.logs;
	}
	public static LogLevel getDefaultLogLevel()
	{
		return DefaultLogLevel;
	}
	public LogLevel getLogLevel()
	{
		return this.logLevel;
	}
	public static int getDefaultRunCount()
	{
		return DefaultRunCount;
	}
	public String getDataset()
	{
		return this.dataset;
	}
	public Integer getRunCount()
	{
		return this.runCount;
	}
	public String getOutputFilePath()
	{
		return this.outputFilePath;
	}
}

class Logger
{
	private static final boolean COLORING_DISABLED = System.console() == null || System.getProperty("os.name").toLowerCase().contains("win");
	private static final String CONTENT_COLOR = COLORING_DISABLED ? "" : "\u001B[0m";
	private static final String TRACE_PROMPT = COLORING_DISABLED ? "[Trace]" : CONTENT_COLOR + "[\u001B[2;37mTrace" + CONTENT_COLOR + "]";
	private static final String DEBUG_PROMPT = COLORING_DISABLED ? "[Debug]" : CONTENT_COLOR + "[\u001B[2;93mDebug" + CONTENT_COLOR + "]";
	private static final String INFO_PROMPT = COLORING_DISABLED ? "[Info]" : CONTENT_COLOR + "[\u001B[32mInfo" + CONTENT_COLOR + "]";
	private static final String WARNING_PROMPT = COLORING_DISABLED ? "[Warning]" : CONTENT_COLOR + "[\u001B[33mWarning" + CONTENT_COLOR + "]";
	private static final String ERROR_PROMPT = COLORING_DISABLED ? "[Error]" : CONTENT_COLOR + "[\u001B[31mError" + CONTENT_COLOR + "]";
	private static final String FATAL_PROMPT = COLORING_DISABLED ? "[Fatal]" : CONTENT_COLOR + "[\u001B[91mFatal" + CONTENT_COLOR + "]";
	
	private LogLevel logLevel = Parser.getDefaultLogLevel();
	
	public Logger()
	{
		this.test();
	}
	public Logger(final LogLevel level)
	{
		this.setLogLevel(level);
		this.test();
	}
	public boolean setLogLevel(final LogLevel level)
	{
		if (null == level)
		{
			this.logLevel = Parser.getDefaultLogLevel();
			return false;
		}
		else
		{
			this.logLevel = level;
			return true;
		}
	}
	private void test()
	{
		System.out.print(CONTENT_COLOR);
		System.err.print(CONTENT_COLOR);
		if (COLORING_DISABLED)
			this.print("Logger: The logger has been initialized. The coloring is disabled. ", LogLevel.Debug);
		else
			this.print(
				"Logger: The logger has been initialized. The coloring is enabled as " + TRACE_PROMPT + ", " + DEBUG_PROMPT + ", "
				+ INFO_PROMPT + ", " + WARNING_PROMPT + ", " + ERROR_PROMPT + ", and " + FATAL_PROMPT + ". ", 
				LogLevel.Debug
			);
	}
	public boolean print(final String content, final LogLevel level)
	{
		if (level.getValue() >= this.logLevel.getValue())
			switch (level)
			{
			case Trace:
				System.err.println(TRACE_PROMPT + " " + content);
				return true;
			case Debug:
				System.err.println(DEBUG_PROMPT + " " + content);
				return true;
			case Info:
				System.err.println(INFO_PROMPT + " " + content);
				return true;
			case Warning:
				System.err.println(WARNING_PROMPT + " " + content);
				return true;
			case Error:
				System.err.println(ERROR_PROMPT + "[Error]" + " " + content);
				return true;
			case Fatal:
				System.err.println(FATAL_PROMPT + "[Fatal]" + " " + content);
				return true;
			default:
				return false;
			}
		else
			return false;
	}
}

abstract class Algorithm
{
	private static final double DefaultAlpha = 0.5, DefaultBeta = 0.5;
	private static final Double DefaultDelta = null;
	private static final int DefaultK = 10;
	
	protected String inputFilePath = null;
	protected double alpha = DefaultAlpha, beta = DefaultBeta;
	protected Double delta = DefaultDelta;
	protected int k = DefaultK;
	protected Logger logger = null;
	protected long peakMemory = 0L;
	
	public Algorithm(final String inputFilePath, final double alpha, final double beta, final Double delta, final int k, final Logger logger)
	{
		this.inputFilePath = inputFilePath;
		this.alpha = alpha;
		this.beta = beta;
		this.delta = delta;
		this.k = k;
		this.logger = null == logger ? new Logger() : logger;
		if (this.alpha < 0 || 1 < this.alpha || this.beta < 0 || 1 < this.beta || this.alpha + this.beta != 1)
		{
			this.alpha = DefaultAlpha;
			this.beta = DefaultBeta;
			this.logger.print(
				"Algorithm: The variables $\\alpha$ and $\\beta$ should be two doubles satisfying "
				+ "$0 \\leqslant \\alpha \\leqslant \\land 0 \\leqslant \\beta \\leqslant 1 \\land \\alpha + \\beta = 1$, "
				+ "but they are not, which have been defaulted to " + DefaultAlpha + " and " + DefaultBeta + ", respectively. ", 
				LogLevel.Warning
			);
		}
		if (this.k < 1)
		{
			this.k = DefaultK;
			this.logger.print("Algorithm: The variable $k$ should be a positive integer, but it is not, which has been defaulted to " + DefaultK + ". ", LogLevel.Warning);
		}
		checkMemory();
	}
	protected final boolean checkMemory()
	{
		final long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		if (currentMemory > this.peakMemory)
		{
			this.peakMemory = currentMemory;
			return true;
		}
		else
			return false;
	}
	public abstract Number[] runAlgorithm();
}

class AlgorithmTHUFI extends Algorithm
{
	public AlgorithmTHUFI(final String inputFilePath, final double alpha, final double beta, final Double delta, final int k, final Logger logger)
	{
		super(inputFilePath, alpha, beta, delta, k, logger);
	}
	@Override
	public Number[] runAlgorithm()
	{
		final long startTime = System.nanoTime();
		this.delta = 73.5;
		final long endTime = System.nanoTime();
		return new Number[] { endTime - startTime, this.peakMemory, this.delta };
	}
}

class AlgorithmGUMM extends Algorithm
{
	public AlgorithmGUMM(final String inputFilePath, final double alpha, final double beta, final Double delta, final int k, final Logger logger)
	{
		super(inputFilePath, alpha, beta, delta, k, logger);
	}
	@Override
	public Number[] runAlgorithm()
	{
		final long startTime = System.nanoTime();
		this.delta = 73.5;
		final long endTime = System.nanoTime();
		return new Number[] { endTime - startTime, this.peakMemory, this.delta };
	}
}

class AlgorithmTTFE extends Algorithm
{
	private boolean[] switches = { false, true, true, false, true, true };
	private final ArrayList<Transaction> transactions = new ArrayList<>();
	private LinkedHashMap<Integer, Double> TWTF = new LinkedHashMap<>();
	private int[] sequence = null;
	private Event[] events = null;
	private LinkedHashMap<Integer, Double> ETF = new LinkedHashMap<>();
	private Table LETF = null;
	private final PriorityQueue<Double> letf_e = new PriorityQueue<>();
	private final PriorityQueue<Double> letf_lb = new PriorityQueue<>();
	private final PriorityQueue<HTFE> finalResults = new PriorityQueue<>();
	private int candidateCount = 0;
	private final int BUFFERS_SIZE = 200;

	public AlgorithmTTFE(final String inputFilePath, final double alpha, final double beta, final Double delta, final int k, final Logger logger)
	{
		super(inputFilePath, alpha, beta, delta, k, logger);
	}
	private boolean loadDataset()
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.inputFilePath), StandardCharsets.UTF_8)))
		{
			String line = null;
			int tid = 0;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (!line.isEmpty() && '1' <= line.charAt(0) && line.charAt(0) <= '9')
				{
					Transaction trans = new Transaction(++tid, this.k, this.alpha, this.beta);
					String[] parts = line.split(":");
					if (parts.length < 3)
					{
						this.logger.print("Skipped malformed line (missing ':'): " + line, LogLevel.Warning);
						continue;
					}
					String[] items = parts[0].trim().split("\\s+");
					String[] threats = parts[1].trim().split("\\s+");
					String[] freqs = parts[2].trim().split("\\s+");
					if (items.length != threats.length || items.length != freqs.length)
					{
						this.logger.print("Skipped line with mismatched item/threat/freq count: " + line, LogLevel.Warning);
						continue;
					}
					try
					{
						for (int i = 0; i < items.length; ++i)
						{
							int item = Integer.parseInt(items[i]);
							double threat = Double.parseDouble(threats[i]);
							double freq = Double.parseDouble(freqs[i]);
							trans.put(item, new TF(threat, freq, this.alpha, this.beta));
						}
						if (parts.length >= 4)
						{
							trans.ttf = Double.parseDouble(parts[3]);
						}
					}
					catch (NumberFormatException e)
					{
						this.logger.print("Skipped line due to number format: " + line, LogLevel.Warning);
						continue;
					}
					transactions.add(trans);
				}
			}
		}
		catch (Exception e)
		{
			this.logger.print("Failed to read dataset file: " + this.inputFilePath + " - " + e.getMessage(), LogLevel.Error);
			return false;
		}

		if (Math.abs(this.alpha + this.beta - 1.0) > 1e-9)
		{
			this.logger.print("Alpha and beta do not sum to 1, normalizing.", LogLevel.Warning);
			double sum = this.alpha + this.beta;
			this.alpha /= sum;
			this.beta /= sum;
		}
		if (this.delta != null)
		{
			this.delta = this.delta;
		}
		for (Transaction t : transactions)
		{
			if (t.ttf == null)
			{
				t.update(this.k, this.alpha, this.beta);
			}
		}
		return true;
	}

	@Override
	public Number[] runAlgorithm()
	{
		if (!this.loadDataset())
		{
			return new Number[] { null, null, null };
		}
		if (transactions.isEmpty())
		{
			this.logger.print("No transactions loaded, cannot run algorithm.", LogLevel.Error);
			return new Number[] { null, null, null };
		}
		final long startTime = System.nanoTime();
		try
		{
			computeTWTF();   this.checkMemory();
			sortTWTF();      this.checkMemory();
			computeRTF();    this.checkMemory();
			computeETF();    this.checkMemory();
			sortETF();       this.checkMemory();
			pruneItem();     this.checkMemory();
			sortTTFE();      this.checkMemory();
			generateTable(); this.checkMemory();
			if (switches[2]) { raiseThreshold_LETF_E(); this.checkMemory(); }
			if (switches[3]) { raiseThreshold_LETF_LB(); this.checkMemory(); }
			mineWithUtilityLists();
			this.checkMemory();
		}
		catch (Exception e)
		{
			this.logger.print("Algorithm execution failed: " + e.getMessage(), LogLevel.Error);
			return new Number[] { null, null, null };
		}
		final long endTime = System.nanoTime();
		return new Number[] { endTime - startTime, this.peakMemory, this.delta };
	}

	private void computeTWTF()
	{
		for (Transaction t : transactions)
		{
			for (Integer item : t.events.keySet())
			{
				TWTF.put(item, TWTF.getOrDefault(item, 0.0) + t.ttf);
			}
		}
	}

	private void sortTWTF()
	{
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(TWTF.entrySet());
		list.sort(Map.Entry.comparingByValue());
		TWTF.clear();
		sequence = new int[list.size()];
		events = new Event[list.size()];
		int i = 0;
		for (Map.Entry<Integer, Double> e : list)
		{
			TWTF.put(e.getKey(), e.getValue());
			sequence[i] = e.getKey();
			events[i] = new Event(e.getKey());
			i++;
		}
	}

	private void computeRTF()
	{
		for (Transaction t : transactions)
		{
			for (int i = 0; i < sequence.length; i++)
			{
				if (t.events.containsKey(sequence[i]))
				{
					for (int j = i + 1; j < sequence.length; j++)
					{
						if (t.events.containsKey(sequence[j]))
						{
							t.events.get(sequence[i]).rtf += t.events.get(sequence[j]).tf;
						}
					}
					events[i].transactions.put(t.tid, t.events.get(sequence[i]));
				}
			}
		}
	}

	private void computeETF()
	{
		for (Transaction t : transactions)
		{
			for (Map.Entry<Integer, TF> e : t.events.entrySet())
			{
				ETF.put(e.getKey(), ETF.getOrDefault(e.getKey(), 0.0) + e.getValue().tf);
			}
		}
	}

	private void sortETF()
	{
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(ETF.entrySet());
		list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
		ETF.clear();
		for (Map.Entry<Integer, Double> e : list) ETF.put(e.getKey(), e.getValue());
		if (switches[1] && !list.isEmpty())
		{
			int idx = Math.min(this.k, list.size()) - 1;
			double tmp = list.get(idx).getValue();
			if (delta == null || tmp > delta) delta = tmp;
		}
	}

	private void pruneItem()
	{
		LinkedHashMap<Integer, Double> newTWTF = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> e : TWTF.entrySet())
		{
			if (delta == null || e.getValue() >= delta)
				newTWTF.put(e.getKey(), e.getValue());
			else
				for (Transaction t : transactions) t.events.remove(e.getKey());
		}
		TWTF = newTWTF;
	}

	private void sortTTFE()
	{
		for (int i = 0; i < transactions.size(); i++)
		{
			Transaction old = transactions.get(i);
			Transaction newT = new Transaction(old.tid, old.topK, old.alpha, old.beta);
			for (Map.Entry<Integer, Double> e : TWTF.entrySet())
			{
				if (old.events.containsKey(e.getKey()))
					newT.put(e.getKey(), old.events.get(e.getKey()));
			}
			transactions.set(i, newT);
		}
	}

	private void generateTable()
	{
		if (TWTF.isEmpty()) return;
		int size = TWTF.size();
		int[] columns = new int[size - 1];
		int[] index = new int[size - 1];
		double[][] values = new double[size - 1][size - 1];
		{
			int cnt = 0;
			for (Map.Entry<Integer, Double> e : TWTF.entrySet())
			{
				if (cnt == 0) index[cnt] = e.getKey();
				else if (cnt == size - 1) columns[cnt - 1] = e.getKey();
				else { index[cnt] = e.getKey(); columns[cnt - 1] = e.getKey(); }
				cnt++;
			}
		}
		LETF = new Table(values, index, columns, "LETF");
		for (Transaction t : transactions)
		{
			ArrayList<Integer> itemSeq = new ArrayList<>(t.events.keySet());
			for (int i = 0; i < sequence.length - 1; i++)
			{
				int p = sequence[i];
				if (!t.events.containsKey(p)) continue;
				ArrayList<Integer> subSeq = new ArrayList<>();
				subSeq.add(p);
				double sum = t.events.get(p).tf;
				for (int j = i + 1; j < sequence.length; j++)
				{
					int q = sequence[j];
					if (!t.events.containsKey(q)) break;
					subSeq.add(q);
					sum += t.events.get(q).tf;
					if (t.isSequence(subSeq))
						LETF.addValueByName(p, q, sum);
					else
						break;
				}
			}
		}
	}

	private void raiseThreshold_LETF_E()
	{
		if (LETF == null || LETF.values.length == 0) return;
		int n = LETF.values.length;
		letf_e.offer(LETF.values[n-1][n-1]);
		for (int j = n-2; j >= 0; j--)
		{
			for (int i = j; i >= 0 && letf_e.size() < this.k; i--)
			{
				if (LETF.values[i][j+1] > LETF.values[j][j])
					letf_e.offer(LETF.values[i][j+1]);
				else
				{
					letf_e.offer(LETF.values[j][j]);
					break;
				}
			}
			if (letf_e.size() >= this.k) break;
		}
		if (letf_e.size() >= this.k && (delta == null || letf_e.peek() > delta))
			delta = letf_e.peek();
	}

	private void raiseThreshold_LETF_LB()
	{
		if (LETF == null || LETF.values.length == 0) return;
		for (int i = 0; i < LETF.values.length; i++)
		{
			for (int j = 0; j < LETF.values[i].length; j++)
			{
				int p = LETF.index[i], q = LETF.columns[j];
				ArrayList<Integer> mids = LETF.getMiddleElements(p, q, false);
				double tmp = LETF.values[i][j];
				for (int m = 0; m < 3 && m < mids.size(); m++)
				{
					tmp -= ETF.get(mids.get(m));
					if (delta == null || tmp > delta)
					{
						letf_lb.offer(tmp);
						while (letf_lb.size() > this.k) letf_lb.poll();
					}
					else
						break;
				}
			}
		}
		if (letf_lb.size() >= this.k && (delta == null || letf_lb.peek() > delta))
			delta = letf_lb.peek();
	}

	private void mineWithUtilityLists()
	{
		LinkedHashMap<Integer, UList> mapItemToUList = new LinkedHashMap<>();
		for (int item : sequence)
		{
			if (TWTF.containsKey(item) && (delta == null || TWTF.get(item) >= delta))
			{
				UList ul = new UList(item);
				mapItemToUList.put(item, ul);
			}
		}
		for (Transaction trans : transactions)
		{
			ArrayList<Integer> itemsInTrans = new ArrayList<>();
			for (int item : sequence)
			{
				if (trans.events.containsKey(item))
				{
					itemsInTrans.add(item);
				}
			}
			if (itemsInTrans.isEmpty()) continue;
			double remaining = 0;
			for (int i = itemsInTrans.size() - 1; i >= 0; i--)
			{
				int item = itemsInTrans.get(i);
				double ttf = trans.events.get(item).tf;
				UList ul = mapItemToUList.get(item);
				if (ul != null)
				{
					ul.addElement(new UElement(trans.tid, ttf, remaining));
				}
				remaining += ttf;
			}
		}
		ArrayList<UList> listOfULists = new ArrayList<>();
		for (int item : sequence)
		{
			UList ul = mapItemToUList.get(item);
			if (ul != null && ul.elements.size() > 0)
			{
				listOfULists.add(ul);
			}
		}
		int[] prefix = new int[BUFFERS_SIZE];
		thui(prefix, 0, null, listOfULists);
		mapItemToUList.clear();
	}

	private void thui(int[] prefix, int prefixLength, UList pUL, ArrayList<UList> ULs)
	{
		for (int i = ULs.size() - 1; i >= 0; i--)
		{
			UList X = ULs.get(i);
			if (X.sumIutils >= (delta == null ? 0 : delta))
			{
				savePattern(prefix, prefixLength, X);
			}
		}
		for (int i = ULs.size() - 2; i >= 0; i--)
		{
			UList X = ULs.get(i);
			if (X.sumIutils + X.sumRutils >= (delta == null ? 0 : delta))
			{
				ArrayList<UList> exULs = new ArrayList<>();
				for (int j = i + 1; j < ULs.size(); j++)
				{
					UList Y = ULs.get(j);
					candidateCount++;
					UList ex = construct(pUL, X, Y);
					if (ex != null)
					{
						exULs.add(ex);
					}
				}
				prefix[prefixLength] = X.item;
				thui(prefix, prefixLength + 1, X, exULs);
			}
		}
	}

	private UList construct(UList P, UList px, UList py)
	{
		UList pxyUL = new UList(py.item);
		double totUtil = px.sumIutils + px.sumRutils;
		int ei = 0, ej = 0, Pi = -1;
		while (ei < px.elements.size() && ej < py.elements.size())
		{
			UElement ex = px.elements.get(ei);
			UElement ey = py.elements.get(ej);
			if (ex.tid > ey.tid)
			{
				ej++;
				continue;
			}
			if (ex.tid < ey.tid)
			{
				totUtil -= ex.iutils + ex.rutils;
				if (delta != null && totUtil < delta) return null;
				ei++;
				if (P != null) Pi++;
				continue;
			}
			if (P == null)
			{
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils, ey.rutils));
			}
			else
			{
				while (Pi < P.elements.size() && P.elements.get(++Pi).tid < ex.tid) ;
				UElement e = P.elements.get(Pi);
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rutils));
			}
			ei++; ej++;
		}
		while (ei < px.elements.size())
		{
			UElement ex = px.elements.get(ei);
			totUtil -= ex.iutils + ex.rutils;
			if (delta != null && totUtil < delta) return null;
			ei++;
		}
		return pxyUL;
	}

	private void savePattern(int[] prefix, int length, UList X)
	{
		ArrayList<Integer> seq = new ArrayList<>();
		for (int i = 0; i < length; i++)
		{
			seq.add(prefix[i]);
		}
		seq.add(X.item);
		HTFE htfe = new HTFE(seq, X.sumIutils);
		finalResults.offer(htfe);
		while (finalResults.size() > this.k)
		{
			finalResults.poll();
		}
		if (finalResults.size() >= this.k)
		{
			delta = finalResults.peek().eetf;
		}
	}

	private class TF
	{
		double threat, frequency, tf, rtf;

		TF(double threat, double frequency, double alpha, double beta)
		{
			this.threat = threat; this.frequency = frequency;
			this.tf = alpha * threat + beta * frequency;
		}

		void update(double alpha, double beta)
		{
			this.tf = alpha * threat + beta * frequency;
		}
	}

	private class Transaction
	{
		int tid, topK;
		double alpha, beta;
		LinkedHashMap<Integer, TF> events = new LinkedHashMap<>();
		Double ttf = null;

		Transaction(int tid, int topK, double alpha, double beta)
		{
			this.tid = tid; this.topK = topK; this.alpha = alpha; this.beta = beta;
		}

		boolean contains(Integer item) { return events.containsKey(item); }

		boolean put(Integer item, TF tf)
		{
			if (events.containsKey(item)) return false;
			events.put(item, tf);
			return true;
		}

		boolean remove(Integer item)
		{
			if (!events.containsKey(item)) return false;
			events.remove(item);
			return true;
		}

		double update(int topK, double alpha, double beta)
		{
			this.topK = topK; this.alpha = alpha; this.beta = beta;
			ttf = 0.0;
			for (Map.Entry<Integer, TF> e : events.entrySet())
			{
				e.getValue().update(alpha, beta);
				ttf += e.getValue().tf;
			}
			return ttf;
		}

		boolean isSequence(ArrayList<Integer> seq)
		{
			int idx = index(seq.get(0));
			if (idx == -1) return false;
			for (int i = 1; i < seq.size(); i++)
			{
				if (++idx != index(seq.get(i))) return false;
			}
			return true;
		}

		int index(int event)
		{
			int idx = -1;
			for (Map.Entry<Integer, TF> e : events.entrySet())
			{
				idx++;
				if (e.getKey() == event) return idx;
			}
			return -1;
		}
	}

	private class Event
	{
		int event;
		LinkedHashMap<Integer, TF> transactions = new LinkedHashMap<>();

		Event(int event) { this.event = event; }
	}

	private class Table
	{
		String name;
		int[] index, columns, sequence;
		double[][] values;

		Table(double[][] values, int[] index, int[] columns, String name)
		{
			this.values = values; this.index = index; this.columns = columns; this.name = name;
			this.sequence = new int[index.length + 1];
			this.sequence[0] = index[0];
			for (int i = 0; i < columns.length; i++) this.sequence[i+1] = columns[i];
		}

		boolean addValueByName(int indexName, int columnName, double value)
		{
			int ci = -1, ii = -1;
			for (int i = 0; i < columns.length; i++) if (columns[i] == columnName) { ci = i; break; }
			for (int i = 0; i < index.length; i++) if (index[i] == indexName) { ii = i; break; }
			if (ci == -1 || ii == -1) return false;
			values[ii][ci] += value;
			return true;
		}

		ArrayList<Integer> getMiddleElements(int p, int q, boolean inclusive)
		{
			ArrayList<Integer> arr = new ArrayList<>();
			boolean add = false;
			for (int elem : sequence)
			{
				if (elem == p) add = true;
				else if (elem == q) { if (inclusive) arr.add(q); break; }
				else if (add) arr.add(elem);
			}
			if (inclusive) arr.add(0, p);
			return arr;
		}
	}

	private class UElement
	{
		final int tid;
		double iutils;
		double rutils;

		UElement(int tid, double iutils, double rutils)
		{
			this.tid = tid; this.iutils = iutils; this.rutils = rutils;
		}
	}

	private class UList
	{
		Integer item;
		double sumIutils = 0;
		double sumRutils = 0;
		ArrayList<UElement> elements = new ArrayList<>();

		UList(Integer item) { this.item = item; }

		void addElement(UElement e)
		{
			sumIutils += e.iutils;
			sumRutils += e.rutils;
			elements.add(e);
		}
	}

	private class HTFE implements Comparable<HTFE>
	{
		ArrayList<Integer> sequence;
		double eetf;

		HTFE(ArrayList<Integer> seq, double eetf)
		{
			this.sequence = new ArrayList<>(seq);
			this.eetf = eetf;
		}

		@Override
		public int compareTo(HTFE o) { return Double.compare(this.eetf, o.eetf); }
	}
}

class Saver
{
	private static final String LINE_SEPARATOR = System.lineSeparator();
	
	private String outputFilePath = null;
	private String[] columns = null;
	private Logger logger = null;
	private String escapedOutputFilePath = null;
	
	public Saver(final String outputFilePath, String[] columns, final Logger logger)
	{
		this.outputFilePath = outputFilePath;
		this.columns = columns;
		this.logger = (logger != null) ? logger : new Logger();
		if (null == this.outputFilePath || this.outputFilePath.length() < 1)
			this.logger.print("Saver: The results will display on the console. ", LogLevel.Debug);
		else
		{
			this.escapedOutputFilePath = Formatter.escapeString(this.outputFilePath);
			this.logger.print("Saver: The results will be saved to " + this.escapedOutputFilePath + ". ", LogLevel.Debug);
		}
		if (null == this.columns)
			this.columns = new String[] {};
		if (this.columns.length < 1)
			this.logger.print("Saver: The columns are empty. ", LogLevel.Warning);
	}
	private final boolean displayOnConsole(Object[][] results, final int leftClosing, final int rightOpening)
	{
		System.out.println(Formatter.array2String(this.columns, "", "\t", System.lineSeparator(), column -> Formatter.filterPrintableAsciiCharacters(column)));
		final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
		for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
			System.out.println(Formatter.array2String(results[rIndex], "", "\t", System.lineSeparator(), r -> Formatter.filterPrintableAsciiCharacters(r)));
		System.out.println();
		return true;
	}
	private boolean saveToCSV(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write(Formatter.array2String(this.columns, "", ",", LINE_SEPARATOR, column -> Formatter.escapeCSV(Formatter.filterPrintableAsciiCharacters(column))));
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write(Formatter.array2String(results[rIndex], "", ",", LINE_SEPARATOR, r -> Formatter.escapeCSV(Formatter.filterPrintableAsciiCharacters(r))));
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToHTML(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write("<!DOCTYPE html>" + LINE_SEPARATOR);
			writer.write("<html>" + LINE_SEPARATOR);
			writer.write("\t<head>" + LINE_SEPARATOR);
			writer.write("\t\t<title>Results</title>" + LINE_SEPARATOR);
			writer.write("\t\t<style>" + LINE_SEPARATOR);
			writer.write("\t\t\ttable{width:80%;margin:20px auto;border-collapse:collapse;" + LINE_SEPARATOR);
			writer.write("\t\t\tfont-family:'Times New Roman',serif;border-top:2px solid #000;" + LINE_SEPARATOR);
			writer.write("\t\t\tborder-bottom:2px solid #000;}" + LINE_SEPARATOR);
			writer.write("\t\t\tth,td{border:none;padding:8px 12px;text-align:center;}" + LINE_SEPARATOR);
			writer.write("\t\t\tthead tr{border-bottom:1.5px solid #000;}" + LINE_SEPARATOR);
			writer.write("\t\t\tth{font-weight:bold;}" + LINE_SEPARATOR);
			writer.write("\t\t\tcaption{font-size:1.5em;margin:10px;font-weight:bold;color:#333;caption-side:top;}" + LINE_SEPARATOR);
			writer.write("\t\t</style>" + LINE_SEPARATOR);
			writer.write("\t</head>" + LINE_SEPARATOR);
			writer.write("\t<body>" + LINE_SEPARATOR);
			writer.write("\t\t<table>" + LINE_SEPARATOR);
			writer.write("\t\t\t<caption>Results</caption>" + LINE_SEPARATOR);
			writer.write("\t\t\t<thead>" + LINE_SEPARATOR);
			writer.write("\t\t\t\t<tr>" + LINE_SEPARATOR);
			writer.write(Formatter.array2String(this.columns, "", "", "", column -> "<th>" + Formatter.escapeHTML(Formatter.filterPrintableAsciiCharacters(column)) + "</th>"));
			writer.write("\t\t\t\t</tr>" + LINE_SEPARATOR);
			writer.write("\t\t\t</thead>" + LINE_SEPARATOR);
			writer.write("\t\t\t<tbody>" + LINE_SEPARATOR);
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
			{
				writer.write("\t\t\t\t<tr>" + LINE_SEPARATOR);
				writer.write(Formatter.array2String(
					results[rIndex], "", "", "", r -> "\t\t\t\t\t<td>" + Formatter.escapeHTML(Formatter.filterPrintableAsciiCharacters(r)) + "</td>" + LINE_SEPARATOR
				));
				writer.write("\t\t\t\t</tr>" + LINE_SEPARATOR);
			}
			writer.write("\t\t\t</tbody>" + LINE_SEPARATOR);
			writer.write("\t\t</table>" + LINE_SEPARATOR);
			writer.write("\t</body>" + LINE_SEPARATOR);
			writer.write("</html>");
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToJSON(Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write("{" + LINE_SEPARATOR);
			writer.write("\t\"columns\": [" + LINE_SEPARATOR);
			writer.write(Formatter.array2String(this.columns, "\t\t", ", " + LINE_SEPARATOR + "\t\t", LINE_SEPARATOR, column -> Formatter.escapeString(column)));
			writer.write("\t], " + LINE_SEPARATOR);
			writer.write("\t\"results\": [" + LINE_SEPARATOR);
			writer.write("\t\t[" + LINE_SEPARATOR);
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				if (results[rIndex] != null)
				{
					writer.write(Formatter.array2String(results[rIndex], "\t\t\t", ", " + LINE_SEPARATOR + "\t\t\t", LINE_SEPARATOR, r -> Formatter.escapeString(r)));
					for (++rIndex; rIndex < realRightOpening; ++rIndex)
					{
						writer.write("\t\t], " + LINE_SEPARATOR);
						writer.write("\t\t[" + LINE_SEPARATOR);
						writer.write(Formatter.array2String(results[rIndex], "\t\t\t", ", " + LINE_SEPARATOR + "\t\t\t", LINE_SEPARATOR, r -> Formatter.escapeString(r)));
					}
					writer.write("\t\t]" + LINE_SEPARATOR);
					break;
				}
			writer.write("\t]" + LINE_SEPARATOR);
			writer.write("}");
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToXML(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR);
			writer.write("<results>" + LINE_SEPARATOR);
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
			{
				writer.write("\t<result>" + LINE_SEPARATOR);
				for (int cIndex = 0; cIndex < this.columns.length; ++cIndex)
				{
					final String tag = Formatter.escapeXMLTag(this.columns[cIndex]), r = cIndex < results[rIndex].length ? String.valueOf(results[rIndex][cIndex]) : "";
					writer.write("\t\t<" + tag + ">" + LINE_SEPARATOR);
					writer.write("\t\t\t" + Formatter.escapeXMLContent(Formatter.filterPrintableAsciiCharacters(r)) + LINE_SEPARATOR);
					writer.write("\t\t</" + tag + ">" + LINE_SEPARATOR);
				}
				writer.write("\t</result>" + LINE_SEPARATOR);
			}
			writer.write("</results>");
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToTEX(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write("\\documentclass[a4paper]{article}");
			writer.write("\\usepackage{booktabs}");
			writer.write("\\usepackage{rotating}");
			writer.write("\\begin{document}");
			writer.write("\\begin{sidewaystable}");
			writer.write("\\caption{Results}");
			writer.write("\\centering");
			writer.write("\\begin{tabular}{" + "c".repeat(this.columns.length) + "}");
			writer.write("\\toprule");
			writer.write(Formatter.array2String(this.columns, "", " & ", "\\\\", column -> Formatter.filterPrintableAsciiCharacters(column)));
			writer.write("\\midrule");
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write(Formatter.array2String(results[rIndex], "", " & ", "\\\\", r -> Formatter.filterPrintableAsciiCharacters(r)));
			writer.write("\\bottomrule");
			writer.write("\\end{tabular}");
			writer.write("\\end{sidewaystable}");
			writer.write("\\end{document}");
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToTSV(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write(Formatter.array2String(this.columns, "", "\t", System.lineSeparator(), column -> Formatter.filterPrintableAsciiCharacters(column)));
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write(Formatter.array2String(results[rIndex], "", "\t", System.lineSeparator(), r -> Formatter.filterPrintableAsciiCharacters(r)));
			this.logger.print("Saver: Successfully saved the results to " + this.escapedOutputFilePath + ". ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Saver: Failed to save the results to " + this.escapedOutputFilePath + " due to " + Formatter.escapeString(e.toString()) + ". ", LogLevel.Error);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	public boolean save(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		if (null == results || results.length < 1)
			this.logger.print("Saver: The results are empty. ", LogLevel.Warning);
		if (null == this.escapedOutputFilePath)
			return this.displayOnConsole(results, leftClosing, rightOpening);
		else
		{
			final int dotIndex = this.outputFilePath.lastIndexOf('.');
			final String documentType = dotIndex >= 1 ? this.outputFilePath.substring(dotIndex + 1).toUpperCase() : "";
			switch (documentType)
			{
			case "CSV":
				return this.saveToCSV(results, leftClosing, rightOpening);
			case "HTM":
			case "HTML":
				return this.saveToHTML(results, leftClosing, rightOpening);
			case "JSON":
				return this.saveToJSON(results, leftClosing, rightOpening);
			case "TEX":
				return this.saveToTEX(results, leftClosing, rightOpening);
			case "XML":
				return this.saveToXML(results, leftClosing, rightOpening);
			default:
				return this.saveToTSV(results, leftClosing, rightOpening);
			}
		}
	}
}

public class TopKMining
{
	final static int EXIT_SUCCESS = 0;
	final static int EXIT_FAILURE = 1;
	final static int EOF = (-1);
	final static double EPSILON = 0.0001;
	
	private static String filterMainFileName(final String filePath)
	{
		final String fileName = Paths.get(filePath).getFileName().toString();
		final int dotIndex = fileName.lastIndexOf('.');
		final String mainFileName = dotIndex >= 1 ? fileName.substring(0, dotIndex) : fileName;
		return mainFileName.replaceAll("[^\\-0-9A-Za-z]", "");
	}
	private static boolean checkComplexity(final Number number)
	{
		return number != null && (long)number >= 1;
	}
	private static boolean checkDelta(final Number a, final Number b)
	{
		if (null == a && null == b)
			return true;
		if ((null == a) ^ (null == b))
			return false;
		else if (a instanceof Double || b instanceof Double || a instanceof Float || b instanceof Float)
			return Math.abs(a.doubleValue() - b.doubleValue()) <= EPSILON;
		else
			return a.longValue() == b.longValue();
	}
	public static void main(final String[] arguments)
	{
		final Parser parser = new Parser();
		boolean flag = parser.parseArguments(arguments);
		if (parser.getExitFlag())
			System.exit(EXIT_SUCCESS);
		else
		{
			final String warnings = parser.getWarnings();
			final String logs = parser.getLogs();
			final Logger logger = new Logger(parser.getLogLevel());
			if (warnings != null && !warnings.isEmpty())
				logger.print(warnings, LogLevel.Warning);
			if (logs != null && !logs.isEmpty())
				logger.print(logs, LogLevel.Debug);
			if (flag)
			{
				/* Parameters */
				final String dataset = parser.getDataset();
				LinkedHashMap<String, Function<Number[], Algorithm>> algorithms = new LinkedHashMap<>();
				algorithms.put("THUFI", parameters -> new AlgorithmTHUFI(dataset, (double)parameters[0], (double)parameters[1], (Double)parameters[2], (int)parameters[3], logger));
				algorithms.put("GUMM", parameters -> new AlgorithmGUMM(dataset, (double)parameters[0], (double)parameters[1], (Double)parameters[2], (int)parameters[3], logger));
				algorithms.put("TTFE", parameters -> new AlgorithmTTFE(dataset, (double)parameters[0], (double)parameters[1], (Double)parameters[2], (int)parameters[3], logger));
				final double[] alphaValues = { 0, 0.25, 0.5, 0.75, 1 };
				final Double[] deltaValues = new Double[] { null };
				final int[] kValues = { 5, 10, 50, 100, 500, 1000, 5000, 10000 };
				final int runCount = parser.getRunCount();
				
				/* Algorithms */
				Number[] parameters = new Number[] { null, null, null, null };
				String[] columns = { "Dataset", "Algorithm", "$\\alpha$", "$\\beta$", "$\\delta_0$", "$k$", "Run count", "Time consumption (ns)", "Memory consumption (B)", "$\\delta^*$" };
				final int length = columns.length, metricLength = 3;
				Object[][] results = new Object[algorithms.size() * alphaValues.length * deltaValues.length * kValues.length][length];
				int outerIndex = 0;
				final String datasetName = filterMainFileName(dataset);
				Saver saver = new Saver(parser.getOutputFilePath(), columns, logger);
				for (Map.Entry<String, Function<Number[], Algorithm>> entry : algorithms.entrySet())
				{
					final String algorithmName = entry.getKey();
					final Function<Number[], Algorithm> algorithmFactory = entry.getValue();
					for (final double alpha : alphaValues)
					{
						parameters[0] = alpha;
						final double beta = 1 - alpha;
						parameters[1] = beta;
						for (final Double delta : deltaValues)
						{
							parameters[2] = delta;
							for (final int k : kValues)
							{
								int innerIndex = 0;
								results[outerIndex][innerIndex++] = datasetName; results[outerIndex][innerIndex++] = algorithmName;
								results[outerIndex][innerIndex++] = alpha; results[outerIndex][innerIndex++] = beta;
								results[outerIndex][innerIndex++] = delta; results[outerIndex][innerIndex++] = k;
								results[outerIndex][innerIndex++] = runCount;
								parameters[3] = k;
								System.gc();
								Algorithm algorithm = algorithmFactory.apply(parameters);
								Number[] result = algorithm.runAlgorithm();
								if (null == result || result.length != metricLength)
									for (int i = 0; i < metricLength; ++i)
										results[outerIndex][innerIndex++] = null;
								else
								{
									for (int run = 2; run < runCount; ++run)
									{
										algorithm = algorithmFactory.apply(parameters);
										Number[] r = algorithm.runAlgorithm();
										if (null == r || r.length != metricLength)
										{
											for (int i = 0; i < metricLength; ++i)
												results[outerIndex][innerIndex++] = null;
											break;
										}
										else
										{
											result[0] = checkComplexity(result[0]) ? (long)result[0] + (long)r[0] : null;
											result[1] = checkComplexity(result[1]) ? (long)result[1] + (long)r[1] : null;
											if (!checkDelta(result[2], r[2]))
												result[2] = null;
										}	
									}
									if (checkComplexity(result[0]))
										if ((long)result[0] % runCount == 0)
											results[outerIndex][innerIndex++] = (long)result[0] / runCount;
										else
											results[outerIndex][innerIndex++] = BigDecimal.valueOf(result[0].doubleValue() / runCount).toPlainString();
									else
									{
										results[outerIndex][innerIndex++] = null;
										flag = false;
									}
									if (checkComplexity(result[1]))
										if ((long)result[1] % runCount == 0)
											results[outerIndex][innerIndex++] = (long)result[1] / runCount;
										else
											results[outerIndex][innerIndex++] = BigDecimal.valueOf(result[1].doubleValue() / runCount).toPlainString();
									else
									{
										results[outerIndex][innerIndex++] = null;
										flag = false;
									}
									results[outerIndex][innerIndex++] = (Double)result[2];
								}
								saver.save(results, 0, ++outerIndex);
							}
						}
					}
				}
				System.exit(saver.save(results, 0, outerIndex) && flag ? EXIT_SUCCESS : EXIT_FAILURE);
			}
			else
			{
				logger.print("The path to the dataset must be specified.", LogLevel.Fatal);
				System.exit(EOF);
			}
		}
	}
}