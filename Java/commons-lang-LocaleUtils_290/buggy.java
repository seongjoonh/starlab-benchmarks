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
package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Operations to assist when working with a {@link Locale}.</p>
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will not be thrown for a {@code null} input.
 * Each method documents its behaviour in more detail.</p>
 *
 * @since 2.2
 */
public class LocaleUtils {

    /** Concurrent map of language locales by country. */
    private static final ConcurrentMap<String, List<Locale>> cLanguagesByCountry = 
        new ConcurrentHashMap<String, List<Locale>>();

    /** Concurrent map of country locales by language. */
    private static final ConcurrentMap<String, List<Locale>> cCountriesByLanguage = 
        new ConcurrentHashMap<String, List<Locale>>();

    /**
     * <p>{@code LocaleUtils} instances should NOT be constructed in standard programming.
     * Instead, the class should be used as {@code LocaleUtils.toLocale("en_GB");}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public LocaleUtils() {
      super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("")           = new Locale("", "")
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param str  the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     * @see Locale#forLanguageTag(String)
     */
    public static Locale toLocale(final String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
            return new Locale(StringUtils.EMPTY, StringUtils.EMPTY);
        }
        if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == '_') {
            if (len < 3) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 3) {
                return new Locale(StringUtils.EMPTY, str.substring(1, 3));
            }
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(3) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(StringUtils.EMPTY, str.substring(1, 3), str.substring(4));
        }
        
        final String[] split = str.split("_", -1);
        final int occurrences = split.length -1;
        switch (occurrences) {
            case 0:
                if (StringUtils.isAllLowerCase(str) && (len == 2 || len == 3)) {
                    return new Locale(str);
                }
            throw new IllegalArgumentException("Invalid locale format: " + str);
                
            case 1:
                if (StringUtils.isAllLowerCase(split[0]) &&
                    (split[0].length() == 2 || split[0].length() == 3) &&
                     split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])) {
                    return new Locale(split[0], split[1]);
                }
            throw new IllegalArgumentException("Invalid locale format: " + str);

            case 2:
                if (StringUtils.isAllLowerCase(split[0]) && 
                    (split[0].length() == 2 || split[0].length() == 3) &&
                    (split[1].length() == 0 || split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])) &&
                     split[2].length() > 0) {
                    return new Locale(split[0], split[1], split[2]);
                }

                //$FALL-THROUGH$
            default:
                throw new IllegalArgumentException("Invalid locale format: " + str);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains the list of locales to search through when performing
     * a locale search.</p>
     *
     * <pre>
     * localeLookupList(Locale("fr","CA","xxx"))
     *   = [Locale("fr","CA","xxx"), Locale("fr","CA"), Locale("fr")]
     * </pre>
     *
     * @param locale  the locale to start from
     * @return the unmodifiable list of Locale objects, 0 being locale, not null
     */
    public static List<Locale> localeLookupList(final Locale locale) {
        return localeLookupList(locale, locale);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains the list of locales to search through when performing
     * a locale search.</p>
     *
     * <pre>
     * localeLookupList(Locale("fr", "CA", "xxx"), Locale("en"))
     *   = [Locale("fr","CA","xxx"), Locale("fr","CA"), Locale("fr"), Locale("en"]
     * </pre>
     *
     * <p>The result list begins with the most specific locale, then the
     * next more general and so on, finishing with the default locale.
     * The list will never contain the same locale twice.</p>
     *
     * @param locale  the locale to start from, null returns empty list
     * @param defaultLocale  the default locale to use if no other is found
     * @return the unmodifiable list of Locale objects, 0 being locale, not null
     */
    public static List<Locale> localeLookupList(final Locale locale, final Locale defaultLocale) {
        final List<Locale> list = new ArrayList<Locale>(4);
        if (locale != null) {
            list.add(locale);
            if (locale.getVariant().length() > 0) {
                list.add(new Locale(locale.getLanguage(), locale.getCountry()));
            }
            if (locale.getCountry().length() > 0) {
                list.add(new Locale(locale.getLanguage(), StringUtils.EMPTY));
            }
            if (list.contains(defaultLocale) == false) {
                list.add(defaultLocale);
            }
        }
        return Collections.unmodifiableList(list);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains an unmodifiable list of installed locales.</p>
     * 
     * <p>This method is a wrapper around {@link Locale#getAvailableLocales()}.
     * It is more efficient, as the JDK method must create a new array each
     * time it is called.</p>
     *
     * @return the unmodifiable list of available locales
     */
    public static List<Locale> availableLocaleList() {
        return SyncAvoid.AVAILABLE_LOCALE_LIST;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains an unmodifiable set of installed locales.</p>
     * 
     * <p>This method is a wrapper around {@link Locale#getAvailableLocales()}.
     * It is more efficient, as the JDK method must create a new array each
     * time it is called.</p>
     *
     * @return the unmodifiable set of available locales
     */
    public static Set<Locale> availableLocaleSet() {
        return SyncAvoid.AVAILABLE_LOCALE_SET;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Checks if the locale specified is in the list of available locales.</p>
     *
     * @param locale the Locale object to check if it is available
     * @return true if the locale is a known locale
     */
    public static boolean isAvailableLocale(final Locale locale) {
        return availableLocaleList().contains(locale);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains the list of languages supported for a given country.</p>
     *
     * <p>This method takes a country code and searches to find the
     * languages available for that country. Variant locales are removed.</p>
     *
     * @param countryCode  the 2 letter country code, null returns empty
     * @return an unmodifiable List of Locale objects, not null
     */
    public static List<Locale> languagesByCountry(final String countryCode) {
        if (countryCode == null) {
            return Collections.emptyList();
        }
        List<Locale> langs = cLanguagesByCountry.get(countryCode);
        if (langs == null) {
            langs = new ArrayList<Locale>();
            final List<Locale> locales = availableLocaleList();
            for (int i = 0; i < locales.size(); i++) {
                final Locale locale = locales.get(i);
                if (countryCode.equals(locale.getCountry()) &&
                        locale.getVariant().isEmpty()) {
                    langs.add(locale);
                }
            }
            langs = Collections.unmodifiableList(langs);
            cLanguagesByCountry.putIfAbsent(countryCode, langs);
            langs = cLanguagesByCountry.get(countryCode);
        }
        return langs;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Obtains the list of countries supported for a given language.</p>
     * 
     * <p>This method takes a language code and searches to find the
     * countries available for that language. Variant locales are removed.</p>
     *
     * @param languageCode  the 2 letter language code, null returns empty
     * @return an unmodifiable List of Locale objects, not null
     */
// -----------------------------------------------------------------------
/**
 * <p>Obtains the list of countries supported for a given language.</p>
 *
 * <p>This method takes a language code and searches to find the
 * countries available for that language. Variant locales are removed.</p>
 *
 * @param languageCode
 * 		the 2 letter language code, null returns empty
 * @return an unmodifiable List of Locale objects, not null
 */
public static java.util.List<java.util.Locale> countriesByLanguage(final java.lang.String languageCode) {
    {
        java.util.List<java.util.Locale> countries = org.apache.commons.lang3.LocaleUtils.cCountriesByLanguage.get(/* NPEX_NULL_EXP */
        languageCode);
        if (countries == null) {
            countries = new java.util.ArrayList<java.util.Locale>();
            final java.util.List<java.util.Locale> locales = org.apache.commons.lang3.LocaleUtils.availableLocaleList();
            for (int i = 0; i < locales.size(); i++) {
                final java.util.Locale locale = locales.get(i);
                if ((languageCode.equals(locale.getLanguage()) && (locale.getCountry().length() != 0)) && locale.getVariant().isEmpty()) {
                    countries.add(locale);
                }
            }
            countries = java.util.Collections.unmodifiableList(countries);
            org.apache.commons.lang3.LocaleUtils.cCountriesByLanguage.putIfAbsent(languageCode, countries);
            countries = org.apache.commons.lang3.LocaleUtils.cCountriesByLanguage.get(languageCode);
        }
        return countries;
    }
}

    //-----------------------------------------------------------------------
    // class to avoid synchronization (Init on demand)
    static class SyncAvoid {
        /** Unmodifiable list of available locales. */
        private static final List<Locale> AVAILABLE_LOCALE_LIST;
        /** Unmodifiable set of available locales. */
        private static final Set<Locale> AVAILABLE_LOCALE_SET;
        
        static {
            final List<Locale> list = new ArrayList<Locale>(Arrays.asList(Locale.getAvailableLocales()));  // extra safe
            AVAILABLE_LOCALE_LIST = Collections.unmodifiableList(list);
            AVAILABLE_LOCALE_SET = Collections.unmodifiableSet(new HashSet<Locale>(list));
        }
    }

}
