/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.discocli.util;

import java.util.List;
import java.util.stream.Collectors;

import static eu.hansolo.jdktools.Constants.COLON;
import static eu.hansolo.jdktools.Constants.COMMA;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_CLOSE;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_OPEN;
import static eu.hansolo.jdktools.Constants.QUOTES;
import static eu.hansolo.jdktools.Constants.QUOTES_COMMA_QUOTES;
import static eu.hansolo.jdktools.Constants.SQUARE_BRACKET_CLOSE_QUOTES;
import static eu.hansolo.jdktools.Constants.SQUARE_BRACKET_OPEN_QUOTES;


public record Distribution(String name, String uiString, String apiString, boolean maintained, List<String> synonyms, String spacer) {
    public static final String FIELD_NAME       = "name";
    public static final String FIELD_UI_STRING  = "ui_string";
    public static final String FIELD_API_STRING = "api_string";
    public static final String FIELD_MAINTAINED = "maintained";
    public static final String FIELD_SYNONYMS   = "synonyms";


    // ******************** Methods *******************************************
    public Distribution getFromText(final String text) {
        return synonyms.contains(text) ? Distribution.this : null;
    }

    @Override public String toString() {
        return new StringBuilder().append(CURLY_BRACKET_OPEN)
                                  .append(QUOTES).append(FIELD_NAME).append(QUOTES).append(COLON).append(QUOTES).append(name).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(FIELD_UI_STRING).append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(FIELD_API_STRING).append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(FIELD_MAINTAINED).append(QUOTES).append(COLON).append(QUOTES).append(maintained).append(COMMA)
                                  .append(QUOTES).append(FIELD_SYNONYMS).append(QUOTES).append(COLON).append(QUOTES).append(synonyms.stream().collect(
        Collectors.joining(QUOTES_COMMA_QUOTES, SQUARE_BRACKET_OPEN_QUOTES, SQUARE_BRACKET_CLOSE_QUOTES)))
                                  .append(CURLY_BRACKET_CLOSE)
                                  .toString();
    }
}
