package com.fastopencsv;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCsvReader implements AutoCloseable {

	protected int bufferSize = 1024 * 1024;
	protected boolean hasNext = true;
	protected CSVParser parser;
	protected int skipLines;
	protected boolean linesSkiped;
	protected CharBuffer buffer;
	protected int positionInBuffer = -1;
	protected long totalCharactersRead = 0;
	protected int charactersRead;
	protected char[] nextLine;
	
	/**
	 * The default line to start reading.
	 */
	public static final int DEFAULT_SKIP_LINES = 0;

	public static final int[] getNextLineBoundaries(int start, int end,
			CharBuffer characters) {
		int[] returnValue = new int[] { -1, -1 };

		int i = start;
		while (i < end && characters.hasRemaining()) {
			// System.out.println("boundaries " + characters);
			if (characters.get(i) == '\r' || characters.get(i) == '\n') {
				int previousPos = i - 1;
				if (previousPos >= returnValue[0] && returnValue[0] >= 0) {
					// We have already set the first position. Now we have
					// reached the end of this line.
					// Set the second position
					returnValue[1] = previousPos; // TODO
					break;
				} else {
					returnValue[1] = returnValue[0] = i;
					break;
				}

			} else {
				if (returnValue[0] < 0) {
					// We have not set the first position yet and we see a non
					// new
					// line character. This is the first character of the line.
					returnValue[0] = i;
				}
			}
			i++;
		}

		return returnValue;
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
	public final String[] readNext() throws IOException {
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
					String[] t = new String[result.length + r.length];
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
	private final char[] getNextLine() throws IOException {
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

	public final char[] getNextLineText() {
		return nextLine;
	}

	protected final char[] readLine() throws IOException {
		boolean foundLine = false;
		char[] returnLine = null;
		char[] partialLine = null;

		while (!foundLine) {
			// System.out.println("readline " + positionInBuffer + " " + new
			// String(buffer));
			if (positionInBuffer < 0 || !buffer.hasRemaining()) {
				// read more data
				charactersRead = read();

				totalCharactersRead += charactersRead;
				positionInBuffer = 0;
			}

			int[] lineBoundaries = getNextLineBoundaries(positionInBuffer,
					charactersRead, buffer);
			int lengthOfLine;
			if (lineBoundaries[0] < 0 && lineBoundaries[1] < 0) {
				// We reached the end of the buffer and did not find a new line.
				// reset positionInBuffer
				// Continue and read in more data
				if (charactersRead < 0) {
					return partialLine;
				}
				positionInBuffer = -1;
				continue;
			} else if (lineBoundaries[1] >= 0) {
				// Newline found
				lengthOfLine = lineBoundaries[1]
						- Math.max(0, lineBoundaries[0]) + 1;
			} else {
				lengthOfLine = charactersRead - lineBoundaries[0];
			}
			/************************************************************************************************
			 * TODO I think this is broken because lineBounardaries[0] is not
			 * taken into account when populating the buffer like it was in
			 * System.arrayCopy
			 *************************************************************************************************/
			if (charactersRead < 0) {
				// We've reached the end of the file
				return partialLine;
			} else if (lineBoundaries[0] >= 0) {

				if (lineBoundaries[1] < 0) {
					// If partialLine is already created, just add to it.
					// TODO this is found below. Break it out as a function or
					// change the if statement?
					if (partialLine == null) {
						partialLine = new char[lengthOfLine];
						buffer.position(lineBoundaries[0]);
						buffer.get(partialLine, 0, lengthOfLine);
						// System.arraycopy(buffer, lineBoundaries[0],
						// partialLine, 0, lengthOfLine);
					} else {
						char[] newPartialLine = new char[lengthOfLine
								+ partialLine.length];
						// TODO do we need two copies?
						System.arraycopy(partialLine, 0, newPartialLine, 0,
								partialLine.length);
						// TODO this does not look right
						buffer.position(lineBoundaries[0]);
						buffer.get(newPartialLine, partialLine.length,
								lengthOfLine);
						// System.arraycopy(buffer, lineBoundaries[0],
						// newPartialLine, partialLine.length, lengthOfLine);
						partialLine = newPartialLine;
					}
					positionInBuffer = -1;
				} else if (lineBoundaries[0] == lineBoundaries[1]
						&& (buffer.get(lineBoundaries[0]) == '\n' || buffer
								.get(lineBoundaries[0]) == '\r')) {
					// Found a new line. If there is a partial line, return it
					// otherwise keep looking.
					// Todo put a better check than this

					positionInBuffer = lineBoundaries[1] + 2;
					if (partialLine != null) {
						return partialLine;
					}

				} else {
					if (partialLine == null) {
						returnLine = new char[lengthOfLine];
						buffer.position(lineBoundaries[0]);
						buffer.get(returnLine, 0, lengthOfLine);
						// System.arraycopy(buffer, lineBoundaries[0],
						// returnLine, 0, lengthOfLine);
					} else {
						returnLine = new char[lengthOfLine + partialLine.length];
						// TODO is this right?
						System.arraycopy(partialLine, 0, returnLine, 0,
								partialLine.length);
						buffer.position(lineBoundaries[0]);
						buffer.get(returnLine, partialLine.length, lengthOfLine);
						// System.arraycopy(buffer, lineBoundaries[0],
						// returnLine, partialLine.length, lengthOfLine);
						partialLine = null;
					}
					positionInBuffer = lineBoundaries[1] + 2;
					return returnLine;
				}
			} else {
				// Keep looping. The only case where I can think of that this
				// would happen is if
				// the buffer is full of new line characters. Hopefully that
				// will never happen.
			}

		}
		return returnLine;
	}

	public AbstractCsvReader() {
		super();
	}

	public final long getTotalCharactersRead() {
		return totalCharactersRead;
	}

	public abstract int read() throws IOException;

	/**
	 * Closes the underlying reader.
	 * 
	 * @throws IOException
	 *             if the close fails
	 */
	public abstract void close() throws IOException;

}
