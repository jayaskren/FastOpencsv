/**
 * 
 */
package integrationTest.issue2153020;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fastopencsv.AbstractCsvReader;
import com.fastopencsv.CsvStreamReader;

/**
 * @author scott
 *
 */
public class DataReader
{
	private static final String ADDRESS_FILE="/integrationTest/issue2153020/Sample.csv";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		try (InputStream is = DataReader.class.getResourceAsStream(ADDRESS_FILE);
				AbstractCsvReader reader = new CsvStreamReader(new BufferedReader(new InputStreamReader(is)))) {
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				int numLines = nextLine.length;
				System.out.println("Number of Data Items: " + numLines);
				for (int i = 0; i < numLines; i++) {
					System.out.println("     nextLine[" + i + "]:  " + nextLine[i]);
				}
			}
		}
	}

}
