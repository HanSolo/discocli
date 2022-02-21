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

package eu.hansolo.discocli;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.hansolo.discocli.util.Constants;
import eu.hansolo.discocli.util.Distribution;
import eu.hansolo.discocli.util.Distro;
import eu.hansolo.discocli.util.Helper;
import eu.hansolo.discocli.util.Pkg;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.util.OutputFormat;
import eu.hansolo.jdktools.versioning.VersionNumber;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static eu.hansolo.jdktools.OperatingSystem.WINDOWS;


@Command(
    name                     = "discocli",
    mixinStandardHelpOptions = true,
    description              = "Download a JDK pkg defined by the given parameters to the current folder",
    version                  = "17.0.0"
)
public class DiscoCLI implements Callable<Integer> {

    //@Parameters(index = "0", description = "The operating system of the requested JDK")
    //private OperatingSystem os;

    @Option(names = { "-i", "--info" }, description = "Info") boolean info;

    @Option(names = { "-os", "--operating-system" }, description = "Operating System (windows, linux, macos)")
    private String os = null;

    @Option(names = { "-arc", "--architecture" }, description = "Architecture (x64, aarch64)")
    private String arc = null;

    @Option(names = { "-v", "--version" }, description = "Version")
    private String v = null;

    @Option(names = { "-d", "--distribution" }, description = "Distribution")
    private String d = null;

    @Option(names = { "-pt", "--package-type" }, description = "Package type (jdk, jre)")
    private String pt = null;

    @Option(names = { "-at", "--archive-type" }, description = "Archive type (tar.gz, zip)")
    private String at = null;

    @Option(names = { "-fx", "--javafx" }, description = "Bundled with JavaFX") boolean fx;

    private void downloadPkg(final String url, final String filename, final long size) throws IOException {
        final URL downloadUrl = new URL(url);
        try (InputStream in  = downloadUrl.openStream()) {
            final OutputStream out = new FileOutputStream(filename);
            final byte data[] = new byte[4096];
            long total      = 0;
            int  count      = 0;
            int oldProgress = 0;
            while ((count = in.read(data)) != -1) {
                total += count;
                if (size > 0) {
                    int progress = (int) (total * 100 / size);
                    IntStream.range(0, (progress - oldProgress)).forEach(i -> System.out.print('.'));
                    oldProgress  = progress;
                }
                out.write(data, 0, count);
            }
        }
    }


