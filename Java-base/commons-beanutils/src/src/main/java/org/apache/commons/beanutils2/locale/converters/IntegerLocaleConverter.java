/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.beanutils2.locale.converters;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.beanutils2.ConversionException;

/**
 * <p>Standard {@link org.apache.commons.beanutils2.locale.LocaleConverter}
 * implementation that converts an incoming
 * locale-sensitive String into a {@code java.lang.Integer} object,
 * optionally using a default value or throwing a
 * {@link org.apache.commons.beanutils2.ConversionException}
 * if a conversion error occurs.</p>
 */
public class IntegerLocaleConverter extends DecimalLocaleConverter {

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine and an unlocalized pattern is used
     * for the conversion.
     */
    public IntegerLocaleConverter() {
        this(false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine.
     *
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final boolean locPattern) {
        this(Locale.getDefault(), locPattern);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs. An unlocalized pattern is used for the conversion.
     *
     * @param locale        The locale
     */
    public IntegerLocaleConverter(final Locale locale) {
        this(locale, false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs.
     *
     * @param locale        The locale
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final Locale locale, final boolean locPattern) {
        this(locale, (String) null, locPattern);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs. An unlocalized pattern is used for the conversion.
     *
     * @param locale        The locale
     * @param pattern       The conversion pattern
     */
    public IntegerLocaleConverter(final Locale locale, final String pattern) {
        this(locale, pattern, false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will throw a {@link org.apache.commons.beanutils2.ConversionException}
     * if a conversion error occurs.
     *
     * @param locale        The locale
     * @param pattern       The conversion pattern
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final Locale locale, final String pattern, final boolean locPattern) {
        super(locale, pattern, locPattern);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine and an unlocalized pattern is used
     * for the conversion.
     *
     * @param defaultValue  The default value to be returned
     */
    public IntegerLocaleConverter(final Object defaultValue) {
        this(defaultValue, false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine.
     *
     * @param defaultValue  The default value to be returned
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final Object defaultValue, final boolean locPattern) {
        this(defaultValue, Locale.getDefault(), locPattern);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs. An unlocalized pattern is used for the conversion.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     */
    public IntegerLocaleConverter(final Object defaultValue, final Locale locale) {
        this(defaultValue, locale, false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final Object defaultValue, final Locale locale, final boolean locPattern) {
        this(defaultValue, locale, null, locPattern);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs. An unlocalized pattern is used for the conversion.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param pattern       The conversion pattern
     */
    public IntegerLocaleConverter(final Object defaultValue, final Locale locale, final String pattern) {
        this(defaultValue, locale, pattern, false);
    }

    /**
     * Create a {@link org.apache.commons.beanutils2.locale.LocaleConverter}
     * that will return the specified default value
     * if a conversion error occurs.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param pattern       The conversion pattern
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public IntegerLocaleConverter(final Object defaultValue, final Locale locale, final String pattern, final boolean locPattern) {
        super(defaultValue, locale, pattern, locPattern);
    }

    /**
     * Convert the specified locale-sensitive input object into an output object of the
     * specified type. This method will return Integer type.
     *
     * @param value The input object to be converted
     * @param pattern The pattern is used for the conversion
     * @return The converted value
     *
     * @throws ConversionException if conversion cannot be performed
     *  successfully
     * @throws ParseException if an error occurs parsing a String to a Number
     */
    @Override
    protected Object parse(final Object value, final String pattern) throws ParseException {
        final Number parsed = (Number) super.parse(value, pattern);
        if (parsed.longValue() != parsed.intValue()) {
            throw new ConversionException("Supplied number is not of type Integer: " + parsed.longValue());
        }
        return Integer.valueOf(parsed.intValue()); // unlike superclass it will return proper Integer
    }
}
