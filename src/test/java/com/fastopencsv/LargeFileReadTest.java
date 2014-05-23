package com.fastopencsv;

import static org.junit.Assert.assertEquals;
import integrationTest.issue2153020.DataReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
		int count = 0;
		try (InputStream is = DataReader.class.getResourceAsStream(ADDRESS_FILE);
				CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
				CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("C:\\Jay\\test.csv")))))) {

			String[] nextLine = reader.readNext();
			writer.writeNext(nextLine);
			System.out.println(ADDRESS_FILE + " file has " + nextLine.length + " columns");
//			assertEquals(4, nextLine.length);
			assertEquals(36, nextLine.length);
//			assertEquals(36, nextLine.length);
			count++;
			while((nextLine = reader.readNext()) != null)  {
//				if (count == 9075){
//					System.out.println(count + " " + Arrays.toString(nextLine));
//				}
				assertEquals("Broke at line " + count + ": ", 36, nextLine.length);
				writer.writeNext(nextLine);
				count++;
			}
//			assertEquals(264, count);
//			assertEquals(9081, count);
			assertEquals(275830, count);
			
			System.out.println(ADDRESS_FILE + " file has " + count + " rows");
		}
	}
}
