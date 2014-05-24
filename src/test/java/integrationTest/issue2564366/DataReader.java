/**
 * 
 */
package integrationTest.issue2564366;


import java.io.BufferedReader;
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
	private static final String ADDRESS_FILE="/test/integrationTest/issue2564366/data.csv";

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
					System.out.println("CompanyName: [" + nextLine[0] + "]\nCompanyNumber: [" + nextLine[1] + "]\nClientName: [" + nextLine[2] + "]");
					System.out.println("ClientFirstName: [" + nextLine[3] + "]\nClientLastName: [" + nextLine[4] + "]\nClientId: [" + nextLine[5] + "]");
					System.out.println("ClientGroupId: [" + nextLine[6] + "]\nLogon: [" + nextLine[7] + "]\nLogonPW: [" + nextLine[8] + "]");
					System.out.println("PublishKey: [" + nextLine[9] + "]\nHiddenKey: [" + nextLine[10] + "]\nPublishEncryptMode: [" + nextLine[11] + "]");
					System.out.println("LanFolderId: [" + nextLine[12] + "]\nStaffId: [" + nextLine[13] + "]\n");
				}
			}

	}

}
