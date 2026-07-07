import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
	public static String escapeString(final Object object)
	{
		StringBuilder sb = new StringBuilder("\"");
		for (final char character : String.valueOf(object).toCharArray())
			switch (character)
			{
			case '\b': // 0u0008
				sb.append("\\b");
				break;
			case '\t': // \u0009
				sb.append("\\t");
				break;
			case '\n': // \u000A
				sb.append("\\n");
				break;
			case '\f': // \u000C
				sb.append("\\f");
				break;
			case '\r': // \u000D
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
	public static String filterMainFileName(final String filePath)
	{
		final String fileName = Paths.get(filePath).getFileName().toString();
		final int dotIndex = fileName.lastIndexOf('.');
		final String mainFileName = dotIndex >= 1 ? fileName.substring(0, dotIndex) : fileName;
		return mainFileName.replaceAll("[^\\-0-9A-Za-z]", "");
	}
	public static String filterString(final Object object)
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
	private static final double DefaultDeltaRatio = 0.7;
	private static final LogLevel DefaultLogLevel = LogLevel.Info;
	private static final int DefaultMaximumTransactionCount = Integer.MAX_VALUE;
	private static final int DefaultRunCount = 10;
	private static final int DefaultStartingTransactionID = 1;
	private static final String[] HelpArguments = { "?", "/?", "-?", "h", "/h", "-h", "help", "/help", "--help" };
	private static final String[] DeltaRatioArguments = {"d", "/d", "-d", "deltaRatio", "/deltaRatio", "--deltaRatio" };
	private static final String[] InputFilePathArguments = { "i", "/i", "-i", "inputFilePath", "/inputFilePath", "--inputFilePath" };
	private static final String[] LogLevelArguments = { "l", "/l", "-l", "logLevel", "/logLevel", "--logLevel" };
	private static final String[] MaximumTransactionCountArguments = { "m", "/m", "-m", "maximumTransactionCount", "/maximumTransactionCount", "--maximumTransactionCount" };
	private static final String[] OutputFilePathArguments = { "o", "/o", "-o", "outputFilePath", "/outputFilePath", "--outputFilePath" };
	private static final String[] RunCountArguments = { "r", "/r", "-r", "runCount", "/runCount", "--runCount" };
	private static final String[] StartingTransactionIDArguments = { "s", "/s", "-s", "startingTransactionID", "/startingTransactionID", "--startingTransactionID" };
	
	private LinkedHashMap<LogLevel, StringBuffer> logMessages = null;
	private double deltaRatio = DefaultDeltaRatio;
	private String inputFilePath = null;
	private LogLevel logLevel = LogLevel.Info;
	private Number maximumTransactionCount = DefaultMaximumTransactionCount;
	private String outputFilePath = null;
	private int runCount = DefaultRunCount;
	private Number startingTransactionID = DefaultStartingTransactionID;
	
	public Parser()
	{
		this.logMessages = new LinkedHashMap<LogLevel, StringBuffer>();
		this.logMessages.put(LogLevel.Trace, null);
		this.logMessages.put(LogLevel.Debug, null);
		this.logMessages.put(LogLevel.Info, null);
		this.logMessages.put(LogLevel.Warning, null);
		this.logMessages.put(LogLevel.Error, null);
		this.logMessages.put(LogLevel.Fatal, null);
	}
	private static boolean containing(final String[] array, final String target)
	{
		if (null == array || 0 == array.length)
			return false;
		else if (null == target)
		{
			for (final String element : array)
				if (null == element)
					return true;
			return false;
		}
		else // target != null
		{
			for (final String element : array)
				if (target.equalsIgnoreCase(element))
					return true;
			return false;
		}
	}
	private static void printHelp()
	{
		System.out.println("This is a runCountner for multiple top-$k$ mining algorithms. ");
		System.out.println();
		System.out.println("Options:");
		System.out.println("\t" + Formatter.array2String(HelpArguments) + "\t\tPrint this help document. ");
		System.out.println(
			"\t" + Formatter.array2String(DeltaRatioArguments) + " <deltaRatio>\t\tSpecify the delta ratio for static threshold algorithms, "
			+ "which should be a ratio within the interval $[0, 1]$. The default value is " + DefaultDeltaRatio + ". "
		);
		System.out.println("\t" + Formatter.array2String(InputFilePathArguments) + " <inputFilePath>\t\tSpecify the input file path to the dataset. ");
		System.out.println(
			"\t" + Formatter.array2String(LogLevelArguments) + " <level>\t\tSpecify the log level from "
			+ LogLevel.All + " to " + LogLevel.Off + ". The default value is " + DefaultLogLevel + ". "
		);
		System.out.println(
			"\t" + Formatter.array2String(MaximumTransactionCountArguments) + " <maximumTransactionCount>\t\tSpecify the maximum transaction count, "
			+ "which can be a positive integer or a decimal ratio within the interval $(0, 1)$. The default value is " + DefaultMaximumTransactionCount + ". "
		);
		System.out.println("\t" + Formatter.array2String(OutputFilePathArguments) + " <outputFilePath>\t\tSpecify the output file path. ");
		System.out.println(
			"\t" + Formatter.array2String(RunCountArguments) + " <runCount>\t\tSpecify the run count to repeat, which should be a positive integer. The default value is " + DefaultRunCount + ". "
		);
		System.out.println(
			"\t" + Formatter.array2String(StartingTransactionIDArguments) + " <startingTransactionID>\t\tSpecify the starting transaction ID, "
			+ "which can be a positive integer or a decimal ratio within the interval $(0, 1)$. The default value is " + DefaultStartingTransactionID + ". "
		);
		System.out.println();
		System.out.println("Notes:");
		System.out.println(
			"\t1) All options are optional and processed sequentially. If the same argument is provided multiple times, "
			+ "the last valid one will silently overwrite previous ones. Unrecognized or invalid arguments will be skipped with an aggregated warning. "
		);
		System.out.println("\t2) The $\\delta_0$ will be set to $0$ if the borrowed oracle threshold $\\delta^*$ becomes negative after being multiplied by the delta ratio. ");
		System.out.println(
			"\t3) The input file path to the dataset must be specified. "
			+ "Each unrecognized line in the dataset will be skipped with an aggregated warning and not count for the starting transaction ID or the maximum transaction count. "
		);
		System.out.println(
			"\t4) If the output file path is not specified or exceptions occur when saving to the output file path, the results will be printed to the console. "
			+ "The parent directory for the output file path will be automatically created if its parent directory does not exist. "
		);
		System.out.println();
		return;
	}
	private boolean appendMessage(final LogLevel level, final String message)
	{
		if (level instanceof LogLevel && message instanceof String && !message.isEmpty())
		{
			if (!this.logMessages.containsKey(level) || !(this.logMessages.get(level) instanceof StringBuffer))
				this.logMessages.put(level, new StringBuffer("Parser: "));
			this.logMessages.get(level).append(message);
			return true;
		}
		else
			return false;
	}
	private Number parseRealNumber(final String string)
	{
		if (string instanceof String)
		{
			final String realNumberString = string.replaceAll("[^+\\-.0-9A-Za-z]", "").toLowerCase();
			if (!realNumberString.contains("x") && realNumberString.contains("e") && !realNumberString.endsWith("e"))
				try
				{
					return Double.parseDouble(realNumberString);
				}
				catch (Throwable e)
				{
					this.appendMessage(LogLevel.Warning, "Parsed " + Formatter.escapeString(string) + " as null due to " + Formatter.escapeString(e) + ". ");
					return null;
				}
			else
			{
				int frontIndex = 0, radix = 0, endIndex = realNumberString.length() - 1;
				boolean isNegative = false, isRegular = true;
				for (boolean breakFlag = false; frontIndex < realNumberString.length(); ++frontIndex)
				{
					switch (realNumberString.charAt(frontIndex))
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
				for (boolean breakFlag = false; frontIndex < realNumberString.length(); ++frontIndex) // make ``frontIndex`` point to the first effective digit
				{
					switch (realNumberString.charAt(frontIndex))
					{
					case '0':
						continue;
					case 'x':
						radix = 16;
						++frontIndex;
						breakFlag = true;
						break;
					case 'd':
						radix = 10;
						++frontIndex;
						breakFlag = true;
						break;
					case 'o':
						radix = 8;
						++frontIndex;
						breakFlag = true;
						break;
					case 'q':
						radix = 4;
						++frontIndex;
						breakFlag = true;
						break;
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
						switch (realNumberString.charAt(endIndex))
						{
						case 'x':
							radix = 16;
							--endIndex;
							breakFlag = true;
							break;
						case 'd':
							radix = 10;
							--endIndex;
							breakFlag = true;
							break;
						case 'o':
							radix = 8;
							--endIndex;
							breakFlag = true;
							break;
						case 'q':
							radix = 4;
							--endIndex;
							breakFlag = true;
							break;
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
				Number number = null;
				if (endIndex - frontIndex == 2)
				{
					final String subString = realNumberString.substring(frontIndex, endIndex + 1);
					switch (subString)
					{
					case "inf":
						number = isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
						isRegular = false;
						break;
					case "nan":
						isRegular = false;
						break;
					default:
						break;
					}
				}
				if (isRegular)
				{
					if (0 == radix)
						radix = 10;
					boolean containingMultipleRadixPoints = false, isDecimal = false, isIllegalDigitDetected = false, isOverflowed = false;
					double decimalValue = 0.0;
					int integerValue = 0;
					for (int index = frontIndex; index <= endIndex; ++index)
						if ('.' == realNumberString.charAt(index))
						{
							frontIndex = ++index;
							for (; index <= endIndex; ++index) // locate the second radix point
								if ('.' == realNumberString.charAt(index))
								{
									containingMultipleRadixPoints = true;
									endIndex = index - 1;
									break;
								}
							while (endIndex > frontIndex)
								if ('0' == realNumberString.charAt(endIndex))
									--endIndex;
								else
									break;
							for (index = endIndex; index >= frontIndex; --index)
							{
								final int digit = Character.digit(realNumberString.charAt(index), radix);
								if (0 <= digit && digit < radix)
								{
									decimalValue += digit;
									decimalValue /= radix;
									isDecimal = true;
								}
								else
									isIllegalDigitDetected = true;
							}
							break;
						}
						else
						{
							final int digit = Character.digit(realNumberString.charAt(index), radix);
							if (0 <= digit && digit < radix)
							{
								final long testValue = (long)integerValue * radix + digit; // test whether it is overflowed
								if (testValue > Integer.MAX_VALUE)
								{
									integerValue = Integer.MAX_VALUE; // this line can be commented out
									isOverflowed = true;
									break;
								}
								else
									integerValue = (int)testValue;
							}
							else
								isIllegalDigitDetected = true;
						}
					if (isDecimal)
						if (isOverflowed)
							number = isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
						else
							number = isNegative ? -(decimalValue + integerValue) : decimalValue + integerValue;
					else
						number = isOverflowed ? (isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE) : (isNegative ? -integerValue : integerValue);
					ArrayList<String> issues = new ArrayList<String>();
					if (containingMultipleRadixPoints)
						issues.add("strings after the second radix point ignored");
					if (isIllegalDigitDetected)
						issues.add("at least an illegal digit detected");
					if (isOverflowed)
						issues.add("an overflow signal captured");
					if (issues.isEmpty())
						this.appendMessage(LogLevel.Trace, "Parsed " + Formatter.escapeString(string) + " as " + number + ". ");
					else
						this.appendMessage(
							LogLevel.Warning, "Parsed " + Formatter.escapeString(string) + " as " + number + " with " + Formatter.arrayList2String(issues) + ". "
						);
				}
				else
					this.appendMessage(LogLevel.Trace, "Parsed " + Formatter.escapeString(string) + " as " + number + ". ");
				return number;
			}
		}
		else
			return null;
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
	public boolean parseArguments(final String[] arguments, final boolean resetBeforeParsing) // the return value indicates whether to exit the whole program due to ``help`` option
	{
		for (Map.Entry<LogLevel, StringBuffer> entry : this.logMessages.entrySet()) // reset all messages
			entry.setValue(null);
		if (resetBeforeParsing)
		{
			this.deltaRatio = DefaultDeltaRatio;
			this.inputFilePath = null;
			this.logLevel = DefaultLogLevel;
			this.maximumTransactionCount = DefaultMaximumTransactionCount;
			this.outputFilePath = null;
			this.runCount = DefaultRunCount;
			this.startingTransactionID = DefaultStartingTransactionID;
		}
		if (arguments instanceof String[])
		{
			ArrayList<Integer> invalidArgumentIndexes = new ArrayList<Integer>();
			boolean missingArgument = false;
			for (int i = 0; i < arguments.length; ++i)
			{
				if (null == arguments[i])
					invalidArgumentIndexes.add(i);
				else if (containing(HelpArguments, arguments[i]))
				{
					printHelp();
					return true;
				}
				else if (containing(DeltaRatioArguments, arguments[i]))
					if (++i < arguments.length)
					{
						final Number number = this.parseRealNumber(arguments[i]);
						if (number instanceof Double)
						{
							final double doubleValue = number.doubleValue();
							if (0 <= doubleValue && doubleValue <= 1)
								this.deltaRatio = doubleValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else
							invalidArgumentIndexes.add(i);
					}
					else
						missingArgument = true;
				else if (containing(InputFilePathArguments, arguments[i]))
					if (++i < arguments.length)
						this.inputFilePath = arguments[i];
					else
						missingArgument = true;
				else if (containing(LogLevelArguments, arguments[i]))
					if (++i < arguments.length)
						if (this.parseLogLevel(arguments[i]))
							this.appendMessage(LogLevel.Trace, "Parsed " + Formatter.escapeString(arguments[i]) + " as " + this.logLevel + ". ");
						else
							invalidArgumentIndexes.add(i);
					else
						missingArgument = true;
				else if (containing(MaximumTransactionCountArguments, arguments[i]))
					if (++i < arguments.length)
					{
						final Number number = this.parseRealNumber(arguments[i]);
						if (number instanceof Integer)
						{
							final int intValue = number.intValue();
							if (intValue >= 1)
								this.maximumTransactionCount = intValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else if (number instanceof Double)
						{
							final double doubleValue = number.doubleValue();
							if (0 < doubleValue && doubleValue < 1)
								this.maximumTransactionCount = doubleValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else
							invalidArgumentIndexes.add(i);
					}
					else
						missingArgument = true;
				else if (containing(OutputFilePathArguments, arguments[i]))
					if (++i < arguments.length)
						this.outputFilePath = arguments[i];
					else
						missingArgument = true;
				else if (containing(RunCountArguments, arguments[i]))
					if (++i < arguments.length)
					{
						final Number number = this.parseRealNumber(arguments[i]);
						if (number instanceof Integer)
						{
							final int intValue = number.intValue();
							if (intValue >= 1)
								this.runCount = intValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else
							invalidArgumentIndexes.add(i);
					}
					else
						missingArgument = true;
				else if (containing(StartingTransactionIDArguments, arguments[i]))
					if (++i < arguments.length)
					{
						final Number number = this.parseRealNumber(arguments[i]);
						if (number instanceof Integer)
						{
							final int intValue = number.intValue();
							if (intValue >= 1)
								this.startingTransactionID = intValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else if (number instanceof Double)
						{
							final double doubleValue = number.doubleValue();
							if (0 < doubleValue && doubleValue < 1)
								this.startingTransactionID = doubleValue;
							else
								invalidArgumentIndexes.add(i);
						}
						else
							invalidArgumentIndexes.add(i);
					}
					else
						missingArgument = true;
				else
					invalidArgumentIndexes.add(i);
			}
			final int invalidArgumentCount = invalidArgumentIndexes.size();
			if (1 == invalidArgumentCount)
				this.appendMessage(
					LogLevel.Warning, "The argument whose index is [" + invalidArgumentIndexes.get(0)
					+ "] could not be recognized, which has been skipped. Please note that [0] is the first user argument, not the executable, JAR, or Java source path. "
				);
			else if (invalidArgumentCount >= 2)
				this.appendMessage(
					LogLevel.Warning, "" + invalidArgumentCount + " arguments, whose indexes are " + Formatter.arrayList2String(invalidArgumentIndexes, "[", "]")
					+ ", could not be recognized, which have been skipped. Please note that [0] is the first user argument, not the executable, JAR, or Java source path. "
				);
			if (missingArgument)
				this.appendMessage(LogLevel.Warning, "The corresponding value for the last argument is missing. ");
			this.appendMessage(
				LogLevel.Debug, "Parsed the delta ratio as " + this.deltaRatio + ", the input file path as " + Formatter.escapeString(this.inputFilePath)
				+ ", the log level as " + this.logLevel + ", the maximum transaction count as " + this.maximumTransactionCount + ", the output file path as "
				+ Formatter.escapeString(this.outputFilePath) + ", the run count as " + this.runCount + ", and the starting transaction ID as " + this.startingTransactionID + ". "
			);
		}
		else
			this.appendMessage(LogLevel.Error, "Skipped parsing arguments due to a null argument array passed. ");
		return false;
	}
	public boolean parseArguments(String[] arguments) { return this.parseArguments(arguments, true); }
	public LinkedHashMap<LogLevel, String> getMessages()
	{
		LinkedHashMap<LogLevel, String> messages = new LinkedHashMap<LogLevel, String>();
		for (Map.Entry<LogLevel, StringBuffer> entry : this.logMessages.entrySet())
			if (entry.getValue() instanceof StringBuffer)
				messages.put(entry.getKey(), entry.getValue().toString());
		return messages;
	}
	public static LogLevel getDefaultLogLevel()
	{
		return DefaultLogLevel;
	}
	public LogLevel getLogLevel()
	{
		return this.logLevel;
	}
	public String getInputFilePath()
	{
		return this.inputFilePath;
	}
	public double getDeltaRatio()
	{
		return this.deltaRatio;
	}
	public int getRunCount()
	{
		return this.runCount;
	}
	public Number getStartingTransactionID()
	{
		return this.startingTransactionID;
	}
	public Number getMaximumTransactionCount()
	{
		return this.maximumTransactionCount;
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
			this.print("Logger: The logger has been initialized. The log level is " + this.logLevel + ". The coloring is disabled. ", LogLevel.Debug);
		else
			this.print(
				"Logger: The logger has been initialized. The log level is " + this.logLevel + ". The coloring is enabled as " + TRACE_PROMPT + ", " + DEBUG_PROMPT + ", "
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
				System.err.println(ERROR_PROMPT + " " + content);
				return true;
			case Fatal:
				System.err.println(FATAL_PROMPT + " " + content);
				return true;
			default:
				return false;
			}
		else
			return false;
	}
}

abstract class Algorithm<T extends Algorithm.Transaction> implements Serializable
{
	static class Transaction implements Serializable
	{
		int tid = 0;
		double ttf = 0.0;
		
		Transaction()
		{
			
		}
		int size()
		{
			return 0;
		}
		LinkedHashSet<Integer> keySet()
		{
			return new LinkedHashSet<Integer>();
		}
	};
	private static class CountingOutputStream extends OutputStream
	{
		private long count = 0L;
		
		@Override
		public void write(int b) throws IOException
		{
			++this.count;
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
        		this.count += len;
		}
		public long size()
		{
			return this.count;
		}
	}
	
	private static final double DefaultAlpha = 0.5, DefaultBeta = 0.5, DefaultDelta = Double.NEGATIVE_INFINITY;
	private static final int DefaultK = 10;
	static final int InitialBufferSize = 100;
	
	String algorithmName = "Specified";
	double alpha = DefaultAlpha, beta = DefaultBeta, delta = DefaultDelta;
	int k = DefaultK;
	transient Logger logger = null;
	ArrayList<T> transactions = null;
	boolean strategy_ETF = true, strategy_LETF_E = true, strategy_LETF_LB = true, strategyPruning = true;
	transient long localMemory = 0L, peakMemory = 0L;
	
	Algorithm(final String _algorithmName, final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		if (_algorithmName instanceof String)
			this.algorithmName = _algorithmName;
		this.alpha = _alpha;
		this.beta = _beta;
		this.delta = _delta;
		this.k = _k;
		this.logger = _logger instanceof Logger ? _logger : new Logger();
		if (this.alpha < 0 || 1 < this.alpha || this.beta < 0 || 1 < this.beta || this.alpha + this.beta != 1)
		{
			this.alpha = DefaultAlpha;
			this.beta = DefaultBeta;
			this.logger.print(
				"Algorithm" + this.algorithmName + ": The variables $\\alpha$ and $\\beta$ should be two doubles satisfying "
				+ "$0 \\leqslant \\alpha \\leqslant \\land 0 \\leqslant \\beta \\leqslant 1 \\land \\alpha + \\beta = 1$, "
				+ "but they are not, which have been defaulted to " + DefaultAlpha + " and " + DefaultBeta + ", respectively. ", 
				LogLevel.Warning
			);
		}
		if (this.k < 1)
		{
			this.k = DefaultK;
			this.logger.print(
				"Algorithm" + this.algorithmName + ": The variable $k$ should be a positive integer, but it is not, which has been defaulted to " + DefaultK + ". ", 
				LogLevel.Warning
			);
		}
		this.logger.print(
			"Algorithm" + this.algorithmName + ": Initialized the " + this.algorithmName + " algorithm with $\\alpha = " + this.alpha
			+ "$, $\\beta = " + this.beta + "$, $\\delta = " + String.valueOf(this.delta).replace("Infinity", "\\infty") + "$, and $k = " + this.k + "$. ", 
			LogLevel.Info
		);
	}
	private final String describe(final String inputFilePath)
	{
		final int transactionCount = this.transactions.size();
		StringBuilder stringBuilder = new StringBuilder();
		if (transactionCount >= 2)
		{
			LinkedHashSet<Integer> differentEvents = new LinkedHashSet<Integer>();
			int overallTransactionLength = 0, maximumTransactionLength = Integer.MIN_VALUE, minimumTransactionLength = Integer.MAX_VALUE;
			double overallTTF = 0.0, maximumTTF = Double.NEGATIVE_INFINITY, minimumTTF = Double.POSITIVE_INFINITY;
			for (final Transaction transaction : this.transactions)
			{
				differentEvents.addAll(transaction.keySet());
				final int transactionLength = transaction.size();
				overallTransactionLength += transactionLength;
				if (transactionLength > maximumTransactionLength)
					maximumTransactionLength = transactionLength;
				if (transactionLength < minimumTransactionLength)
					minimumTransactionLength = transactionLength;
				overallTTF += transaction.ttf;
				if (transaction.ttf > maximumTTF)
					maximumTTF = transaction.ttf;
				if (transaction.ttf < minimumTTF)
					minimumTTF = transaction.ttf;
			}
			final int differentEventCount = differentEvents.size();
			String densityDescription = "";
			if (inputFilePath instanceof String)
				stringBuilder.append("Collected ").append(transactionCount).append(" transactions and ").append(differentEventCount)
					.append(" different event(s) from ").append(Formatter.escapeString(inputFilePath));
			else
				stringBuilder.append("After cropping, ").append(transactionCount).append(" transactions and ")
					.append(differentEventCount).append(" different event(s) remain");
			stringBuilder.append(", where, the first and last transaction IDs are ").append(this.transactions.get(0).tid)
				.append(" and ").append(this.transactions.get(transactionCount - 1).tid).append(", respectively. ");
			if (overallTransactionLength % transactionCount == 0)
			{
				final int averageTransactionLength = overallTransactionLength / transactionCount;
				stringBuilder.append("Each transaction contains $").append(overallTransactionLength).append(" / ")
					.append(transactionCount).append(" = ").append(averageTransactionLength).append("$ event(s) on average");
				if (100 * averageTransactionLength % differentEventCount == 0)
					densityDescription = "The dataset density is " + (100 * averageTransactionLength / differentEventCount) + "%. ";
				else
					densityDescription = "The dataset density is about " + (100.0 * averageTransactionLength / differentEventCount) + "%. ";
			}
			else
			{
				final double averageTransactionLength = (double)overallTransactionLength / transactionCount;
				stringBuilder.append("Each transaction contains about $").append(overallTransactionLength).append(" / ")
					.append(transactionCount).append(" \\approx ").append(averageTransactionLength).append("$ event(s) on average");
				densityDescription = "The dataset density is about " + (100.0 * averageTransactionLength / differentEventCount) + "%. ";
			}
			if (maximumTransactionLength >= minimumTransactionLength)
				stringBuilder.append(", with a range of $").append(maximumTransactionLength).append(" - ")
					.append(minimumTransactionLength).append(" = ").append(maximumTransactionLength - minimumTransactionLength).append("$. ");
			else
				stringBuilder.append(". ");
			stringBuilder.append("The average TTF is about $").append(overallTTF).append(" / ").append(transactionCount).append(" \\approx ").append(overallTTF / transactionCount).append("$");
			if (maximumTTF >= minimumTTF)
				stringBuilder.append(", with a range of $").append(maximumTTF).append(" - ").append(minimumTTF < 0 ? "(" + minimumTTF + ")" : minimumTTF)
					.append(" = ").append(maximumTTF - minimumTTF).append("$. ");
			else
				stringBuilder.append(". ");
			stringBuilder.append(densityDescription);
		}
		else if (1 == transactionCount)
		{
			if (inputFilePath instanceof String)
				stringBuilder.append("Collected 1 transaction, whose transaction ID is ").append(this.transactions.get(0).tid).append(", from ")
					.append(Formatter.escapeString(inputFilePath)).append(". ");
			else
				stringBuilder.append("After cropping, 1 transaction, whose transaction ID is ").append(this.transactions.get(0).tid).append(", remains. ");
			stringBuilder.append("This transaction contains ").append(this.transactions.get(0).size())
				.append(" event(s). Its TTF is ").append(this.transactions.get(0).ttf).append(". The dataset density is 100%. ");
		}
		else
			stringBuilder.append("No transactions were collected from ").append(Formatter.escapeString(inputFilePath)).append(". ");
		return stringBuilder.toString();
	}
	final void cropTransactions(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		final String statistics = this.describe(inputFilePath);
		if (this.transactions.isEmpty())
			this.logger.print("Algorithm" + this.algorithmName + ": " + statistics, LogLevel.Warning);
		else
		{
			final int transactionCount = this.transactions.size(), offset = this.transactions.get(0).tid; // ``offset`` indicates the first transaction ID in ``this.transactions``
			int leftClosing = 0, rightOpening = transactionCount;
			if (startingTransactionID instanceof Integer)
			{
				final int intValue = startingTransactionID.intValue();
				if (intValue >= offset)
					leftClosing = Math.min(intValue, transactionCount) - offset;
			}
			else if (startingTransactionID instanceof Double)
			{
				final double doubleValue = startingTransactionID.doubleValue();
				if (0 < doubleValue && doubleValue < 1)
					leftClosing = (int)(doubleValue * transactionCount) - offset; // ``doubleValue`` must be the left operand of the operator ``*``
			}
			leftClosing = Math.max(leftClosing, 0);
			if (maximumTransactionCount instanceof Integer)
			{
				final int intValue = maximumTransactionCount.intValue();
				if (intValue >= offset)
					rightOpening = Math.max(leftClosing + intValue, 0);
			}
			else if (maximumTransactionCount instanceof Double)
			{
				final double doubleValue = maximumTransactionCount.doubleValue();
				if (0 < doubleValue && doubleValue < 1)
					rightOpening = Math.max(leftClosing + (int)(doubleValue * rightOpening), 0); // ``doubleValue`` must be the left operand of the operator ``*``
			}
			rightOpening = Math.min(rightOpening, this.transactions.size());
			this.transactions.subList(rightOpening, this.transactions.size()).clear();
			this.transactions.subList(0, leftClosing).clear();
			if (this.transactions.isEmpty())
				this.logger.print("Algorithm" + this.algorithmName + ": " + statistics + "However, no transactions remain after the crop operation. ", LogLevel.Warning);
			else if (this.transactions.size() == transactionCount)
				this.logger.print("Algorithm" + this.algorithmName + ": " + statistics, LogLevel.Info);
			else
				this.logger.print("Algorithm" + this.algorithmName + ": " + statistics + this.describe(null), LogLevel.Info);
		}
	}
	static final long getObjectSize(final Object object) throws IOException
	{
		final CountingOutputStream countingOutputStream = new CountingOutputStream();
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(countingOutputStream);
		objectOutputStream.writeObject(object);
		objectOutputStream.close();
		return countingOutputStream.size();
	}
	final boolean checkMemory()
	{
		try
		{
			final long currentMemory = getObjectSize(this) + this.localMemory;
			if (currentMemory > this.peakMemory)
			{
				this.peakMemory = currentMemory;
				return true;
			}
		}
		catch (Throwable e)
		{
			this.logger.print("Algorithm" + this.algorithmName + ": Failed to check memory due to " + Formatter.escapeString(e) + ". ", LogLevel.Error);
		}
		return false;
	}
	public abstract Number[] runAlgorithm(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount);
	public abstract ArrayList<ArrayList<Integer>> getTopKPatterns();
	public abstract ArrayList<Double> getTopKValues();
}

class AlgorithmTHUI extends Algorithm<AlgorithmTHUI.Transaction>
{
	private static class ItemInfo implements Serializable
	{
		double utility = 0.0, rtf = 0.0;
		
		ItemInfo(double utility)
		{
			this.utility = utility;
			this.rtf = 0.0;
		}
	}
	static class Transaction extends Algorithm.Transaction
	{
		LinkedHashMap<Integer, ItemInfo> items = new LinkedHashMap<>();
		
		boolean put(Integer item, ItemInfo info)
		{
			if (this.items.containsKey(item))
				return false;
			else
			{
				items.put(item, info);
				return true;
			}
		}
		@Override
		int size()
		{
			return items.size();
		}
		@Override
		LinkedHashSet<Integer> keySet()
		{
			return new LinkedHashSet<Integer>(items.keySet());
		}
		boolean contains(Integer item)
		{
			return this.items.containsKey(item);
		}
		boolean remove(Integer item)
		{
			if (items.containsKey(item))
			{
				items.remove(item);
				return true;
			}
			else
				return false;
		}
		boolean isSequence(ArrayList<Integer> seq)
		{
			int idx = index(seq.get(0));
			if (idx == -1) return false;
			for (int i = 1; i < seq.size(); ++i)
				if (++idx != index(seq.get(i)))
					return false;
			return true;
		}
		int index(int item)
		{
			int idx = -1;
			for (Map.Entry<Integer, ItemInfo> e : items.entrySet())
			{
				++idx;
				if (e.getKey() == item)
					return idx;
			}
			return -1;
		}
	}
	private static class ItemEvent implements Serializable
	{
		int item = 0;
		LinkedHashMap<Integer, ItemInfo> transactions = new LinkedHashMap<>();

		ItemEvent(int item)
		{
			this.item = item;
		}
	}
	private static class Table implements Serializable
	{
		String name;
		int[] index, columns, sequence;
		double[][] values;

		Table(double[][] values, int[] index, int[] columns, String name)
		{
			this.values = values; this.index = index; this.columns = columns; this.name = name;
			this.sequence = new int[index.length + 1];
			this.sequence[0] = index[0];
			for (int i = 0; i < columns.length; ++i) this.sequence[i+1] = columns[i];
		}
		boolean addValueByName(int indexName, int columnName, double value)
		{
			int ci = -1, ii = -1;
			for (int i = 0; i < columns.length; ++i) if (columns[i] == columnName) { ci = i; break; }
			for (int i = 0; i < index.length; ++i) if (index[i] == indexName) { ii = i; break; }
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
	private static class UElement implements Serializable
	{
		int tid = 0;
		double iutils = 0.0;
		double rutils = 0.0;

		UElement(int tid, double iutils, double rutils)
		{
			this.tid = tid;
			this.iutils = iutils;
			this.rutils = rutils;
		}
	}
	private static class UList implements Serializable
	{
		Integer item = null;
		double sumIutils = 0.0;
		double sumRutils = 0.0;
		ArrayList<UElement> elements = new ArrayList<>();
		
		UList(Integer item)
		{
			this.item = item;
		}
		void addElement(UElement e)
		{
			this.sumIutils += e.iutils;
			this.sumRutils += e.rutils;
			this.elements.add(e);
		}
	}

	private static class HTFE implements Comparable<HTFE>, Serializable
	{
		ArrayList<Integer> sequence = null;
		double eetf = 0.0;
		
		HTFE(ArrayList<Integer> seq, double eetf)
		{
			this.sequence = new ArrayList<>(seq);
			this.eetf = eetf;
		}
		@Override
		public int compareTo(HTFE o) { return Double.compare(this.eetf, o.eetf); }
	}
	
	static private int DefaultColumnIndex = 2;
	
	private int columnIndex = DefaultColumnIndex;
	private LinkedHashMap<Integer, Double> TWTF = new LinkedHashMap<>();
	private int[] sequence = null;
	private ItemEvent[] itemEvents = null;
	private LinkedHashMap<Integer, Double> ETF = new LinkedHashMap<>();
	private Table LETF = null;
	private final PriorityQueue<Double> letf_e = new PriorityQueue<>();
	private final PriorityQueue<Double> letf_lb = new PriorityQueue<>();
	private final PriorityQueue<HTFE> finalResults = new PriorityQueue<>();
	private int candidateCount = 0;
	
	public AlgorithmTHUI(final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		super("THUI", _alpha, _beta, _delta, _k, _logger);
	}
	public boolean setColumnIndex(final int index)
	{
		if (index >= 1)
		{
			this.columnIndex = index;
			return true;
		}
		else
			return false;
	}
	
	/* Child procedures */
	private void savePattern(int[] prefix, int length, UList X)
	{
		ArrayList<Integer> seq = new ArrayList<>();
		for (int i = 0; i < length; ++i)
			seq.add(prefix[i]);
		seq.add(X.item);
		HTFE htfe = new HTFE(seq, X.sumIutils);
		this.finalResults.offer(htfe);
		while (this.finalResults.size() > this.k)
			this.finalResults.poll();
		if (this.strategyPruning)
		{
			if (this.finalResults.size() >= this.k)
				this.delta = this.finalResults.peek().eetf;
			this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the final mining procedure. ", LogLevel.Trace);
		}
	}
	private void thui(int[] prefix, int prefixLength, UList pUL, ArrayList<UList> ULs)
	{
		try
		{
			this.localMemory += getObjectSize(prefix) + getObjectSize(pUL) + getObjectSize(ULs);
		}
		catch (Throwable e) {}
		for (int i = ULs.size() - 1; i >= 0; --i)
		{
			UList X = ULs.get(i);
			if (X.sumIutils >= (Double.NEGATIVE_INFINITY == this.delta ? 0 : delta))
				savePattern(prefix, prefixLength, X);
		}
		for (int i = ULs.size() - 2; i >= 0; --i)
		{
			UList X = ULs.get(i);
			if (X.sumIutils + X.sumRutils >= (Double.NEGATIVE_INFINITY == this.delta ? 0 : delta))
			{
				ArrayList<UList> exULs = new ArrayList<>();
				for (int j = i + 1; j < ULs.size(); ++j)
				{
					UList Y = ULs.get(j);
					++candidateCount;
					UList ex = construct(pUL, X, Y);
					if (ex != null)
						exULs.add(ex);
				}
				if (prefixLength >= prefix.length)
				{
					final int newSize = prefix.length << 1;
					int[] newPrefix = new int[newSize];
					System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
					prefix = newPrefix;
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
			UElement ex = px.elements.get(ei), ey = py.elements.get(ej);
			if (ex.tid > ey.tid)
			{
				++ej;
				continue;
			}
			if (ex.tid < ey.tid)
			{
				totUtil -= ex.iutils + ex.rutils;
				if (totUtil < delta)
					return null;
				++ei;
				if (P != null)
					++Pi;
				continue;
			}
			if (null == P)
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils, ey.rutils));
			else
			{
				while (Pi < P.elements.size() && P.elements.get(++Pi).tid < ex.tid) ;
				UElement e = P.elements.get(Pi);
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rutils));
			}
			++ei;
			++ej;
		}
		while (ei < px.elements.size())
		{
			UElement ex = px.elements.get(ei);
			totUtil -= ex.iutils + ex.rutils;
			if (totUtil < delta)
				return null;
			++ei;
		}
		return pxyUL;
	}
	
	/* Main procedures */
	private boolean loadDataset(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		this.transactions = new ArrayList<Transaction>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8)))
		{
			String line = null;
			int tid = 0;
			boolean colonWarningFlag = false, countWarningFlag = false, parsingWarningFlag = false;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (!line.isEmpty() && '1' <= line.charAt(0) && line.charAt(0) <= '9')
				{
					Transaction transaction = new Transaction();
					final String[] parts = line.split(":");
					if (this.columnIndex < parts.length)
					{
						final String[] itemTokens = parts[0].trim().split("\\s+");
						final String[] utilityTokens = parts[this.columnIndex].trim().split("\\s+");
						if (itemTokens.length == utilityTokens.length)
							try
							{
								double transactionUtility = 0;
								for (int i = 0; i < itemTokens.length; ++i)
								{
									int item = Integer.parseInt(itemTokens[i]);
									double utility = Double.parseDouble(utilityTokens[i]);
									transactionUtility += utility;
									transaction.put(item, new ItemInfo(utility));
								}
								transaction.ttf = transactionUtility;
								transaction.tid = ++tid;
								this.transactions.add(transaction);
							}
							catch (Throwable e)
							{
								parsingWarningFlag = true;
							}
						else
							countWarningFlag = true;
					}
					else
						colonWarningFlag = true;
				}
			}
			this.cropTransactions(inputFilePath, startingTransactionID, maximumTransactionCount);
			if (colonWarningFlag)
				this.logger.print(
					"Algorithm" + this.algorithmName + ": One or more effective lines contain fewer than " + this.columnIndex + " colon(s), which have been skipped. ", LogLevel.Warning
				);
			if (countWarningFlag)
				this.logger.print(
					"Algorithm" + this.algorithmName + ": One or more effective lines contain inconsistent counts of items and utilities, which have been skipped. ", LogLevel.Warning
				);
			if (parsingWarningFlag)
				this.logger.print("Algorithm" + this.algorithmName + ": One or more effective lines contain failures in parsing numbers, which have been skipped. ", LogLevel.Warning);
			if (this.transactions.isEmpty())
				this.logger.print("Algorithm" + this.algorithmName + ": No effective transactions were loaded from " + Formatter.escapeString(inputFilePath) + ". ", LogLevel.Warning);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print("Failed to load the dataset " + Formatter.escapeString(inputFilePath) + " due to " + Formatter.escapeString(e) + ". ", LogLevel.Error);
			return false;
		}
	}
	private void computeTWTF()
	{
		for (Transaction transaction : this.transactions)
			for (Integer item : transaction.items.keySet())
				this.TWTF.put(item, TWTF.getOrDefault(item, 0.0) + transaction.ttf);
	}
	private void sortTWTF()
	{
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(TWTF.entrySet());
		list.sort(Map.Entry.comparingByValue());
		TWTF.clear();
		sequence = new int[list.size()];
		itemEvents = new ItemEvent[list.size()];
		int i = 0;
		for (Map.Entry<Integer, Double> e : list)
		{
			TWTF.put(e.getKey(), e.getValue());
			sequence[i] = e.getKey();
			itemEvents[i] = new ItemEvent(e.getKey());
			++i;
		}
	}
	private void computeRTF()
	{
		for (Transaction t : transactions)
			for (int i = 0; i < sequence.length; ++i)
				if (t.items.containsKey(sequence[i]))
				{
					for (int j = i + 1; j < sequence.length; j++)
						if (t.items.containsKey(sequence[j]))
							t.items.get(sequence[i]).rtf += t.items.get(sequence[j]).utility;
					itemEvents[i].transactions.put(t.tid, t.items.get(sequence[i]));
				}
	}
	private void computeETF()
	{
		for (Transaction t : transactions)
			for (Map.Entry<Integer, ItemInfo> e : t.items.entrySet())
				ETF.put(e.getKey(), ETF.getOrDefault(e.getKey(), 0.0) + e.getValue().utility);
	}
	private void sortETF()
	{
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(ETF.entrySet());
		list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
		ETF.clear();
		for (Map.Entry<Integer, Double> e : list)
			ETF.put(e.getKey(), e.getValue());
		if (strategy_ETF)
		{
			if (!list.isEmpty())
			{
				int idx = Math.min(this.k, list.size()) - 1;
				double tmp = list.get(idx).getValue();
				if (tmp > delta)
					delta = tmp;
			}
			this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``sortETF`` procedure. ", LogLevel.Trace);
		}
	}
	private void pruneItem()
	{
		LinkedHashMap<Integer, Double> newTWTF = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> e : TWTF.entrySet())
		{
			if (e.getValue() >= delta)
				newTWTF.put(e.getKey(), e.getValue());
			else
				for (Transaction transaction : transactions)
					transaction.items.remove(e.getKey());
		}
		TWTF = newTWTF;
	}
	private void sortTTFE()
	{
		for (int i = 0; i < this.transactions.size(); ++i)
		{
			final Transaction oldTransaction = transactions.get(i);
			Transaction newTransaction = new Transaction();
			newTransaction.tid = oldTransaction.tid;
			for (Map.Entry<Integer, Double> e : TWTF.entrySet())
				if (oldTransaction.items.containsKey(e.getKey()))
					newTransaction.put(e.getKey(), oldTransaction.items.get(e.getKey()));
			this.transactions.set(i, newTransaction);
		}
	}
	private void generateTable()
	{
		if (TWTF.isEmpty())
			return;
		int size = TWTF.size();
		int[] columns = new int[size - 1];
		int[] index = new int[size - 1];
		double[][] values = new double[size - 1][size - 1];
		{
			int cnt = 0;
			for (Map.Entry<Integer, Double> e : TWTF.entrySet())
			{
				if (0 == cnt)
					index[cnt] = e.getKey();
				else if (cnt == size - 1)
					columns[cnt - 1] = e.getKey();
				else
				{
					index[cnt] = e.getKey();
					columns[cnt - 1] = e.getKey();
				}
				++cnt;
			}
		}
		LETF = new Table(values, index, columns, "LETF");
		for (Transaction t : transactions)
		{
			ArrayList<Integer> itemSeq = new ArrayList<>(t.items.keySet());
			final int lastSequenceIndex = sequence.length - 1;
			for (int i = 0; i < lastSequenceIndex; ++i)
			{
				int p = sequence[i];
				if (!t.items.containsKey(p)) continue;
				ArrayList<Integer> subSeq = new ArrayList<>();
				subSeq.add(p);
				double sum = t.items.get(p).utility;
				for (int j = i + 1; j < sequence.length; ++j)
				{
					int q = sequence[j];
					if (!t.items.containsKey(q))
						break;
					subSeq.add(q);
					sum += t.items.get(q).utility;
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
		if (LETF != null && LETF.values.length >= 1)
		{
			final int n = LETF.values.length;
			letf_e.offer(LETF.values[n - 1][n - 1]);
			for (int j = n - 2; j >= 0; --j)
			{
				for (int i = j; i >= 0 && letf_e.size() < this.k; --i)
				{
					if (LETF.values[i][j + 1] > LETF.values[j][j])
						letf_e.offer(LETF.values[i][j + 1]);
					else
					{
						letf_e.offer(LETF.values[j][j]);
						break;
					}
				}
				if (letf_e.size() >= this.k) break;
			}
			if (letf_e.size() >= this.k && letf_e.peek() > delta)
				delta = letf_e.peek();
		}
		this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``raiseThreshold_LETF_E`` procedure. ", LogLevel.Trace);
	}
	private void raiseThreshold_LETF_LB()
	{
		if (LETF != null && LETF.values.length >= 1)
		{
			for (int i = 0; i < LETF.values.length; ++i)
			{
				for (int j = 0; j < LETF.values[i].length; ++j)
				{
					int p = LETF.index[i], q = LETF.columns[j];
					ArrayList<Integer> mids = LETF.getMiddleElements(p, q, false);
					double tmp = LETF.values[i][j];
					for (int m = 0; m < 3 && m < mids.size(); ++m)
					{
						tmp -= ETF.get(mids.get(m));
						if (tmp > delta)
						{
							letf_lb.offer(tmp);
							while (letf_lb.size() > this.k) letf_lb.poll();
						}
						else
							break;
					}
				}
			}
			if (letf_lb.size() >= this.k && letf_lb.peek() > delta)
				delta = letf_lb.peek();
		}
		this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``raiseThreshold_LETF_LB`` procedure. ", LogLevel.Trace);
	}
	private void mineWithUtilityLists()
	{
		LinkedHashMap<Integer, UList> mapItemToUList = new LinkedHashMap<>();
		for (int item : sequence)
		{
			if (TWTF.containsKey(item) && TWTF.get(item) >= delta)
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
				if (trans.items.containsKey(item))
				{
					itemsInTrans.add(item);
				}
			}
			if (itemsInTrans.isEmpty()) continue;
			double remaining = 0;
			for (int i = itemsInTrans.size() - 1; i >= 0; --i)
			{
				int item = itemsInTrans.get(i);
				double utility = trans.items.get(item).utility;
				UList ul = mapItemToUList.get(item);
				if (ul != null)
					ul.addElement(new UElement(trans.tid, utility, remaining));
				remaining += utility;
			}
		}
		ArrayList<UList> listOfULists = new ArrayList<>();
		for (int item : sequence)
		{
			UList ul = mapItemToUList.get(item);
			if (ul != null && ul.elements.size() > 0)
				listOfULists.add(ul);
		}
		int[] prefix = new int[InitialBufferSize];
		thui(prefix, 0, null, listOfULists);
		mapItemToUList.clear();
	}
	@Override
	public Number[] runAlgorithm(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		if (this.loadDataset(inputFilePath, startingTransactionID, maximumTransactionCount) && this.checkMemory())
			try
			{
				final long startTime = System.nanoTime();
				this.computeTWTF(); this.checkMemory();
				this.sortTWTF(); this.checkMemory();
				this.computeRTF(); this.checkMemory();
				this.computeETF(); this.checkMemory();
				this.sortETF(); this.checkMemory();
				this.pruneItem(); this.checkMemory();
				this.sortTTFE(); this.checkMemory();
				this.generateTable(); this.checkMemory();
				if (strategy_LETF_E) { this.raiseThreshold_LETF_E(); this.checkMemory(); }
				if (strategy_LETF_LB) { this.raiseThreshold_LETF_LB(); this.checkMemory(); }
				this.mineWithUtilityLists(); this.checkMemory();
				final long endTime = System.nanoTime();
				return new Number[] { endTime - startTime, this.peakMemory, this.delta };
			}
			catch (Throwable e)
			{
				this.logger.print("Algorithm" + this.algorithmName + ": Failed to execute the " + this.algorithmName + " algorithm due to " + Formatter.escapeString(e) + ". ", LogLevel.Error);
			}
		return new Number[] { null, null, null };
	}
	@Override
	public ArrayList<ArrayList<Integer>> getTopKPatterns()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<ArrayList<Integer>>();
		else
		{
			ArrayList<HTFE> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.eetf, a.eetf));
			ArrayList<ArrayList<Integer>> patterns = new ArrayList<>();
			for (HTFE result : results)
				patterns.add(new ArrayList<>(result.sequence));
			return patterns;
		}
	}
	@Override
	public ArrayList<Double> getTopKValues()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<Double>();
		else
		{
			ArrayList<HTFE> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.eetf, a.eetf));
			ArrayList<Double> values = new ArrayList<>();
			for (HTFE result : results)
				values.add(result.eetf);
			return values;
		}
	}
}

class AlgorithmTHUFI extends Algorithm<AlgorithmTHUFI.Transaction>
{
	private static class PatternTHUFI implements Comparable<PatternTHUFI>, Serializable
	{
		ArrayList<Integer> items = null;
		double combinedUtility = 0;
		
		PatternTHUFI(ArrayList<Integer> items, double combinedUtility)
		{
			this.items = items;
			this.combinedUtility = combinedUtility;
		}
		@Override
		public int compareTo(PatternTHUFI o)
		{
			return Double.compare(this.combinedUtility, o.combinedUtility);
		}
	}
	
	private PriorityQueue<PatternTHUFI> finalResults = new PriorityQueue<>();
	
	public AlgorithmTHUFI(final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		super("THUFI", _alpha, _beta, _delta, _k, _logger);
	}
	@Override
	public Number[] runAlgorithm(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		final long startTime = System.nanoTime();
		
		/* Utility */
		final AlgorithmTHUI utilityAlgorithm = new AlgorithmTHUI(this.alpha, this.beta, this.delta, this.k, this.logger);
		utilityAlgorithm.setColumnIndex(1);
		final Number[] utilityMetrics = utilityAlgorithm.runAlgorithm(inputFilePath, startingTransactionID, maximumTransactionCount);
		final ArrayList<ArrayList<Integer>> utilityTopKPatterns = utilityAlgorithm.getTopKPatterns();
		final ArrayList<Double> utilityTopKValues = utilityAlgorithm.getTopKValues();
		
		/* Frequency */
		final AlgorithmTHUI frequencyAlgorithm = new AlgorithmTHUI(this.alpha, this.beta, this.delta, this.k, this.logger);
		frequencyAlgorithm.setColumnIndex(2);
		final Number[] frequencyMetrics = frequencyAlgorithm.runAlgorithm(inputFilePath, startingTransactionID, maximumTransactionCount);
		final ArrayList<ArrayList<Integer>> frequencyTopKPatterns = frequencyAlgorithm.getTopKPatterns();
		final ArrayList<Double> frequencyTopKValues = frequencyAlgorithm.getTopKValues();
		
		/* Merge */
		final LinkedHashMap<ArrayList<Integer>, double[]> combinedMap = new LinkedHashMap<>();
		for (int i = 0; i < utilityTopKPatterns.size(); ++i)
		{
			ArrayList<Integer> items = utilityTopKPatterns.get(i);
			double utility = utilityTopKValues.get(i);
			double[] vals = combinedMap.get(items);
			if (null == vals)
				combinedMap.put(items, new double[] { utility, 0.0 });
			else
				vals[0] = utility;
		}
		for (int i = 0; i < frequencyTopKPatterns.size(); ++i)
		{
			ArrayList<Integer> items = frequencyTopKPatterns.get(i);
			double frequency = frequencyTopKValues.get(i);
			double[] vals = combinedMap.get(items);
			if (null == vals)
				combinedMap.put(items, new double[] { 0.0, frequency });
			else
				vals[1] = frequency;
		}
		for (Map.Entry<ArrayList<Integer>, double[]> entry : combinedMap.entrySet())
		{
			double u = entry.getValue()[0];
			double f = entry.getValue()[1];
			double combined = this.alpha * u + this.beta * f;
			finalResults.offer(new PatternTHUFI(entry.getKey(), combined));
			while (finalResults.size() > this.k)
				finalResults.poll();
		}
		
		final long endTime = System.nanoTime();
		
		/* Metrics */
		Long peakMemory = null;
		if (utilityMetrics[1] != null && frequencyMetrics[1] != null)
			peakMemory = (Long)utilityMetrics[1] + (Long)frequencyMetrics[1];
		else if (utilityMetrics[1] != null)
			peakMemory = (Long)utilityMetrics[1];
		else if (frequencyMetrics[1] != null)
			peakMemory = (Long)frequencyMetrics[1];
		if (utilityMetrics[2] != null && (double)utilityMetrics[2] != Double.NEGATIVE_INFINITY && frequencyMetrics[2] != null && (double)frequencyMetrics[2] != Double.NEGATIVE_INFINITY)
			this.delta = Math.min((Double)utilityMetrics[2], (Double)frequencyMetrics[2]);
		else if (utilityMetrics[2] != null && (double)utilityMetrics[2] != Double.NEGATIVE_INFINITY)
			this.delta = (Double)utilityMetrics[2];
		else if (frequencyMetrics[2] != null && (double)frequencyMetrics[2] != Double.NEGATIVE_INFINITY)
			this.delta = (Double)frequencyMetrics[2];
		
		return new Number[] { endTime - startTime, peakMemory, this.delta };
	}
	@Override
	public ArrayList<ArrayList<Integer>> getTopKPatterns()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<ArrayList<Integer>>();
		else
		{
			ArrayList<PatternTHUFI> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.combinedUtility, a.combinedUtility));
			ArrayList<ArrayList<Integer>> patterns = new ArrayList<>();
			for (PatternTHUFI result : results)
				patterns.add(new ArrayList<>(result.items));
			return patterns;
		}
	}
	@Override
	public ArrayList<Double> getTopKValues()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<Double>();
		else
		{
			ArrayList<PatternTHUFI> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.combinedUtility, a.combinedUtility));
			ArrayList<Double> values = new ArrayList<>();
			for (PatternTHUFI result : results)
				values.add(result.combinedUtility);
			return values;
		}
	}
}

