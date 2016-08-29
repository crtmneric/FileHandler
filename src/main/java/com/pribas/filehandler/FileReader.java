package com.pribas.filehandler;

import java.io.File;
import java.io.IOException;

import gw.internal.ext.org.apache.commons.cli.ParseException;

public class FileReader {

	static File file;

	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		StringMatcher sm = new StringMatcher();

		sm.regexWrite();
	}

}
