package com.fastopencsv.bean;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fastopencsv.CSVReader;


/**
 * Copyright 2007 Kyle Miller.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class HeaderColumnNameMappingStrategy<T> implements MappingStrategy<T> {
    protected String[] header;
    protected Map<String, PropertyDescriptor> descriptorMap = null;
    protected Class<T> type;

    public void captureHeader(CSVReader reader) throws IOException {
        header = reader.readNext();
    }

    public PropertyDescriptor findDescriptor(int col) throws IntrospectionException {
        String columnName = getColumnName(col);
        return (null != columnName && columnName.trim().length() > 0) ? findDescriptor(columnName) : null;
    }

    protected String getColumnName(int col) {
        return (null != header && col < header.length) ? header[col] : null;
    }

    protected PropertyDescriptor findDescriptor(String name) throws IntrospectionException {
        if (null == descriptorMap) descriptorMap = loadDescriptorMap(getType()); //lazy load descriptors
        return descriptorMap.get(name.toUpperCase().trim());
    }

    protected boolean matches(String name, PropertyDescriptor desc) {
        return desc.getName().equals(name.trim());
    }

    protected Map<String, PropertyDescriptor> loadDescriptorMap(Class<T> cls) throws IntrospectionException {
        Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();

        PropertyDescriptor[] descriptors;
        descriptors = loadDescriptors(getType());
        for (PropertyDescriptor descriptor : descriptors) {
            map.put(descriptor.getName().toUpperCase().trim(), descriptor);
        }

        return map;
    }

    private PropertyDescriptor[] loadDescriptors(Class<T> cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        return beanInfo.getPropertyDescriptors();
    }

    public T createBean() throws InstantiationException, IllegalAccessException {
        return type.newInstance();
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }
}