class AlgorithmTTFE extends Algorithm<AlgorithmTTFE.Transaction>
{
	class TF implements Serializable
	{
		double threat = 0.0, frequency = 0.0, tf = 0.0, rtf = 0.0;
		
		TF(double threat, double frequency)
		{
			this.threat = threat;
			this.frequency = frequency;
			this.tf = alpha * threat + beta * frequency;
		}
	}
	static class Transaction extends Algorithm.Transaction
	{
		LinkedHashMap<Integer, TF> events = new LinkedHashMap<>();
		
		boolean put(Integer item, TF tf)
		{
			if (this.events.containsKey(item))
				return false;
			else
			{
				events.put(item, tf);
				return true;
			}
		}
		@Override
		int size()
		{
			return events.size();
		}
		@Override
		LinkedHashSet<Integer> keySet()
		{
			return new LinkedHashSet<Integer>(events.keySet());
		}
		boolean contains(Integer item)
		{
			return this.events.containsKey(item);
		}
		boolean remove(Integer item)
		{
			if (events.containsKey(item))
			{
				events.remove(item);
				return true;
			}
			else
				return false;
		}
		boolean isSequence(ArrayList<Integer> seq)
		{
			int idx = index(seq.get(0));
			if (idx == -1) return false;
			for (int i = 1; i < seq.size(); ++i)
				if (++idx != index(seq.get(i)))
					return false;
			return true;
		}
		int index(int event)
		{
			int idx = -1;
			for (Map.Entry<Integer, TF> e : events.entrySet())
			{
				++idx;
				if (e.getKey() == event)
					return idx;
			}
			return -1;
		}
	}
	private static class Event implements Serializable
	{
		int event = 0;
		LinkedHashMap<Integer, TF> transactions = new LinkedHashMap<>();
		
