package com.fastopencsv.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.fastopencsv.AbstractCsvReader;
import com.fastopencsv.CsvStreamReader;

public class CsvToBeanTest {

    private static final String TEST_STRING = "name,orderNumber,num\n" +
            "kyle,abc123456,123\n" +
            "jimmy,def098765,456 ";

    private AbstractCsvReader createReader() {
        StringReader reader = new StringReader(TEST_STRING);
        return new CsvStreamReader(reader);
    }

    private MappingStrategy createErrorMappingStrategy() {
        return new MappingStrategy() {

            public PropertyDescriptor findDescriptor(int col) throws IntrospectionException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object createBean() throws InstantiationException, IllegalAccessException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void captureHeader(AbstractCsvReader reader) throws IOException {
                throw new IOException("This is the test exception");
            }
        };
    }

    @Test(expected = RuntimeException.class)
    public void throwRuntimeExceptionWhenExceptionIsThrown() {
        CsvToBean bean = new CsvToBean();
        bean.parse(createErrorMappingStrategy(), createReader());
    }
}
