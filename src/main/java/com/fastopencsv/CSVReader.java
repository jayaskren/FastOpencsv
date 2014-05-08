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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple CSV reader released under a commercial-friendly license.
 * 
 * @author Glen Smith
 * 
 */
public class CSVReader implements Closeable {

	public final static int BUFFER_SIZE = 1024*1024;
    private Reader reader;

    private boolean hasNext = true;

    private CSVParser parser;
    
    private int skipLines;

    private boolean linesSkiped;
    
    private char[] buffer;
    private int positionInBuffer = -1;

    long totalCharactersRead = 0;
    int charactersRead;

	private char[] nextLine;

    /**
     * The default line to start reading.
     */
    public static final int DEFAULT_SKIP_LINES = 0;

    /**
     * Constructs CSVReader using a comma for the separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     */
    public CSVReader(Reader reader) {
        this(reader, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries.
     */
    public CSVReader(Reader reader, char separator) {
        this(reader, separator, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     */
    public CSVReader(Reader reader, char separator, char quotechar) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
    }

    /**
     * Constructs CSVReader with supplied separator, quote char and quote handling
     * behavior.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param strictQuotes
     *            sets if characters outside the quotes are ignored
     */
    public CSVReader(Reader reader, char separator, char quotechar, boolean strictQuotes) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, strictQuotes);
    }

   /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     */

    public CSVReader(Reader reader, char separator,
			char quotechar, char escape) {
        this(reader, separator, quotechar, escape, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
	}
    
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param line
     *            the line number to skip for start reading 
     */
    public CSVReader(Reader reader, char separator, char quotechar, int line) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, line, CSVParser.DEFAULT_STRICT_QUOTES);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param line
     *            the line number to skip for start reading
     */
    public CSVReader(Reader reader, char separator, char quotechar, char escape, int line) {
        this(reader, separator, quotechar, escape, line, CSVParser.DEFAULT_STRICT_QUOTES);
    }
    
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param line
     *            the line number to skip for start reading
     * @param strictQuotes
     *            sets if characters outside the quotes are ignored
     */
    public CSVReader(Reader reader, char separator, char quotechar, char escape, int line, boolean strictQuotes) {
        this(reader, separator, quotechar, escape, line, strictQuotes, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param line
     *            the line number to skip for start reading
     * @param strictQuotes
     *            sets if characters outside the quotes are ignored
     * @param ignoreLeadingWhiteSpace
     *            it true, parser should ignore white space before a quote in a field
     */
    public CSVReader(Reader reader, char separator, char quotechar, char escape, int line, boolean strictQuotes, boolean ignoreLeadingWhiteSpace) {
        this.reader = reader;
        this.parser = new CSVParser(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace);
        this.skipLines = line;
        buffer = new char[BUFFER_SIZE];
    }

   /**
     * Reads the entire file into a List with each element being a String[] of
     * tokens.
     * 
     * @return a List of String[], with each String[] representing a line of the
     *         file.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public List<String[]> readAll() throws IOException {

        List<String[]> allElements = new ArrayList<String[]>();
        while (hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null)
                allElements.add(nextLineAsTokens);
        }
        return allElements;

    }

    /**
     * Reads the next line from the buffer and converts to a string array.
     * 
     * @return a string array with each comma-separated element as a separate
     *         entry.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public String[] readNext() throws IOException {
    	String[] result = null;
    	do {
    		nextLine = getNextLine();
    		if (!hasNext) {
    			return result; // should throw if still pending?
    		}
    		String[] r = parser.parseLineMulti(nextLine);
    		if (r.length > 0) {
    			if (result == null) {
    				result = r;
    			} else {
    				String[] t = new String[result.length+r.length];
    				System.arraycopy(result, 0, t, 0, result.length);
    				System.arraycopy(r, 0, t, result.length, r.length);
    				result = t;
    			}
    		}
    	} while (parser.isPending());
    	return result;
    }

    /**
     * Reads the next line from the file.
     * 
     * @return the next line from the file without trailing newline
     * @throws IOException
     *             if bad things happen during the read
     */
    private char[] getNextLine() throws IOException {
    	if (!this.linesSkiped) {
            for (int i = 0; i < skipLines; i++) {
                readLine();
            }
            this.linesSkiped = true;
        }
        char[] nextLine = readLine();
        if (nextLine == null) {
            hasNext = false;
        }
        return hasNext ? nextLine : null;
    }
    
    public char[] getNextLineText() {
		return nextLine;
	}
    
    char[] readLine() throws IOException{
    	boolean foundLine = false;
    	char[] returnLine = null;
    	char[] partialLine = null;
    	
    	while (!foundLine) {
//    		System.out.println("readline " + positionInBuffer + " " + new String(buffer));
    		if (positionInBuffer < 0 || positionInBuffer >= buffer.length) {
    			// read more data
    			charactersRead = reader.read(buffer); 
    			
        		totalCharactersRead += charactersRead;
        		positionInBuffer = 0;
    		}
    		
    		int[] lineBoundaries = getNextLineBoundaries(positionInBuffer, charactersRead, buffer);
    		int lengthOfLine;
    		if (lineBoundaries[0] < 0 && lineBoundaries[1] < 0) {
    			// We reached the end of the buffer and did not find the a new line.
    			// reset positionInBuffer
    			// Continue and read in more data
    			if (charactersRead < 0) {
    				return partialLine;
    			}
    			positionInBuffer = -1;
    			continue;
    		} else if (lineBoundaries[1] >= 0) {
    			// Newline found
    			lengthOfLine = lineBoundaries[1] - Math.max(0, lineBoundaries[0])+1;
    		} else {
    			lengthOfLine = charactersRead - lineBoundaries[0];
    		}
    		
    		if (charactersRead < 0 ) {
    			// We've reached the end of the file
    			return partialLine;
    		} else if (lineBoundaries[0]>=0) {
    			if  (lineBoundaries[1] < 0) {
    				// If partialLine is already created, just add to it.
    				if (partialLine == null) {
    					partialLine = new char[lengthOfLine];
    					System.arraycopy(buffer, lineBoundaries[0], partialLine, 0, lengthOfLine);
    				} else {
    					char[] newPartialLine = new char[lengthOfLine];
    					System.arraycopy(partialLine, 0, newPartialLine, 0, partialLine.length);
    					System.arraycopy(buffer, lineBoundaries[0], newPartialLine, partialLine.length, lengthOfLine);
    					partialLine = newPartialLine;
    				}
    				positionInBuffer = -1;
    			} else if (lineBoundaries[0] == lineBoundaries[1] && (buffer[lineBoundaries[0]] == '\n' || buffer[lineBoundaries[0]] == '\r')){
    				//    Found a new line.  If there is a partial line, return it otherwise keep looking.
					// Todo put a better check than this
    				
    				positionInBuffer = lineBoundaries[1]+2;
    				if(partialLine != null) {
    					return partialLine;
    				}

    			} else {
    				if (partialLine == null) {
    					returnLine = new char[lengthOfLine];
    					System.arraycopy(buffer, lineBoundaries[0], returnLine, 0, lengthOfLine);
    				} else {
    					returnLine = new char[lengthOfLine + partialLine.length];
    					System.arraycopy(partialLine, 0, returnLine, 0, partialLine.length);
    					System.arraycopy(buffer, lineBoundaries[0], returnLine, partialLine.length, lengthOfLine);
    					partialLine = null;
    				}
    				positionInBuffer = lineBoundaries[1]+2;
    				return returnLine;
    			}
    		} else {
    			// Keep looping.  The only case where I can think of that this would happen is if  
    			//  the buffer is full of new line characters.  Hopefully that will never happen.
    		}

    	}
    	return returnLine;
    }
	
    public static int[] getNextLineBoundaries(int start, int end, char[] characters) {
    	int[] returnValue = new int[]{-1,-1};
    	final int length = Math.min(characters.length, end);
    	for (int i=start; i< length; i++) {
//    		System.out.println("boundaries " + characters);
    		if (characters[i] == '\n' || characters[i] == '\r') {
    			int previousPos = i-1;
    			if (returnValue[0] >= 0 && previousPos >= returnValue[0] ) {
    				 // We have already set the first position.  Now we have reached the end of this line.
    				 // Set the second position
    				returnValue[1] = previousPos; // TODO
    				break;
    			 } else {
    				 returnValue[1] = i;
    				 returnValue[0] = i;
    				 break;
    			 }
    			
			} else {
				if (returnValue[0] < 0) {
					// We have not set the first position yet and we see a non new 
					//   line character.  This is the first character of the line.
    				returnValue[0] = i;
    			}
			}
    	}

    	return returnValue;
    }
    
    public long getTotalCharactersRead() {
		return totalCharactersRead;
	}

	/**
     * Closes the underlying reader.
     * 
     * @throws IOException if the close fails
     */
    public void close() throws IOException{
    	reader.close();
    }
    
}
