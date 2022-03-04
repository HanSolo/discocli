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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.LibCType;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.VersionNumber;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


public class Helper {
    // ******************** Constructors **************************************
    private Helper() {}


    // ******************** Methods *******************************************
    private static HttpClient httpClient;

    public static final Distribution getDistributionFromText(final String text) {
        if (null == text) { return null; }
        switch (text) {
            case "zulu":
            case "ZULU":
            case "Zulu":
            case "zulucore":
            case "ZULUCORE":
            case "ZuluCore":
            case "zulu_core":
            case "ZULU_CORE":
            case "Zulu_Core":
            case "zulu core":
            case "ZULU CORE":
            case "Zulu Core":
                return Distro.ZULU.get();
            case "zing":
            case "ZING":
            case "Zing":
            case "prime":
            case "PRIME":
            case "Prime":
            case "zuluprime":
            case "ZULUPRIME":
            case "ZuluPrime":
            case "zulu_prime":
            case "ZULU_PRIME":
            case "Zulu_Prime":
            case "zulu prime":
            case "ZULU PRIME":
            case "Zulu Prime":
                return Distro.ZULU_PRIME.get();
            case "aoj":
            case "AOJ":
                return Distro.AOJ.get();
            case "aoj_openj9":
            case "AOJ_OpenJ9":
            case "AOJ_OPENJ9":
            case "AOJ OpenJ9":
            case "AOJ OPENJ9":
            case "aoj openj9":
                return Distro.AOJ_OPENJ9.get();
            case "corretto":
            case "CORRETTO":
            case "Corretto":
                return Distro.CORRETTO.get();
            case "dragonwell":
            case "DRAGONWELL":
            case "Dragonwell":
                return Distro.DRAGONWELL.get();
            case "graalvm_ce8":
            case "graalvmce8":
            case "GraalVM CE 8":
            case "GraalVMCE8":
            case "GraalVM_CE8":
                return Distro.GRAALVM_CE8.get();
            case "graalvm_ce11":
            case "graalvmce11":
            case "GraalVM CE 11":
            case "GraalVMCE11":
            case "GraalVM_CE11":
                return Distro.GRAALVM_CE11.get();
            case "graalvm_ce16":
            case "graalvmce16":
            case "GraalVM CE 16":
            case "GraalVMCE16":
            case "GraalVM_CE16":
                return Distro.GRAALVM_CE16.get();
            case "graalvm_ce17":
            case "graalvmce17":
            case "GraalVM CE 17":
            case "GraalVMCE17":
            case "GraalVM_CE17":
                return Distro.GRAALVM_CE17.get();
            case "jetbrains":
            case "JetBrains":
            case "JETBRAINS":
                return Distro.JETBRAINS.get();
            case "liberica":
            case "LIBERICA":
            case "Liberica":
                return Distro.LIBERICA.get();
            case "liberica_native":
            case "LIBERICA_NATIVE":
            case "libericaNative":
            case "LibericaNative":
            case "liberica native":
            case "LIBERICA NATIVE":
            case "Liberica Native":
                return Distro.LIBERICA_NATIVE.get();
            case "mandrel":
            case "MANDREL":
            case "Mandrel":
                return Distro.MANDREL.get();
            case "microsoft":
            case "Microsoft":
            case "MICROSOFT":
            case "Microsoft OpenJDK":
            case "Microsoft Build of OpenJDK":
                return Distro.MICROSOFT.get();
            case "ojdk_build":
            case "OJDK_BUILD":
            case "OJDK Build":
            case "ojdk build":
            case "ojdkbuild":
            case "OJDKBuild":
                return Distro.OJDK_BUILD.get();
            case "openlogic":
            case "OPENLOGIC":
            case "OpenLogic":
            case "open_logic":
            case "OPEN_LOGIC":
            case "Open Logic":
            case "OPEN LOGIC":
            case "open logic":
                return Distro.OPEN_LOGIC.get();
            case "oracle":
            case "Oracle":
            case "ORACLE":
                return Distro.ORACLE.get();
            case "oracle_open_jdk":
            case "ORACLE_OPEN_JDK":
            case "oracle_openjdk":
            case "ORACLE_OPENJDK":
            case "Oracle_OpenJDK":
            case "Oracle OpenJDK":
            case "oracle openjdk":
            case "ORACLE OPENJDK":
            case "open_jdk":
            case "openjdk":
            case "OpenJDK":
            case "Open JDK":
            case "OPEN_JDK":
            case "open-jdk":
            case "OPEN-JDK":
            case "Oracle-OpenJDK":
            case "oracle-openjdk":
            case "ORACLE-OPENJDK":
            case "oracle-open-jdk":
            case "ORACLE-OPEN-JDK":
                return Distro.ORACLE_OPEN_JDK.get();
            case "sap_machine":
            case "sapmachine":
            case "SAPMACHINE":
            case "SAP_MACHINE":
            case "SAPMachine":
            case "SAP Machine":
            case "sap-machine":
            case "SAP-Machine":
            case "SAP-MACHINE":
                return Distro.SAP_MACHINE.get();
            case "semeru":
            case "Semeru":
            case "SEMERU":
                return Distro.SEMERU.get();
            case "semeru_certified":
            case "SEMERU_CERTIFIED":
            case "Semeru_Certified":
            case "Semeru_certified":
            case "semeru certified":
            case "SEMERU CERTIFIED":
            case "Semeru Certified":
            case "Semeru certified":
                return Distro.SEMERU_CERTIFIED.get();
            case "temurin":
            case "Temurin":
            case "TEMURIN":
                return Distro.TEMURIN.get();
            case "trava":
            case "TRAVA":
            case "Trava":
            case "trava_openjdk":
            case "TRAVA_OPENJDK":
            case "trava openjdk":
            case "TRAVA OPENJDK":
                return Distro.TRAVA.get();
            case "kona":
            case "KONA":
            case "Kona":
                return Distro.KONA.get();
            case "bisheng":
            case "BISHENG":
            case "BiSheng":
            case "bi_sheng":
            case "BI_SHENG":
            case "bi-sheng":
            case "BI-SHENG":
            case "bi sheng":
            case "Bi Sheng":
            case "BI SHENG":
                return Distro.BISHENG.get();
            default:
                return null;
        }
    }