    @Override public Integer call() throws Exception {
        if (info) {
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- DiscoCLI --------------- |@"));
            System.out.println("Supported parameters with their available values.");
            System.out.println("Keep in mind that not every distribution supports all operating systems, archive types, package types etc.");
            System.out.println();
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Distributions ---------- |@"));
            Distro.getAsListWithoutNoneAndNotFound().stream().sorted(Comparator.comparing(Distro::getUiString)).forEach(distro -> System.out.println(distro.getApiString() + distro.get().getSpacer() + "(" + distro.getUiString() + ")"));
            System.out.println();
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Operating systems ------ |@"));
            System.out.println("windows");
            System.out.println("linux");
            System.out.println("macos");
            System.out.println("alpine-linux");
            System.out.println();
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Architectures ---------- |@"));
            Architecture.getAsList().stream().filter(architecture -> Architecture.NONE != architecture).filter(architecture -> Architecture.NOT_FOUND != architecture).forEach(architecture -> System.out.println(architecture.getApiString()));
            System.out.println();
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Archive types ---------- |@"));
            ArchiveType.getAsList().stream().filter(archiveType -> ArchiveType.NONE != archiveType).filter(archiveType -> ArchiveType.NOT_FOUND != archiveType).forEach(archiveType -> System.out.println(archiveType.getApiString()));
            System.out.println();
            System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Package types ---------- |@"));
            PackageType.getAsList().stream().filter(packageType -> PackageType.NONE != packageType).filter(packageType -> PackageType.NOT_FOUND != packageType).forEach(packageType -> System.out.println(packageType.getApiString()));
            return 0;
        }

        // Parse distro
        final Distro parsedDistro = null == d ? Distro.ZULU : Distro.fromText(d);
        final Distro distro;
        if (Distro.NONE == parsedDistro || Distro.NOT_FOUND == parsedDistro) {
            distro = Distro.ZULU;
        } else {
            distro = parsedDistro;
        }
        if (Distro.NOT_FOUND == distro || Distro.NONE == distro) {
            System.out.println(Ansi.AUTO.string("@|red Distribution cannot be found |@"));
            return 1;
        }

        // Parse operating system
        final OperatingSystem parsedOperatingSystem = null == os ? eu.hansolo.toolbox.Helper.getOperatingSystem() : OperatingSystem.fromText(os);
        final OperatingSystem operatingSystem;
        if (OperatingSystem.NONE == parsedOperatingSystem || OperatingSystem.NOT_FOUND == parsedOperatingSystem) {
            operatingSystem = eu.hansolo.toolbox.Helper.getOperatingSystem();
        } else {
            operatingSystem = parsedOperatingSystem;
        }
        if (OperatingSystem.NOT_FOUND == operatingSystem || OperatingSystem.NONE == operatingSystem) {
            System.out.println(Ansi.AUTO.string("@|red Operating system cannot be found |@"));
            return 1;
        }

        // Parse architecture
        final Architecture parsedArchitecture = null == arc ? Architecture.X64 : Architecture.fromText(arc);
        final Architecture architecture;
        if (Architecture.NONE == parsedArchitecture || Architecture.NOT_FOUND == parsedArchitecture) {
            architecture = Architecture.X64;
        } else {
            architecture = parsedArchitecture;
        }
        if (Architecture.NONE == architecture || Architecture.NOT_FOUND == architecture) {
            System.out.println(Ansi.AUTO.string("@|red Architecture cannot be found |@"));
            return 1;
        }

        // Parse package type
        final PackageType parsedPackageType = null == pt ? PackageType.JDK : PackageType.fromText(pt);
        final PackageType packageType;
        if (PackageType.NONE == parsedPackageType || PackageType.NOT_FOUND == parsedPackageType) {
            packageType = PackageType.JDK;
        } else {
            packageType = parsedPackageType;
        }
        if (PackageType.NOT_FOUND == packageType || PackageType.NONE == packageType) {
            System.out.println(Ansi.AUTO.string("@|red Package type cannot be found |@"));
            return 1;
        }

        // Parse archive type
        final ArchiveType parsedArchiveType = null == at ? (WINDOWS == operatingSystem ? ArchiveType.ZIP : ArchiveType.TAR_GZ) : ArchiveType.fromText(at);
        final ArchiveType archiveType;
        if (ArchiveType.NONE == parsedArchiveType || ArchiveType.NOT_FOUND == parsedArchiveType) {
            archiveType = (WINDOWS == operatingSystem ? ArchiveType.ZIP : ArchiveType.TAR_GZ);
        } else {
            archiveType = parsedArchiveType;
        }
        if (ArchiveType.NOT_FOUND == archiveType || ArchiveType.NONE == archiveType) {
            System.out.println(Ansi.AUTO.string("@|red Archive type cannot be found |@"));
            return 1;
        }

        final String distributionParam         = "?distro=" + distro.getApiString();
        final String operatingSystemParam      = "&operating_system=" + operatingSystem.getApiString();
        final String versionParam              = null == v ? "&latest=available" : "&version=" + VersionNumber.fromText(v).toString(OutputFormat.FULL_COMPRESSED, true, false);
        final String archiveTypeParam          = "&archive_type=" + archiveType.getApiString();
        final String javafxBundledParam        = fx ? "&javafx_bundled=true" : "";
        final String packageTypeParam          = "&package_type=" + packageType.getApiString();
        final String directlyDownloadableParam = "&directlyDownloadable=true";
        final String libCTypeParam             = "&lib_c_type=" + operatingSystem.getLibCType().getApiString();
        final String architectureParam         = "&architecture=" + architecture.getApiString();

        final String request = new StringBuilder().append(Constants.DISCO_API_URL)
                                                  .append(Constants.PACKAGES_ENDPOINT)
                                                  .append(distributionParam)
                                                  .append(operatingSystemParam)
                                                  .append(libCTypeParam)
                                                  .append(architectureParam)
                                                  .append(versionParam)
                                                  .append(archiveTypeParam)
                                                  .append(javafxBundledParam)
                                                  .append(packageTypeParam)
                                                  .append(directlyDownloadableParam)
                                                  .toString();

        HttpResponse<String> response = Helper.get(request, Constants.USER_AGENT);
        if (null == response) {
            System.out.println(Ansi.AUTO.string("@|red Error retrieving pkg info from Disco API |@"));
            return 1;
        } else if (response.statusCode() != 200) {
            switch(response.statusCode()) {
                case 400 -> {
                    System.out.println(Ansi.AUTO.string("@|red Sorry, defined pkg not found in Disco API |@"));
                    // TODO: Load all available pkgs for selected distribution and major version
                    //Helper.getPkgsForDistributionAndMajorVersion()

                }
                default  -> System.out.println(Ansi.AUTO.string("@|red Error retrieving pkg info from Disco API |@"));
            }
            return 1;
        } else {
            List<Pkg>   pkgs      = new LinkedList<>();
            Set<Pkg>    pkgsFound = new HashSet<>();
            Gson        gson      = new Gson();
            JsonElement element   = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray jsonArray = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                    pkgsFound.add(new Pkg(pkgJsonObj.toString()));
                }
            }
            pkgs.addAll(pkgsFound);
            if (pkgs.isEmpty()) {
                System.out.println(Ansi.AUTO.string("@|red Sorry, defined pkg not found in Disco API |@"));
                return 1;
            }