		Event(int event)
		{
			this.event = event;
		}
	}
	private static class Table implements Serializable
	{
		String name;
		int[] index, columns, sequence;
		double[][] values;

		Table(double[][] values, int[] index, int[] columns, String name)
		{
			this.values = values; this.index = index; this.columns = columns; this.name = name;
			this.sequence = new int[index.length + 1];
			this.sequence[0] = index[0];
			for (int i = 0; i < columns.length; ++i) this.sequence[i+1] = columns[i];
		}
		boolean addValueByName(int indexName, int columnName, double value)
		{
			int ci = -1, ii = -1;
			for (int i = 0; i < columns.length; ++i) if (columns[i] == columnName) { ci = i; break; }
			for (int i = 0; i < index.length; ++i) if (index[i] == indexName) { ii = i; break; }
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
				if (elem == p)
					add = true;
				else if (elem == q)
				{
					if (inclusive)
						arr.add(q);
					break;
				}
				else if (add)
					arr.add(elem);
			}
			if (inclusive) arr.add(0, p);
			return arr;
		}
	}
	private static class UElement implements Serializable
	{
		int tid = 0;
		double iutils = 0.0;
		double rutils = 0.0;
		
		UElement(int tid, double iutils, double rutils)
		{
			this.tid = tid;
			this.iutils = iutils;
			this.rutils = rutils;
		}
	}
	static class UList implements Serializable
	{
		Integer item = null;
		double sumIutils = 0;
		double sumRutils = 0;
		ArrayList<UElement> elements = new ArrayList<>();
		
		UList(Integer item)
		{
			this.item = item;
		}
		void addElement(UElement e)
		{
			sumIutils += e.iutils;
			sumRutils += e.rutils;
			elements.add(e);
		}
	}
	static class HTFE implements Comparable<HTFE>, Serializable
	{
		ArrayList<Integer> sequence;
		double eetf;
		
		HTFE(ArrayList<Integer> seq, double eetf)
		{
			this.sequence = new ArrayList<>(seq);
			this.eetf = eetf;
		}
		@Override
		public int compareTo(HTFE o)
		{
			return Double.compare(this.eetf, o.eetf);
		}
	}
	
	private LinkedHashMap<Integer, Double> TWTF = new LinkedHashMap<>();
	private int[] sequence = null;
	private Event[] events = null;
	private LinkedHashMap<Integer, Double> ETF = new LinkedHashMap<>();
	private Table LETF = null;
	private final PriorityQueue<Double> letf_e = new PriorityQueue<>();
	private final PriorityQueue<Double> letf_lb = new PriorityQueue<>();
	final PriorityQueue<HTFE> finalResults = new PriorityQueue<>();
	private int candidateCount = 0;
	
	public AlgorithmTTFE(final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		super("TTFE", _alpha, _beta, _delta, _k, _logger);
	}
	AlgorithmTTFE(final String _algorithmName, final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		super(_algorithmName, _alpha, _beta, _delta, _k, _logger);
	}
	
	/* Child procedures */
	private void savePattern(int[] prefix, final int length, UList X)
	{
		ArrayList<Integer> seq = new ArrayList<>();
		for (int i = 0; i < length; ++i)
			seq.add(prefix[i]);
		seq.add(X.item);
		HTFE htfe = new HTFE(seq, X.sumIutils);
		this.finalResults.offer(htfe);
		while (this.finalResults.size() > this.k)
			this.finalResults.poll();
		if (this.strategyPruning)
		{
			if (this.finalResults.size() >= this.k)
				this.delta = this.finalResults.peek().eetf;
			this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the final mining procedure. ", LogLevel.Trace);
		}
	}
	private void thui(int[] prefix, int prefixLength, UList pUL, ArrayList<UList> ULs)
	{
		try
		{
			this.localMemory += getObjectSize(prefix) + getObjectSize(pUL) + getObjectSize(ULs);
		}
		catch (Throwable e) {}
		for (int i = ULs.size() - 1; i >= 0; --i)
		{
			UList X = ULs.get(i);
			if (X.sumIutils >= (Double.NEGATIVE_INFINITY == this.delta ? 0 : delta)) // do not prune if ``delta`` is ``Double.NEGATIVE_INFINITY``
				savePattern(prefix, prefixLength, X);
		}
		for (int i = ULs.size() - 2; i >= 0; --i)
		{
			UList X = ULs.get(i);
			if (X.sumIutils + X.sumRutils >= (Double.NEGATIVE_INFINITY == this.delta ? 0 : delta)) // do not prune if ``delta`` is ``Double.NEGATIVE_INFINITY``
			{
				ArrayList<UList> exULs = new ArrayList<>();
				for (int j = i + 1; j < ULs.size(); ++j)
				{
					UList Y = ULs.get(j);
					++candidateCount;
					UList ex = construct(pUL, X, Y);
					if (ex != null)
						exULs.add(ex);
				}
				if (prefixLength >= prefix.length)
				{
					final int newSize = prefix.length << 1;
					int[] newPrefix = new int[newSize];
					System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
					prefix = newPrefix;
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
			UElement ex = px.elements.get(ei), ey = py.elements.get(ej);
			if (ex.tid > ey.tid)
			{
				++ej;
				continue;
			}
			if (ex.tid < ey.tid)
			{
				totUtil -= ex.iutils + ex.rutils;
				if (totUtil < delta)
					return null;
				++ei;
				if (P != null)
					++Pi;
				continue;
			}
			if (null == P)
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils, ey.rutils));
			else
			{
				while (Pi < P.elements.size() && P.elements.get(++Pi).tid < ex.tid) ;
				UElement e = P.elements.get(Pi);
				pxyUL.addElement(new UElement(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rutils));
			}
			++ei;
			++ej;
		}
		while (ei < px.elements.size())
		{
			UElement ex = px.elements.get(ei);
			totUtil -= ex.iutils + ex.rutils;
			if (totUtil < delta)
				return null;
			++ei;
		}
		return pxyUL;
	}
	
	/* Main procedures */
	boolean loadDataset(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		this.transactions = new ArrayList<Transaction>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8)))
		{
			String line = null;
			int tid = 0;
			boolean colonWarningFlag = false, countWarningFlag = false, parsingWarningFlag = false;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (!line.isEmpty() && '1' <= line.charAt(0) && line.charAt(0) <= '9')
				{
					Transaction transaction = new Transaction();
					final String[] parts = line.split(":");
					if (parts.length >= 3)
					{
						final String[] events = parts[0].trim().split("\\s+");
						final String[] threats = parts[1].trim().split("\\s+");
						final String[] frequencies = parts[2].trim().split("\\s+");
						if (events.length == threats.length && events.length == frequencies.length)
							try
							{
								double ttf = 0;
								for (int i = 0; i < events.length; ++i)
								{
									int event = Integer.parseInt(events[i]);
									double threat = Double.parseDouble(threats[i]);
									ttf += this.alpha * threat;
									double frequency = Double.parseDouble(frequencies[i]);
									ttf += this.beta * frequency;
									transaction.put(event, new TF(threat, frequency));
								}
								transaction.ttf = ttf;
								transaction.tid = ++tid;
								this.transactions.add(transaction);
							}
							catch (Throwable e)
							{
								parsingWarningFlag = true;
							}
						else
							countWarningFlag = true;
					}
					else
						colonWarningFlag = true;
				}
			}
			this.cropTransactions(inputFilePath, startingTransactionID, maximumTransactionCount);
			if (colonWarningFlag)
				this.logger.print(
					"Algorithm" + this.algorithmName + ": One or more effective lines contain fewer than 3 colons, which have been skipped. ", LogLevel.Warning
				);
			if (countWarningFlag)
				this.logger.print(
					"Algorithm" + this.algorithmName + ": One or more effective lines contain inconsistent counts of items and utilities, which have been skipped. ", LogLevel.Warning
				);
			if (parsingWarningFlag)
				this.logger.print("Algorithm" + this.algorithmName + ": One or more effective lines contain failures in parsing numbers, which have been skipped. ", LogLevel.Warning);
			if (this.transactions.isEmpty())
				this.logger.print("Algorithm" + this.algorithmName + ": No effective transactions were loaded from " + Formatter.escapeString(inputFilePath) + ". ", LogLevel.Warning);
			return true;			
		}
		catch (Throwable e)
		{
			this.logger.print("Failed to load the dataset " + Formatter.escapeString(inputFilePath) + " due to " + Formatter.escapeString(e) + ". ", LogLevel.Error);
			return false;
		}
	}
	private void computeTWTF()
	{
		for (Transaction transaction : this.transactions)
			for (Integer event : transaction.events.keySet())
				this.TWTF.put(event, TWTF.getOrDefault(event, 0.0) + transaction.ttf);
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
			++i;
		}
	}
	private void computeRTF()
	{
		for (Transaction t : transactions)
			for (int i = 0; i < sequence.length; ++i)
				if (t.events.containsKey(sequence[i]))
				{
					for (int j = i + 1; j < sequence.length; ++j)
						if (t.events.containsKey(sequence[j]))
							t.events.get(sequence[i]).rtf += t.events.get(sequence[j]).tf;
					events[i].transactions.put(t.tid, t.events.get(sequence[i]));
				}
	}
	private void computeETF()
	{
		for (Transaction t : transactions)
			for (Map.Entry<Integer, TF> e : t.events.entrySet())
				ETF.put(e.getKey(), ETF.getOrDefault(e.getKey(), 0.0) + e.getValue().tf);
	}
	private void sortETF()
	{
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(ETF.entrySet());
		list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
		ETF.clear();
		for (Map.Entry<Integer, Double> e : list)
			ETF.put(e.getKey(), e.getValue());
		if (strategy_ETF)
		{
			if (!list.isEmpty())
			{
				int idx = Math.min(this.k, list.size()) - 1;
				double tmp = list.get(idx).getValue();
				if (tmp > this.delta)
					this.delta = tmp;
			}
			this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``sortETF`` procedure. ", LogLevel.Trace);
		}
	}
	private void pruneItem()
	{
		LinkedHashMap<Integer, Double> newTWTF = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> e : TWTF.entrySet())
		{
			if (e.getValue() >= delta)
				newTWTF.put(e.getKey(), e.getValue());
			else
				for (Transaction transaction : transactions)
					transaction.events.remove(e.getKey());
		}
		TWTF = newTWTF;
	}
	private void sortTTFE()
	{
		for (int i = 0; i < this.transactions.size(); ++i)
		{
			final Transaction oldTransaction = this.transactions.get(i);
			Transaction newTransaction = new Transaction();
			newTransaction.tid = oldTransaction.tid;
			for (Map.Entry<Integer, Double> e : TWTF.entrySet())
				if (oldTransaction.events.containsKey(e.getKey()))
					newTransaction.put(e.getKey(), oldTransaction.events.get(e.getKey()));
			this.transactions.set(i, newTransaction);
		}
	}
	private void generateTable()
	{
		if (TWTF.isEmpty())
			return;
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
			for (int i = 0; i < sequence.length - 1; ++i)
			{
				int p = sequence[i];
				if (!t.events.containsKey(p)) continue;
				ArrayList<Integer> subSeq = new ArrayList<>();
				subSeq.add(p);
				double sum = t.events.get(p).tf;
				for (int j = i + 1; j < sequence.length; ++j)
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
		if (LETF != null && LETF.values.length >= 1)
		{
			final int n = LETF.values.length;
			letf_e.offer(LETF.values[n - 1][n - 1]);
			for (int j = n - 2; j >= 0; --j)
			{
				for (int i = j; i >= 0 && letf_e.size() < this.k; --i)
				{
					if (LETF.values[i][j + 1] > LETF.values[j][j])
						letf_e.offer(LETF.values[i][j + 1]);
					else
					{
						letf_e.offer(LETF.values[j][j]);
						break;
					}
				}
				if (letf_e.size() >= this.k) break;
			}
			if (letf_e.size() >= this.k && letf_e.peek() > delta)
				delta = letf_e.peek();
		}
		this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``raiseThreshold_LETF_E`` procedure. ", LogLevel.Trace);
	}
	private void raiseThreshold_LETF_LB()
	{
		if (LETF != null && LETF.values.length >= 1)
		{
			for (int i = 0; i < LETF.values.length; ++i)
				for (int j = 0; j < LETF.values[i].length; ++j)
				{
					int p = LETF.index[i], q = LETF.columns[j];
					ArrayList<Integer> mids = LETF.getMiddleElements(p, q, false);
					double tmp = LETF.values[i][j];
					for (int m = 0; m < 3 && m < mids.size(); ++m)
					{
						tmp -= ETF.get(mids.get(m));
						if (tmp > delta)
						{
							letf_lb.offer(tmp);
							while (letf_lb.size() > this.k) letf_lb.poll();
						}
						else
							break;
					}
				}
			if (letf_lb.size() >= this.k && letf_lb.peek() > delta)
				delta = letf_lb.peek();
		}
		this.logger.print("Algorithm" + this.algorithmName + ": The $\\delta$ has been raised to " + this.delta + " in the ``raiseThreshold_LETF_LB`` procedure. ", LogLevel.Trace);
	}
	private void mineWithEnumerationTree()
	{
		LinkedHashMap<Integer, UList> mapItemToUList = new LinkedHashMap<>();
		for (int item : sequence)
			if (TWTF.containsKey(item) && TWTF.get(item) >= delta)
			{
				UList ul = new UList(item);
				mapItemToUList.put(item, ul);
			}
		for (Transaction trans : transactions)
		{
			ArrayList<Integer> itemsInTrans = new ArrayList<>();
			for (int item : sequence)
				if (trans.events.containsKey(item))
					itemsInTrans.add(item);
			if (itemsInTrans.isEmpty())
				continue;
			double remaining = 0;
			for (int i = itemsInTrans.size() - 1; i >= 0; --i)
			{
				int item = itemsInTrans.get(i);
				double ttf = trans.events.get(item).tf;
				UList ul = mapItemToUList.get(item);
				if (ul != null)
					ul.addElement(new UElement(trans.tid, ttf, remaining));
				remaining += ttf;
			}
		}
		ArrayList<UList> listOfULists = new ArrayList<>();
		for (int item : sequence)
		{
			UList ul = mapItemToUList.get(item);
			if (ul != null && ul.elements.size() > 0)
				listOfULists.add(ul);
		}
		int[] prefix = new int[InitialBufferSize];
		thui(prefix, 0, null, listOfULists);
		mapItemToUList.clear();
	}
	@Override
	public Number[] runAlgorithm(final String inputFilePath, final Number startingTransactionID, final Number maximumTransactionCount)
	{
		if (this.loadDataset(inputFilePath, startingTransactionID, maximumTransactionCount) && this.checkMemory())
			try
			{
				final long startTime = System.nanoTime();
				this.computeTWTF(); this.checkMemory();
				this.sortTWTF(); this.checkMemory();
				this.computeRTF(); this.checkMemory();
				this.computeETF(); this.checkMemory();
				this.sortETF(); this.checkMemory();
				this.pruneItem(); this.checkMemory();
				this.sortTTFE(); this.checkMemory();
				this.generateTable(); this.checkMemory();
				if (strategy_LETF_E) { this.raiseThreshold_LETF_E(); this.checkMemory(); }
				if (strategy_LETF_LB) { this.raiseThreshold_LETF_LB(); this.checkMemory(); }
				this.mineWithEnumerationTree(); this.checkMemory();
				final long endTime = System.nanoTime();
				return new Number[] { endTime - startTime, this.peakMemory, this.delta };
			}
			catch (Throwable e)
			{
				this.logger.print("Algorithm" + this.algorithmName + ": Failed to execute the " + this.algorithmName + " algorithm due to " + Formatter.escapeString(e) + ". ", LogLevel.Error);
			}
		return new Number[] { null, null, null };
	}
	@Override
	public ArrayList<ArrayList<Integer>> getTopKPatterns()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<ArrayList<Integer>>();
		else
		{
			ArrayList<HTFE> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.eetf, a.eetf));
			ArrayList<ArrayList<Integer>> patterns = new ArrayList<>();
			for (final HTFE result : results)
				patterns.add(new ArrayList<>(result.sequence));
			return patterns;
		}
	}
	@Override
	public ArrayList<Double> getTopKValues()
	{
		if (null == this.finalResults || this.finalResults.isEmpty())
			return new ArrayList<Double>();
		else
		{
			ArrayList<HTFE> results = new ArrayList<>(this.finalResults);
			results.sort((a, b) -> Double.compare(b.eetf, a.eetf));
			ArrayList<Double> values = new ArrayList<>();
			for (final HTFE result : results)
				values.add(result.eetf);
			return values;
		}
	}
}

