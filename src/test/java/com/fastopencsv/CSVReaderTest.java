package com.fastopencsv;

/**
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class CSVReaderTest {

    AbstractCsvReader csvr;


    /**
     * Setup the test.
     */
    @Before
    public void setUp() throws Exception {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("a,b,c").append("\n");   // standard case
        sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
        sb.append(",,").append("\n"); // empty elements
        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n");
        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
        sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
        sb.append("\"a\nb\",b,\"\nd\",e\n");
        csvr = new CsvStreamReader(new StringReader(sb.toString()));
    }

    @After
    public void tearDown() throws Exception {
    	
    }
    /**
     * Tests getting the next line from a character array
     *
     */
    @Test
    public void testGetNextLineBoundaries() {
    	char[] line = new char[]{'\r', '\n', 'c', 'h', 'i', '.', 'k', 'p', 'h', '\n', 
    			'\r', 'n', 'o', '\n', 'p', 'a', 'g', 'o', 'r', 't'};
    	
    	int[] nextLine = CsvStreamReader.getNextLineBoundaries(0, 1000, line);
    	assertArrayEquals(new int[]{0,0}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(2, 1000, line);
    	assertArrayEquals(new int[]{2,8}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(0, 4, line);
    	assertArrayEquals(new int[]{0,0}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(9, line.length, line);
    	assertArrayEquals(new int[]{9,9}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(11, line.length, line);
    	assertArrayEquals(new int[]{11,12}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(13, line.length, line);
    	assertArrayEquals(new int[]{13,13}, nextLine);
    	
    	nextLine = CsvStreamReader.getNextLineBoundaries(14, line.length, line);
    	assertArrayEquals(new int[]{14,-1}, nextLine);
    	
    	line = new char[]{'\r', '\n', '\n', 
    			'\r', '\n',};
    	
    	assertArrayEquals(new int[]{0,0},CsvStreamReader.getNextLineBoundaries(0, 1000, line));
    	
    	line = new char[]{'\r', '\n', 'c', 'h', 'i', '.', 'k', 'p', 'h', '\n', '\r'};
    	
    	assertArrayEquals(new int[]{10,10}, CsvStreamReader.getNextLineBoundaries(10, line.length, line));
    }

    /**
     * Tests iterating over a reader.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testParseLine() throws IOException {

        // test normal case
        String[] nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test quoted commas
        nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b,b,b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test empty elements
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test multiline quoted
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test quoted quote chars
        nextLine = csvr.readNext();
        assertEquals("Glen \"The Man\" Smith", nextLine[0]);

        nextLine = csvr.readNext();
        assertEquals("\"\"", nextLine[0]); // check the tricky situation
        assertEquals("test", nextLine[1]); // make sure we didn't ruin the next field..

        nextLine = csvr.readNext();
        assertEquals(4, nextLine.length);

        //test end of stream
        assertNull(csvr.readNext());

    }

    @Test
    public void testParseLineStrictQuote() throws IOException {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("a,b,c").append("\n");   // standard case
        sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
        sb.append(",,").append("\n"); // empty elements
        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n");
        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
        sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
        sb.append("\"a\nb\",b,\"\nd\",e\n");
        csvr = new CsvStreamReader(new StringReader(sb.toString()), ',', '\"', true);

        // test normal case
        String[] nextLine = csvr.readNext();
        assertEquals("", nextLine[0]);
        assertEquals("", nextLine[1]);
        assertEquals("", nextLine[2]);

        // test quoted commas
        nextLine = csvr.readNext();
        assertEquals("", nextLine[0]);
        assertEquals("b,b,b", nextLine[1]);
        assertEquals("", nextLine[2]);

        // test empty elements
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test multiline quoted
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test quoted quote chars
        nextLine = csvr.readNext();
        assertEquals("Glen \"The Man\" Smith", nextLine[0]);

        nextLine = csvr.readNext();
        assertTrue(nextLine[0].equals("\"\"")); // check the tricky situation
        assertTrue(nextLine[1].equals("test")); // make sure we didn't ruin the next field..

        nextLine = csvr.readNext();
        assertEquals(4, nextLine.length);
        assertEquals("a\nb", nextLine[0]);
        assertEquals("", nextLine[1]);
        assertEquals("\nd", nextLine[2]);
        assertEquals("", nextLine[3]);

        //test end of stream
        assertNull(csvr.readNext());
    }

    public void testReadLine() {
        String str = "85_1870-08-10_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn84028385/1870-08-10/ed-1/seq-1/ocr.xml\",\"\",\"\",\"10\",\"2013-07-11 14:37:13\",\"37869\""+
        		"\"http://chroniclingamerica.loc.gov/lccn/sn92070143/issues//1905\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/1905-09-02/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/\",\"86\",\"The Imperial press. : (Imperial, Cal.) 1903-1906\",\"The Imperial press.\",\"1905\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/issues/\",\"Imperial County (Calif, )--Newspapers,; San Diego County (Calif, )--Newspapers\",\"Archived issues are available in digital format from the Library of Congress Chronicling America online collection.;Master negatives are available for duplication from:\",\"\",\"1903-1906\",\"Edgar F. Howe\",\"Weekly\",\"English\",\"Imperial, Imperial, California\",\"Imperial, Cal.\",\"The Imperial press.\",\"27084760\",\"sn92070143\",\"sn92070143_1903\",\"1905-09-30\",\"1903-04-04\",\"California\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/\",\"B#\",\"September 02, 1905, Image 1\",\"lccn_sn92070143_1905-09-02_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/1905-09-02/ed-1/seq-1.jp2\",\"lccn_sn92070143_1905-09-02_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn92070143/1905-09-02/ed-1/seq-1/ocr.xml\",\"\",\"\",\"2\",\"2013-07-11 14:37:15\",\"37905\""+
        		"\"http://chroniclingamerica.loc.gov/lccn/sn89081022/issues//1896\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/1896-05-12/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/\",\"1509\",\"Willmar tribune. : (Willmar, Minn.) 1895-1931\",\"Willmar tribune.\",\"1896\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/issues/\",\"Willmar (Minn, )--Newspapers\",\"Available on microfilm from the Minnesota Historical Society.\",\"Twice-a-week Willmar tribune; Willmar weekly tribune\",\"1895-1931\",\"[s.n.]\",\"Weekly Dec. 2, 1903-Sept. 30, 1931.\",\"English\",\"Willmar, Kandiyohi, Minnesota\",\"Willmar, Minn.\",\"Willmar tribune.\",\"1643058\",\"sn89081022\",\"sn89081022_1895\",\"1922-12-27\",\"1895-02-19\",\"Minnesota\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/\",\"B#\",\"May 12, 1896, Image 1\",\"lccn_sn89081022_1896-05-12_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/1896-05-12/ed-1/seq-1.jp2\",\"lccn_sn89081022_1896-05-12_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn89081022/1896-05-12/ed-1/seq-1/ocr.xml\",\"\",\"\",\"12\",\"2013-07-11 14:37:16\",\"37919\""+
        		"\"http://chroniclingamerica.loc.gov/lccn/sn84026403/issues//1850\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/1850-05-11/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/\",\"1127\",\"Sunbury American. : (Sunbury, Pa.) 1848-1879\",\"Sunbury American.\",\"1850\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/issues/\",\"Sunbury (Pa, )--Newspapers\",\"Archived issues are available in digital format as part of the Library of Congress Chronicling America online collection.;Editors: H.B. Masser, <1856-1876>; Emanuel Wilvert, <1876>.;Merged with: Sunbury gazette, to form: Sunbury gazette-American. Cf. Salisbury, R. Pa. newspapers.;Proprietor: H.B. Masser, <1856-1864>.;Publisher: Emanuel Wilvert, 1864-1879.\",\"\",\"1848-1879\",\"H.B. Masser\",\"Weekly\",\"English\",\"Sunbury, Northumberland, Pennsylvania\",\"Sunbury, Pa.\",\"Sunbury American.\",\"10470581\",\"sn84026403\",\"sn84026403_1848\",\"1876-04-07\",\"1848-04-01\",\"Pennsylvania\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/\",\"B#\",\"May 11, 1850, Image 1\",\"lccn_sn84026403_1850-05-11_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/1850-05-11/ed-1/seq-1.jp2\",\"lccn_sn84026403_1850-05-11_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn84026403/1850-05-11/ed-1/seq-1/ocr.xml\",\"\",\"\",\"11\",\"2013-07-11 14:37:22\",\"38015\""+
        		"\"http://chroniclingamerica.loc.gov/lccn/sn82016187/issues//1901\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/1901-02-21/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/\",\"1476\",\"The National tribune. : (Washington, D.C.) 1877-1917\",\"The National tribune.\",\"1901\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/issues/\",\"Washington (D, C, )--Newspapers\",\"Also issued on microfilm from the Library of Congress Photoduplication Service.;Archived issues are available in digital format as part of the Library of Congress Chronicling America online collection.;Began New ser., v. 1, no. 1 (Aug. 20, 1881).;Beginning with v. 2, no. 3, imprint varies: Washington, D.C. : National Tribune Company.;Issues for Aug. 12, 1882-July 12, 1917 called also whole no. 53-whole no. 1870.;Supplements accompany some issues.;Suspended July-Aug. 19, 1881?\",\"United States national tribune\",\"1877-1917\",\"G.E. Lemon & Co.\",\"Weekly Aug. 20, 1881-July 12, 1917\",\"English\",\"Washington, District of Columbia  |  View more titles from this: City State\",\"Washington, D.C.\",\"The National tribune.\",\"9186825\",\"sn82016187\",\"sn82016187_1877\",\"1911-04-27\",\"1877-10-01\",\"District of Columbia\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/\",\"B#\",\"February 21, 1901, Image 1\",\"lccn_sn82016187_1901-02-21_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/1901-02-21/ed-1/seq-1.jp2\",\"lccn_sn82016187_1901-02-21_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn82016187/1901-02-21/ed-1/seq-1/ocr.xml\",\"\",\"\",\"21\",\"2013-07-11 14:37:24\",\"38033\""+
        		"\"http://chroniclingamerica.loc.gov/lccn/sn87052181/issues//1890\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/1890-04-12/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/\",\"2198\",\"Fair play. : (Ste. Genevieve [Mo.]) 1872-1961\",\"Fair play.\",\"1890\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/issues/\",\"Sainte Genevieve (Mo, )--Newspapers,; Sainte Genevieve County (Mo, )--Newspapers\",\"Archived issues are available in digital format as part of the Library of Congress Chronicling America online collection.;Description based on: Vol. 1, no. 2 (June 14, 1872).;Suspended publication Jan. 16, 1943-Dec. 14, 1945.\",\"\",\"1872-1961\",\"S. Henry Smith\",\"Weekly\",\"English\",\"Sainte Genevieve, Sainte Genevieve, Missouri\",\"Ste. Genevieve [Mo.]\",\"Fair play.\",\"15182728\",\"sn87052181\",\"sn87052181_1872\",\"1921-12-31\",\"1872-06-14\",\"Missouri\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/\",\"B#\",\"April 12, 1890, Image 1\",\"lccn_sn87052181_1890-04-12_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/1890-04-12/ed-1/seq-1.jp2\",\"lccn_sn87052181_1890-04-12_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn87052181/1890-04-12/ed-1/seq-1/ocr.xml\",\"\",\"\",\"12\",\"2013-07-11 14:37:25\",\"38049\""+
				"\"http://chroniclingamerica.loc.gov/lccn/sn84023127/issues//1864\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/1864-10-14/ed-1/\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/\",\"1469\",\"Burlington free press. : (Burlington, Vt.) 1827-1865\",\"Burlington free press.\",\"1864\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/issues/\",\"Burlington (Vt, )--Newspapers\",\"Archived issues are available in digital format as part of the Library of Congress Chronicling America online collection.;Daily eds.: Daily free press (Burlington, Vt.), Apr. 1, 1848-July 9, 1868, and: Burlington daily free press (Burlington, Vt. : 1868), July 10-Dec. 31, 1868.;Editors: L. Foote, 1827-<1832>; D.W.C. Clarke, <1850>-1853; G.W. Benedict, 1853-<1863>; G.G. Benedict, 1854-<1863>.;Other weekly eds.: Times (Burlington, Vt. : 1858), 1858-1861, and: Burlington times (Burlington, Vt. : 1861), 1861-1865.;Publishers: Foote & Stacy, <1828-1832>; H.B. Stacy, <1835-1846>; D.W.C. Clarke, <1850>-1853; G.W. Benedict, 1853-<1863>; G.W. & G.G. Benedict, 1854-<1863>.;Republican. Cf. Rowell, 1869-1876.\",\"Weekly free press\",\"1827-1865\",\"L. Foote\",\"Weekly\",\"English\",\"Burlington, Chittenden, Vermont\",\"Burlington, Vt.\",\"Burlington free press.\",\"6497291\",\"sn84023127\",\"sn84023127_1827\",\"1865-12-29\",\"1836-01-08\",\"Vermont\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/\",\"B#\",\"October 14, 1864, Image 1\",\"lccn_sn84023127_1864-10-14_ed-1_seq-1\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/1864-10-14/ed-1/seq-1.jp2\",\"lccn_sn84023127_1864-10-14_ed-1_seq-1_ocr\",\"http://chroniclingamerica.loc.gov/lccn/sn84023127/1864-10-14/ed-1/seq-1/ocr.xml\",\"\",\"\",\"14\",\"2013-07-11 14:37:27\",\"38080\"";
        
//        for (int i = 0; i<8092 ; i++) {
//        	sb.append('j');
//        }
//        String answer = sb.toString();
//
//        sb.append('"');
//    	sb.append('\n');
//    	sb.append('"');
//    	sb.append('k');
        
        
        AbstractCsvReader c = new CsvStreamReader(new StringReader(str), ',', '"');

        try {
			char[] nextLine = c.readLine();
			assertEquals('"',nextLine[0]);
			assertEquals('h',nextLine[1]);
			assertEquals('t',nextLine[2]);
			assertEquals('t',nextLine[3]);
			assertEquals('p',nextLine[3]);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Test parsing to a list.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testParseAll() throws IOException {
        assertEquals(7, csvr.readAll().size());
    }

    /**
     * Tests constructors with optional delimiters and optional quote char.
     *
     * @throws IOException if the reader fails.
     */
    @Test
    public void testOptionalConstructors() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("a\tb\tc").append("\n");   // tab separated case
        sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements
        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), '\t', '\'');

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        nextLine = c.readNext();
        assertEquals(3, nextLine.length);
    }

    @Test
    public void parseQuotedStringWithDefinedSeperator() throws IOException {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("a\tb\tc").append("\n");   // tab separated case

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), '\t');

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

    }

    /**
     * Tests option to skip the first few lines of a file.
     *
     * @throws IOException if bad things happen
     */
    @Test
    public void testSkippingLines() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("Skip this line\t with tab").append("\n");   // should skip this
        sb.append("And this line too").append("\n");   // and this
        sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements
        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), '\t', '\'', 2);

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
    }


    /**
     * Tests option to skip the first few lines of a file.
     *
     * @throws IOException if bad things happen
     */
    @Test
    public void testSkippingLinesWithDifferentEscape() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);
        sb.append("Skip this line?t with tab").append("\n");   // should skip this
        sb.append("And this line too").append("\n");   // and this
        sb.append("a\t'b\tb\tb'\t'c'").append("\n");  // single quoted elements
        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), '\t', '\'', '?', 2);

        String[] nextLine = c.readNext();

        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals("c", nextLine[2]);
    }

    /**
     * Test a normal non quoted line with three elements
     *
     * @throws IOException
     */
    @Test
    public void testNormalParsedLine() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,1234567,c").append("\n");// a,1234,c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()));

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals("1234567", nextLine[1]);
        assertEquals("c", nextLine[2]);

    }


    /**
     * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
     * single quote.
     *
     * @throws IOException
     */
    @Test
    public void testASingleQuoteAsDataElement() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,'''',c").append("\n");// a,',c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), ',', '\'');

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals(1, nextLine[1].length());
        assertEquals("\'", nextLine[1]);
        assertEquals("c", nextLine[2]);

    }

    /**
     * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
     * single quote.  Also the middle field is empty.
     *
     * @throws IOException
     */
    @Test
    public void testASingleQuoteAsDataElementWithEmptyField() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,'',c").append("\n");// a,,c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), ',', '\'');

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals(0, nextLine[1].length());
        assertEquals("", nextLine[1]);
        assertEquals("c", nextLine[2]);

    }

    @Test
    public void testSpacesAtEndOfString() throws IOException {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("\"a\",\"b\",\"c\"   ");

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, true);

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals("b", nextLine[1]);
        assertEquals("c", nextLine[2]);
    }


    @Test
    public void testEscapedQuote() throws IOException {

        StringBuffer sb = new StringBuffer();

        sb.append("a,\"123\\\"4567\",c").append("\n");// a,123"4",c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()));

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("123\"4567", nextLine[1]);

    }

    @Test
    public void testEscapedEscape() throws IOException {

        StringBuffer sb = new StringBuffer();

        sb.append("a,\"123\\\\4567\",c").append("\n");// a,123"4",c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()));

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("123\\4567", nextLine[1]);

    }


    /**
     * Test a line where one of the elements is two single quotes and the
     * quote character is the default double quote.  The expected result is two
     * single quotes.
     *
     * @throws IOException
     */
    @Test
    public void testSingleQuoteWhenDoubleQuoteIsQuoteChar() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,'',c").append("\n");// a,'',c

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()));

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals(2, nextLine[1].length());
        assertEquals("''", nextLine[1]);
        assertEquals("c", nextLine[2]);

    }

    /**
     * Test a normal line with three elements and all elements are quoted
     *
     * @throws IOException
     */
    @Test
    public void testQuotedParsedLine() throws IOException {

        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("\"a\",\"1234567\",\"c\"").append("\n"); // "a","1234567","c"

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, true);

        String[] nextLine = c.readNext();
        assertEquals(3, nextLine.length);

        assertEquals("a", nextLine[0]);
        assertEquals(1, nextLine[0].length());

        assertEquals("1234567", nextLine[1]);
        assertEquals("c", nextLine[2]);

    }

    @Test
    public void testIssue2992134OutOfPlaceQuotes() throws IOException {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()));

        String[] nextLine = c.readNext();

        assertEquals("a", nextLine[0]);
        assertEquals("b", nextLine[1]);
        assertEquals("c", nextLine[2]);
        assertEquals("ddd\"eee", nextLine[3]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void quoteAndEscapeMustBeDifferent() {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_QUOTE_CHARACTER, AbstractCsvReader.DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void separatorAndEscapeMustBeDifferent() {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_SEPARATOR, AbstractCsvReader.DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void separatorAndQuoteMustBeDifferent() {
        StringBuilder sb = new StringBuilder(CSVParser.INITIAL_READ_SIZE);

        sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

        AbstractCsvReader c = new CsvStreamReader(new StringReader(sb.toString()), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_ESCAPE_CHARACTER, AbstractCsvReader.DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
    }

}
