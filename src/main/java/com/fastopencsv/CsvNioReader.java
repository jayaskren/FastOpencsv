package com.fastopencsv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class CsvNioReader extends AbstractCsvReader {

	protected FileChannel channel;
	protected RandomAccessFile file;
	protected ByteBuffer byteBuffer;
	protected final Charset charEncoding;
	
	/**
	 * Constructs CSVReader using a comma for the separator.
	 * 
	 * @param reader
	 *            the reader to an underlying CSV source.
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding) throws IOException {
		this(file, charEncoding, CSVParser.DEFAULT_SEPARATOR,
				CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER);
	}

	/**
	 * Constructs CSVReader with supplied separator.
	 * 
	 * @param reader
	 *            the reader to an underlying CSV source.
	 * @param separator
	 *            the delimiter to use for separating entries.
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator) throws IOException {
		this(file, charEncoding, separator, CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER);
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
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar)
			throws IOException {
		this(file, charEncoding, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER,
				DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
	}

	/**
	 * Constructs CSVReader with supplied separator, quote char and quote
	 * handling behavior.
	 * 
	 * @param reader
	 *            the reader to an underlying CSV source.
	 * @param separator
	 *            the delimiter to use for separating entries
	 * @param quotechar
	 *            the character to use for quoted elements
	 * @param strictQuotes
	 *            sets if characters outside the quotes are ignored
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar,
			boolean strictQuotes) throws IOException {
		this(file, charEncoding, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER,
				DEFAULT_SKIP_LINES, strictQuotes);
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
	 * @throws IOException
	 */

	public CsvNioReader(File file, String charEncoding, char separator, char quotechar, char escape)
			throws IOException {
		this(file, charEncoding, separator, quotechar, escape, DEFAULT_SKIP_LINES,
				CSVParser.DEFAULT_STRICT_QUOTES);
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
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar, int line)
			throws IOException {
		this(file, charEncoding, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER,
				line, CSVParser.DEFAULT_STRICT_QUOTES);
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
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar, char escape,
			int line) throws IOException {
		this(file, charEncoding, separator, quotechar, escape, line,
				CSVParser.DEFAULT_STRICT_QUOTES);
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
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar, char escape,
			int line, boolean strictQuotes) throws IOException {
		this(file, charEncoding, separator, quotechar, escape, line, strictQuotes,
				CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
	}

	/**
	 * Constructs CSVReader with supplied separator and quote char.
	 * 
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
	 *            it true, parser should ignore white space before a quote in a
	 *            field
	 * @throws IOException
	 */
	public CsvNioReader(File file, String charEncoding, char separator, char quotechar, char escape,
			int line, boolean strictQuotes, boolean ignoreLeadingWhiteSpace)
			throws IOException {
		this.parser = new CSVParser(separator, quotechar, escape, strictQuotes,
				ignoreLeadingWhiteSpace);
		this.skipLines = line;
		this.charEncoding = Charset.forName(charEncoding);
		this.file = new RandomAccessFile(file, "r");
		if (file != null) {
			channel = this.file.getChannel();
		}

		byteBuffer = ByteBuffer.allocate(this.bufferSize);
		totalCharactersRead = charactersRead = read();
		positionInBuffer = 0;
		// System.out.println("Done with constructor");
	}

	@Override
	public final int read() throws IOException {
		byteBuffer.clear();
		int count = channel.read(byteBuffer);
		if (count < 0) {
			return count;
		}

		byteBuffer.flip();
		buffer = charEncoding.decode(byteBuffer);
		count = buffer.remaining();
		return count;
	}

	@Override
	public final void close() throws IOException {
		file.close();
	}

}