class AlgorithmGUMM extends AlgorithmTTFE
{
	public AlgorithmGUMM(final double _alpha, final double _beta, final double _delta, final int _k, final Logger _logger)
	{
		super("GUMM", _alpha, _beta, _delta, _k, _logger);
		this.strategy_ETF = false;
		this.strategy_LETF_E = false;
		this.strategy_LETF_LB = false;
		this.strategyPruning = false;
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
			this.logger.print("Saver: The results will be displayed on the console. ", LogLevel.Debug);
		else
		{
			this.escapedOutputFilePath = Formatter.escapeString(this.outputFilePath);
			this.logger.print("Saver: The saver has been initialized. The results will be saved to " + this.escapedOutputFilePath + ". ", LogLevel.Debug);
		}
		if (null == this.columns)
			this.columns = new String[] {};
		if (this.columns.length < 1)
			this.logger.print("Saver: The columns are empty. ", LogLevel.Warning);
	}
	private final boolean displayOnConsole(Object[][] results, final int leftClosing, final int rightOpening)
	{
		System.out.println(Formatter.array2String(this.columns, "", "\t", "", column -> Formatter.filterString(column)));
		final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
		for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
			System.out.println(Formatter.array2String(results[rIndex], "", "\t", "", r -> Formatter.filterString(r)));
		System.out.println();
		return true;
	}
	static boolean handleDirectory(final Path directory)
	{
		try
		{
			if (Files.exists(directory))
				return Files.isDirectory(directory);
			else
				Files.createDirectories(directory);
				return Files.isDirectory(directory);
		}
		catch (Throwable e) {}
		return false;
	}
	private boolean saveToCSV(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write(Formatter.array2String(this.columns, "", ",", LINE_SEPARATOR, column -> Formatter.escapeCSV(Formatter.filterString(column))));
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write(Formatter.array2String(results[rIndex], "", ",", LINE_SEPARATOR, r -> Formatter.escapeCSV(Formatter.filterString(r))));
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + " in the CSV format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the CSV format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
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
			writer.write(Formatter.array2String(this.columns, "", "", "", column -> "<th>" + Formatter.escapeHTML(Formatter.filterString(column)) + "</th>"));
			writer.write("\t\t\t\t</tr>" + LINE_SEPARATOR);
			writer.write("\t\t\t</thead>" + LINE_SEPARATOR);
			writer.write("\t\t\t<tbody>" + LINE_SEPARATOR);
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
			{
				writer.write("\t\t\t\t<tr>" + LINE_SEPARATOR);
				writer.write(Formatter.array2String(
					results[rIndex], "", "", "", r -> "\t\t\t\t\t<td>" + Formatter.escapeHTML(Formatter.filterString(r)) + "</td>" + LINE_SEPARATOR
				));
				writer.write("\t\t\t\t</tr>" + LINE_SEPARATOR);
			}
			writer.write("\t\t\t</tbody>" + LINE_SEPARATOR);
			writer.write("\t\t</table>" + LINE_SEPARATOR);
			writer.write("\t</body>" + LINE_SEPARATOR);
			writer.write("</html>");
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + "in the HTM(L) format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the HTM(L) format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
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
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + "in the JSON format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the JSON format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
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
					writer.write("\t\t\t" + Formatter.escapeXMLContent(Formatter.filterString(r)) + LINE_SEPARATOR);
					writer.write("\t\t</" + tag + ">" + LINE_SEPARATOR);
				}
				writer.write("\t</result>" + LINE_SEPARATOR);
			}
			writer.write("</results>");
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + "in the XML format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the XML format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToTEX(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write("\\documentclass[a4paper]{article}" + LINE_SEPARATOR);
			writer.write("\\usepackage{booktabs}" + LINE_SEPARATOR);
			writer.write("\\usepackage{rotating}" + LINE_SEPARATOR + LINE_SEPARATOR);
			writer.write("\\begin{document}" + LINE_SEPARATOR + LINE_SEPARATOR);
			writer.write("\\begin{sidewaystable}" + LINE_SEPARATOR);
			writer.write("\t\\caption{The comparison results. }" + LINE_SEPARATOR);
			writer.write("\t\\centering" + LINE_SEPARATOR);
			writer.write("\t\\begin{tabular}{" + "c".repeat(this.columns.length) + "}" + LINE_SEPARATOR);
			writer.write("\t\t\\toprule" + LINE_SEPARATOR);
			writer.write("\t\t" + Formatter.array2String(this.columns, "", " & ", "\\\\", column -> Formatter.filterString(column)) + LINE_SEPARATOR);
			writer.write("\t\t\\midrule" + LINE_SEPARATOR);
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write("\t\t" + Formatter.array2String(results[rIndex], "", " & ", "\\\\", r -> Formatter.filterString(r)) + LINE_SEPARATOR);
			writer.write("\t\t\\bottomrule" + LINE_SEPARATOR);
			writer.write("\t\\end{tabular}" + LINE_SEPARATOR);
			writer.write("\\end{sidewaystable}" + LINE_SEPARATOR + LINE_SEPARATOR);
			writer.write("\\end{document}");
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + "in the TEX format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the TEX format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
			this.displayOnConsole(results, leftClosing, rightOpening);
			return false;
		}
	}
	private boolean saveToTSV(final Object[][] results, final int leftClosing, final int rightOpening)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFilePath), StandardCharsets.UTF_8)))
		{
			writer.write(Formatter.array2String(this.columns, "", "\t", LINE_SEPARATOR, column -> Formatter.filterString(column)));
			final int realLeftClosing = Math.max(0, leftClosing), realRightOpening = Math.min(rightOpening, results.length);
			for (int rIndex = realLeftClosing; rIndex < realRightOpening; ++rIndex)
				writer.write(Formatter.array2String(results[rIndex], "", "\t", LINE_SEPARATOR, r -> Formatter.filterString(r)));
			this.logger.print("Saver: Successfully saved the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath + "in the TSV format. ", LogLevel.Info);
			return true;
		}
		catch (Throwable e)
		{
			this.logger.print(
				"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to "
				+ this.escapedOutputFilePath + " in the TSV format due to " + Formatter.escapeString(e) + ". ", 
				LogLevel.Error
			);
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
			final Path parentDirectory = Paths.get(this.outputFilePath).getParent();
			if (null == parentDirectory || handleDirectory(parentDirectory))
			{
				this.logger.print(
					"Saver: Successfully prepared the parent directory " + Formatter.escapeString(parentDirectory) + ". ", 
					LogLevel.Debug
				);
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
			else
			{
				this.logger.print(
					"Saver: Failed to save the results[" + leftClosing + ":" + rightOpening + "] to " + this.escapedOutputFilePath
					+ " due to failures of preparing its parent directory " + Formatter.escapeString(parentDirectory) + ". ", 
					LogLevel.Error
				);
				this.displayOnConsole(results, leftClosing, rightOpening);
				return false;
			}
		}
	}
}

