/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Equals pattern converter.
 */
@Plugin(name = "equals", category = PatternConverter.CATEGORY)
@ConverterKeys({ "equals" })
public final class EqualsReplacementConverter extends LogEventPatternConverter {

    /**
     * Gets an instance of the class.
     *
     * @param config  The current Configuration.
     * @param options pattern options, an array of three elements: pattern, testString, and substitution.
     * @return instance of class.
     */
    public static EqualsReplacementConverter newInstance(final Configuration config, final String[] options) {
        if (options.length != 3) {
            LOGGER.error("Incorrect number of options on equals. Expected 3 received " + options.length);
            return null;
        }
        if (options[0] == null) {
            LOGGER.error("No pattern supplied on equals");
            return null;
        }
        if (options[1] == null) {
            LOGGER.error("No test string supplied on equals");
            return null;
        }
        if (options[2] == null) {
            LOGGER.error("No substitution supplied on equals");
            return null;
        }
        final String p = options[1];
        final PatternParser parser = PatternLayout.createPatternParser(config);
        final List<PatternFormatter> formatters = parser.parse(options[0]);
        return new EqualsReplacementConverter(formatters, p, options[2], parser);
    }

    private final List<PatternFormatter> formatters;
    private final List<PatternFormatter> substitutionFormatters;
    private final String substitution;
    private final String testString;

    /**
     * Construct the converter.
     *
     * @param formatters   The PatternFormatters to generate the text to manipulate.
     * @param testString   The test string.
     * @param substitution The substitution string.
     * @param parser       The PatternParser.
     */
    private EqualsReplacementConverter(final List<PatternFormatter> formatters, final String testString,
                                       final String substitution, final PatternParser parser) {
        super("equals", "equals");
        this.testString = testString;
        this.substitution = substitution;
        this.formatters = formatters;

        // check if substitution needs to be parsed
        substitutionFormatters = substitution.contains("%") ? parser.parse(substitution) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final int initialSize = toAppendTo.length();
        for (int i = 0; i < formatters.size(); i++) {
            final PatternFormatter formatter = formatters.get(i);
            formatter.format(event, toAppendTo);
        }
        if (equals(testString, toAppendTo, initialSize, toAppendTo.length() - initialSize)) {
            toAppendTo.setLength(initialSize);
            parseSubstitution(event, toAppendTo);
        }
    }

    private static boolean equals(String str, StringBuilder buff, int from, int len) {
        if (str.length() == len) {
            for (int i = 0; i < len; i++) {
                if (str.charAt(i) != buff.charAt(i + from)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds the parsed substitution text to the specified buffer.
     *
     * @param event the current log event
     * @param substitutionBuffer the StringBuilder to append the parsed substitution text to
     */
    void parseSubstitution(final LogEvent event, final StringBuilder substitutionBuffer) {
        if (substitutionFormatters != null) {
            for (int i = 0; i < substitutionFormatters.size(); i++) {
                final PatternFormatter formatter = substitutionFormatters.get(i);
                formatter.format(event, substitutionBuffer);
            }
        } else {
            substitutionBuffer.append(substitution);
        }
    }
}
