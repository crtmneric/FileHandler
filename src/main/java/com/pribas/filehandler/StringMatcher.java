package com.pribas.filehandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.WeakHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.opencsv.CSVWriter;

public class StringMatcher {

	String word;
	private Scanner scn;

	int wordCount = 0;

	List<String[]> data;
	private static WeakHashMap<String, Pattern> patternMap = new WeakHashMap<String, Pattern>();
	CSVWriter writer;

	String sessionIDs;
	Map<String, String> mapping;
	String[] matchRegex;
	int matchNumber;
	boolean[] resultS;
	String[] grabData;

	private boolean isMatched(String patternString, String content) {
		PatternCompiler compiler = new Perl5Compiler();
		PatternMatcher matcher = new Perl5Matcher();
		Pattern pattern = null;
		pattern = (Pattern) patternMap.get(patternString);
		if (pattern == null) {
			try {
				pattern = compiler.compile(patternString, Perl5Compiler.SINGLELINE_MASK);
				patternMap.put(patternString, pattern);
			} catch (MalformedPatternException e) {
				System.out.println("Bad pattern: " + patternString + ".");
				return false;
			}
		}
		if (matcher.matches(content, pattern)) {
			return true;
		}
		return false;
	}

	public void regexWrite() throws IOException {
		try {
			File fileToReadable;
			scn = new Scanner(System.in);
			data = new ArrayList<String[]>();

			System.out.print("File path to read :");
			String fileToRead = scn.nextLine();

			fileToReadable = new File(fileToRead);
			LineIterator it = null;

			getInput(fileToReadable, fileToRead, it);

			String fileToWrite = fileCheckHeaderAdd();

			startToWrite(fileToReadable, fileToWrite);
		} catch (

		Exception ex) {
			System.out.print("Are u sure the path or file name is correct ?");

		}
	}

	private void startToWrite(File fileToReadable, String fileToWrite) {
		LineIterator it;
		System.out.println("Now Searching and Writing .. Please wait untill i say it's done!");
		long start = System.currentTimeMillis();

		try {
			it = FileUtils.lineIterator(fileToReadable, "UTF-8");
			while (it.hasNext()) {
				wordCount = grabRegex(wordCount, it);
			}
			writer.close();
			if (wordCount > 0) {
				inphoGiven(start);
			} else {
				System.out.println("Not a single line contains " + word + " and RegEx input :(");
				System.out
						.print("while documentation reading took : " + (System.currentTimeMillis() - start) + " ms ! ");

			}

		} catch (Exception ex) {
			System.out.println("Are u sure there is a file with " + fileToWrite + " named?");
			System.err.println(ex);

		}
	}

	private String fileCheckHeaderAdd() {
		File csvFile;
		String fileToWrite = regexInputs();
		try {
			csvFile = createFileToWrite(fileToWrite + ".csv");
			writer = new CSVWriter(new FileWriter(csvFile), ';');
		} catch (Exception ex) {
			System.out.print("Wrong file path...");

		}
		List<String> headers = getHeaders(matchRegex, "SessionID's");
		data.add(headers.toArray(new String[headers.size()]));
		writer.writeAll(data);
		data.clear();
		return fileToWrite;
	}

	private String regexInputs() {
		System.out.println("Match Regex Number:");
		matchNumber = scn.nextInt();
		matchRegex = new String[matchNumber];
		resultS = new boolean[matchNumber];
		grabData = new String[matchNumber];
		for (int i = 0; i < matchNumber; i++) {
			System.out.print("Match Regex Input:");
			matchRegex[i] = scn.next();
			if (StringUtils.isEmpty(matchRegex[i]) || matchRegex[i].length() == 1) {
				while (StringUtils.isEmpty(matchRegex[i])) {
					System.out.println("Empty input for RegExp please write somethink:");
					System.out.print("Match Regex Input:");
					matchRegex[i] = scn.next();
				}
			}
		}

		for (int j = 0; j < matchNumber; j++) {
			matchRegex[j] = ".*" + matchRegex[j] + ".*";
		}

		System.out.print("File path to write like :");
		String fileToWrite = scn.next();
		return fileToWrite;
	}

	private void getInput(File fileToReadable, String fileToRead, LineIterator it) {
		try {
			it = FileUtils.lineIterator(fileToReadable, "UTF-8");

		} catch (Exception ex) {
			while (!it.hasNext()) {
				System.out.print("There is no file with path:" + fileToRead + "!");
				System.out.print("File path to read :");
				fileToRead = scn.nextLine();
			}
		}
		System.out.print("Search Regex Input:");
		word = scn.nextLine();
		if (StringUtils.isEmpty(word)) {
			while (StringUtils.isEmpty(word) || word.length() == 1) {
				System.out.println("Empty input for keyword please write somethink.");
				System.out.print("Search Regex Input:");
				word = scn.nextLine();
			}
		}
		word = ".*" + word + ".*";
	}

	private List<String> getHeaders(String[] data, String firstElement) {
		List<String> result = new ArrayList<>();
		if (firstElement != null) {
			result.add(firstElement);
		}
		for (String string : data) {
			result.add(string);
		}
		return result;
	}

	private int grabRegex(int wordCount, LineIterator it) throws Exception {
		String line = it.nextLine();
		if (isMatched(word, line)) {
			String[] words = StringUtils.split(line, " ");
			for (int j = 0; j < matchNumber; j++) {
				resultS[j] = isMatched(matchRegex[j], line);
				if (resultS[j]) {
					for (String ss : words) {

						resultS[j] = isMatched(matchRegex[j], ss);
						if (resultS[j]) {
							grabData[j] = ss;
							wordCount++;
						}
						if (StringUtils.containsIgnoreCase(ss, "sessionid=")) {
							sessionIDs = ss.replace("[", "");
							sessionIDs = ss.replace("'", "");
							sessionIDs = ss.replace("]", "");
							sessionIDs = ss.replace("sessionid=", "");

						}

					}
					if (sessionIDs.length() > 3) {
						csvWriterS();
					}
				}
			}

			if (wordCount % 100 == 0 && wordCount != 0) {
				System.out.print(".");
			}
		}

		return wordCount;
	}

	private void csvWriterS() {
		List<String> BOK = getHeaders(grabData, sessionIDs);
		data.add(BOK.toArray(new String[BOK.size()]));
		writer.writeAll(data);
		data.clear();
	}

	private void inphoGiven(long start) {
		System.out.println();
		System.out.println("while documentation writing took : " + (System.currentTimeMillis() - start) + " ms ! ");
		System.out.println("Whole documentation contains the keyword:" + word + ", and the Sessionid Filter "
				+ wordCount + " Times!");
		System.out.println("It's done! :)");
	}

	private File createFileToWrite(String fileToWrite) throws IOException {
		File file;
		file = new File(fileToWrite);
		file.createNewFile();
		return file;

	}
}