public class TopKMining
{
	final static int EXIT_SUCCESS = 0;
	final static int EXIT_FAILURE = 1;
	final static int EOF = (-1);
	final static double EPSILON = 0.00001;
	
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
		if (parser.parseArguments(arguments))
			System.exit(EXIT_SUCCESS);
		else
		{
			final LinkedHashMap<LogLevel, String> messages = parser.getMessages();
			final Logger logger = new Logger(parser.getLogLevel());
			for (Map.Entry<LogLevel, String> entry : messages.entrySet())
			{
				final String message = entry.getValue();
				if (message instanceof String && !message.isEmpty())
					logger.print(entry.getValue(), entry.getKey());
			}
			final String inputFilePath = parser.getInputFilePath();
			if (inputFilePath instanceof String)
			{
				/* Parameters */
				Object[][] parameters = {
					{ "THUFI", 0.5, 0.5 }, { "TTFE", 0, 1 }, { "TTFE", 0.25, 0.75 }, { "TTFE", 0.5, 0.5 }, 
					{ "TTFE", 0.75, 0.25 }, { "TTFE", 1, 0 }, { "GUMM", 0.5, 0.5 }
				};
				final int[] kValues = { 5, 10, 50, 100, 500, 1000, 5000, 10000 };
				
				/* Algorithms */
				final String[] columns = {
					"Dataset", "Algorithm", "$\\alpha$", "$\\beta$", "$\\delta_0$", "$k$", "Run count", "Time consumption (ns)", "Memory consumption (B)", "$\\delta^*$"
				};
				final int length = columns.length, metricLength = 3;
				Object[][] results = new Object[parameters.length * kValues.length][length];
				int outerIndex = 0;
				final String datasetName = Formatter.filterMainFileName(inputFilePath);
				LinkedHashMap<String, Function<Integer, Double>> deltaFactory = new LinkedHashMap<String, Function<Integer, Double>>();
				deltaFactory.put("THUFI", (Function<Integer, Double>)x -> Double.NEGATIVE_INFINITY);
				deltaFactory.put("TTFE", (Function<Integer, Double>)x -> Double.NEGATIVE_INFINITY);
				deltaFactory.put("GUMM", (Function<Integer, Double>)x -> (
					results[x - 3 * kValues.length][length - 1] instanceof Double && ((Number)results[x - 3 * kValues.length][length - 1]).doubleValue() >= 0
					? ((Number)results[x - 3 * kValues.length][length - 1]).doubleValue() * parser.getDeltaRatio() : 0.0
				));
				final int runCount = parser.getRunCount();
				LinkedHashMap<String, Function<Number[], Algorithm>> algorithmFactory = new LinkedHashMap<String, Function<Number[], Algorithm>>();
				algorithmFactory.put("THUFI", numbers -> new AlgorithmTHUFI(numbers[0].doubleValue(), numbers[1].doubleValue(), numbers[2].doubleValue(), numbers[3].intValue(), logger));
				algorithmFactory.put("TTFE", numbers -> new AlgorithmTTFE(numbers[0].doubleValue(), numbers[1].doubleValue(), numbers[2].doubleValue(), numbers[3].intValue(), logger));
				algorithmFactory.put("GUMM", numbers -> new AlgorithmGUMM(numbers[0].doubleValue(), numbers[1].doubleValue(), numbers[2].doubleValue(), numbers[3].intValue(), logger));
				final Number startingTransactionID = parser.getStartingTransactionID(), maximumTransactionCount = parser.getMaximumTransactionCount();
				boolean flag = true;
				Saver saver = new Saver(parser.getOutputFilePath(), columns, logger);
				for (Object[] parameter : parameters)
					for (final int k : kValues)
					{
						int innerIndex = 0;
						results[outerIndex][innerIndex++] = datasetName;
						results[outerIndex][innerIndex++] = parameter[0];
						results[outerIndex][innerIndex++] = parameter[1];
						results[outerIndex][innerIndex++] = parameter[2];
						final double delta = deltaFactory.get(parameter[0]).apply(outerIndex);
						results[outerIndex][innerIndex++] = delta;
						results[outerIndex][innerIndex++] = k;
						results[outerIndex][innerIndex++] = runCount;
						final Number[] numbers = { ((Number)parameter[1]).doubleValue(), ((Number)parameter[2]).doubleValue(), delta, k };
						Algorithm algorithm = algorithmFactory.get(parameter[0]).apply(numbers);
						Number[] result = algorithm.runAlgorithm(inputFilePath, startingTransactionID, maximumTransactionCount);
						if (result != null && result.length == metricLength)
						{
							for (int run = 2; run < runCount; ++run)
							{
								algorithm = algorithmFactory.get(parameter[0]).apply(numbers);
								Number[] r = algorithm.runAlgorithm(inputFilePath, startingTransactionID, maximumTransactionCount);
								if (r != null && r.length == metricLength)
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
				System.exit(flag ? EXIT_SUCCESS : EXIT_FAILURE);
			}
			else
			{
				logger.print("The input file path to the dataset must be specified. ", LogLevel.Fatal);
				System.exit(EOF);
			}
		}
	}
}