    public static final List<Pkg> getPkgsForDistributionAndMajorVersion(final Distribution distribution, final int majorVersion, final OperatingSystem operatingSystem, final LibCType libcType, final Architecture architecture, final PackageType packageType, final ArchiveType archiveType, final boolean includeEA) {
        StringBuilder builder = new StringBuilder().append(Constants.DISCO_API_URL).append(Constants.PACKAGES_ENDPOINT).append("?distro=").append(distribution.apiString()).append("&version=").append(majorVersion);
        if (null != operatingSystem) {
            builder.append("&operating_system=").append(operatingSystem.getApiString());
            if (null != libcType) {
                builder.append("&lib_c_type=").append(libcType.getApiString());
            }
        }
        if (null != architecture)    { builder.append("&architecture=").append(architecture.getApiString()); }
        if (null != packageType)     { builder.append("&package_type=").append(packageType.getApiString()); }
        if (null != archiveType)     { builder.append("&archive_type=").append(archiveType.getApiString()); }
        builder.append(includeEA ? "&release_status=ea&release_status=ga" : "&release_status=ga");
        builder.append("&latest=all_of_version");

        String request = builder.toString();
        HttpResponse<String> response = get(request);
        if (null == response || response.statusCode() != 200 || null == response.body() || response.body().isEmpty()) {
            return List.of();
        }

        List<Pkg>   pkgs      = new LinkedList<>();
        String      bodyText  = response.body();
        Set<Pkg>    pkgsFound = new HashSet<>();
        Gson        gson      = new Gson();
        JsonElement element   = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            final JsonObject jsonObject = element.getAsJsonObject();
            final JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                pkgsFound.add(new Pkg(pkgJsonObj.toString()));
            }
        }
        pkgs.addAll(pkgsFound);
        Collections.sort(pkgs, Comparator.comparing(Pkg::getJavaVersion).reversed());
        return pkgs;
    }

    public static final List<String> readTextFileToList(final String filename) throws IOException {
        final Path           path   = Paths.get(filename);
        final BufferedReader reader = Files.newBufferedReader(path);
        return reader.lines().collect(Collectors.toList());
    }


    public static final TermOfSupport getTermOfSupport(final VersionNumber versionNumber) {
        if (!versionNumber.getFeature().isPresent() || versionNumber.getFeature().isEmpty()) {
            throw new IllegalArgumentException("VersionNumber need to have a feature version");
        }
        return getTermOfSupport(versionNumber.getFeature().getAsInt());
    }
    public static final TermOfSupport getTermOfSupport(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (isLTS(featureVersion)) {
            return TermOfSupport.LTS;
        } else if (isMTS(featureVersion)) {
            return TermOfSupport.MTS;
        } else if (isSTS(featureVersion)) {
            return TermOfSupport.STS;
        } else {
            return TermOfSupport.NOT_FOUND;
        }
    }
    public static final boolean isSTS(final int featureVersion) {
        if (featureVersion < 9) { return false; }
        switch(featureVersion) {
            case 9 :
            case 10: return true;
            default: return !isLTS(featureVersion);
        }
    }
    public static final boolean isMTS(final int featureVersion) {
        if (featureVersion < 13) { return false; }
        return (!isLTS(featureVersion)) && featureVersion % 2 != 0;
    }
    public static final boolean isLTS(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (featureVersion <= 8) { return true; }
        if (featureVersion < 11) { return false; }
        return ((featureVersion - 11.0) / 6.0) % 1 == 0;
    }



    // ******************** REST calls ****************************************
    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(20))
                         .version(Version.HTTP_2)
                         .followRedirects(Redirect.NORMAL)
                         //.executor(Executors.newFixedThreadPool(4))
                         .build();
    }

    public static final HttpResponse<String> get(final String uri) {
        if (null == httpClient) { httpClient = createHttpClient(); }
        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .setHeader("User-Agent", "DiscoCLI")
                                         .timeout(Duration.ofSeconds(10))
                                         .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            return response;
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }
}