            // Get first package found
            Pkg pkg = pkgs.get(0);

            // Get direct download link
            String               urlRequest  = new StringBuilder().append(Constants.DISCO_API_URL).append(Constants.IDS_ENDPOINT).append(pkg.getId()).toString();
            HttpResponse<String> urlResponse = Helper.get(urlRequest, Constants.USER_AGENT);
            if (null == urlResponse) {
                System.out.println(Ansi.AUTO.string("@|red Error retrieving pkg info from Disco API |@"));
                return 1;
            } else if (urlResponse.statusCode() != 200) {
                System.out.println(Ansi.AUTO.string("@|red Error retrieving pkg info from Disco API with status code " + response.statusCode() + " |@"));
                return 1;
            } else {
                String      packageInfoBody    = urlResponse.body();
                Gson        packageInfoGson    = new Gson();
                JsonElement packageInfoElement = packageInfoGson.fromJson(packageInfoBody, JsonElement.class);
                if (packageInfoElement instanceof JsonObject) {
                    JsonObject jsonObject = packageInfoElement.getAsJsonObject();
                    JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                    if (jsonArray.size() > 0) {
                        final JsonObject    packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                        final String        filename          = packageInfoJson.has(Constants.FIELD_FILENAME)            ? packageInfoJson.get(Constants.FIELD_FILENAME).getAsString()                              : "";
                        final String        directDownloadUri = packageInfoJson.has(Constants.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(Constants.FIELD_DIRECT_DOWNLOAD_URI).getAsString()                   : "";
                        //final String        signatureUri      = packageInfoJson.has(Constants.FIELD_SIGNATURE_URI)       ? packageInfoJson.get(Constants.FIELD_SIGNATURE_URI).getAsString()                         : "";
                        //final String        checksumUri       = packageInfoJson.has(Constants.FIELD_CHECKSUM_URI)        ? packageInfoJson.get(Constants.FIELD_CHECKSUM_URI).getAsString()                          : "";
                        //final String        checksum          = packageInfoJson.has(Constants.FIELD_CHECKSUM)            ? packageInfoJson.get(Constants.FIELD_CHECKSUM).getAsString()                              : "";
                        //final HashAlgorithm checksumType      = packageInfoJson.has(Constants.FIELD_CHECKSUM_TYPE) ? HashAlgorithm.fromText(packageInfoJson.get(Constants.FIELD_CHECKSUM_TYPE).getAsString()) : HashAlgorithm.NONE;
                        if (null == filename) { return 1; }
                        System.out.println("Downloading " + pkg.getFileName() + ":");
                        try {
                            downloadPkg(directDownloadUri, pkg.getFileName(), pkg.getSize());
                            System.out.println(Ansi.AUTO.string("@|green \nSuccessfully dowloaded JDK pkg |@"));

                            return 0;
                        } catch (IOException e) {
                            System.out.println(Ansi.AUTO.string("@|red \nError downloading " + pkg.getFileName() + " from " + directDownloadUri + " |@"));
                            return 1;
                        }
                    } else {
                        System.out.println(Ansi.AUTO.string("@|red Error retrieving direct download uri |@"));
                        return 1;
                    }
                }
            }
        }
        return 1;
    }


    public static void main(final String... args) {
        if (null == args || args.length == 0) {
            System.exit(0);
        }

        int exitCode = new CommandLine(new DiscoCLI()).execute(args);
        System.exit(exitCode);
    }
}
