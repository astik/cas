/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support.kryo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that provides convenience methods for getting and setting field values via reflection.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public final class FieldHelper {
    private final Map<String, Field> fieldCache = new HashMap<String, Field>();

    /**
     * Gets the field value.
     *
     * @param target the target
     * @param fieldName the field name
     * @return the field value
     */
    public Object getFieldValue(final Object target, final String fieldName) {
        final Field f = getField(target, fieldName);
        try {
            return f.get(target);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Error getting field value", e);
        }
    }

    /**
     * Sets the field value.
     *
     * @param target the target
     * @param fieldName the field name
     * @param value the value
     */
    public void setFieldValue(final Object target, final String fieldName, final Object value) {
        final Field f = getField(target, fieldName);
        try {
            f.set(target, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Error setting field value", e);
        }
    }

    /**
     * Gets the field.
     *
     * @param target the target
     * @param name the name
     * @return the field
     */
    private Field getField(final Object target, final String name) {
        Class<?> clazz = target.getClass();
        final String key = new StringBuilder().append(clazz.getName()).append('.').append(name).toString();
        Field f = fieldCache.get(key);
        while (f == null) {
            try {
                f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                fieldCache.put(key, f);
            } catch (final NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    throw new IllegalStateException("No such field " + key);
                }
            }
        }
        return f;
    }

}
