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


public class Constants {
    private Constants() {}

    public static final String DISCO_API_URL             = "https://api.foojay.io/disco/v3.0/";
    public static final String DISTRIBUTIONS_ENDPOINT    = "distributions";
    public static final String MAJOR_VERSIONS_ENDPOINT   = "major_versions";
    public static final String PACKAGES_ENDPOINT         = "packages";
    public static final String IDS_ENDPOINT              = "ids/";
    public static final String DISTRIBUTION_JSON         = "distributions.json";
    public static final String FIELD_FILENAME            = "filename";
    public static final String FIELD_JAVA_VERSION        = "java_version";
    public static final String FIELD_DIRECT_DOWNLOAD_URI = "direct_download_uri";
    public static final String FIELD_DOWNLOAD_SITE_URI   = "download_site_uri";
    public static final String FIELD_SIGNATURE_URI       = "signature_uri";
    public static final String FIELD_CHECKSUM_URI        = "checksum_uri";
    public static final String FIELD_CHECKSUM            = "checksum";
    public static final String FIELD_CHECKSUM_TYPE       = "checksum_type";

    public static final String DEFAULT_ERROR_MSG         = "@|red \nError retrieving pkg info from Disco API |@ \n";
}
