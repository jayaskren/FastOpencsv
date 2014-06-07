package com.fastopencsv;

import static org.junit.Assert.assertEquals;
import integrationTest.issue2153020.DataReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.junit.Test;

public class LargeFileReadTest {

	@Test
	public void testReadLargeFile() throws Exception {
		String ADDRESS_FILE = "/CtyxCty_US.csv";
//		String ADDRESS_FILE = "/LoC-FullSiteMap-reduced.csv";
//		String ADDRESS_FILE = "/TB_data_dictionary_2014-03-31.csv";
		
		File streamFile = new File("./testStream.csv");
		File nioFile = new File("./testNio.csv");
		try (
				InputStream is = DataReader.class.getResourceAsStream(ADDRESS_FILE);
				AbstractCsvReader streamReader = new CsvStreamReader(new BufferedReader(new InputStreamReader(is)));
				AbstractCsvReader nioReader = new CsvNioReader(new File(DataReader.class.getResource(ADDRESS_FILE).getFile()), "UTF-8");
				CSVWriter streamWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streamFile))));
				CSVWriter nioWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nioFile))))) {

			testReader(streamReader, streamWriter, ADDRESS_FILE);
			testReader(nioReader, nioWriter, ADDRESS_FILE);
		} finally {
			streamFile.delete();
			nioFile.delete();
		}
	}
	
	public void testReader(AbstractCsvReader reader, CSVWriter writer,  String addressFile) throws IOException {
		String[] nextLine = reader.readNext();
		int count = 0;
		writer.writeNext(nextLine);
//		System.out.println(addressFile + " file has " + nextLine.length + " columns");
//		assertEquals(4, nextLine.length);
//		System.out.println(count + " " + Arrays.toString(nextLine));
		assertEquals(36, nextLine.length);
		assertEquals(36, nextLine.length);
		count++;
		while((nextLine = reader.readNext()) != null)  {
			assertEquals("Broke at line " + count + ": ", 36, nextLine.length);
			writer.writeNext(nextLine);
			count++;
		}
//		assertEquals(264, count);
//		assertEquals(9081, count);
		assertEquals(275830, count);
	}
}
