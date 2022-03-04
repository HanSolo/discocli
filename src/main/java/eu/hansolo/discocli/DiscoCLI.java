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
import eu.hansolo.discocli.util.Detector;
import eu.hansolo.discocli.util.Distro;
import eu.hansolo.discocli.util.Helper;
import eu.hansolo.discocli.util.Pkg;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.LibCType;
import eu.hansolo.jdktools.OperatingMode;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.util.Helper.OsArcMode;
import eu.hansolo.jdktools.util.OutputFormat;
import eu.hansolo.jdktools.versioning.VersionNumber;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.hansolo.jdktools.OperatingSystem.WINDOWS;


@Command(
    name        = "discocli",
    description = "Download a JDK pkg defined by the given parameters",
    version     = "17.0.6"
)
public class DiscoCLI implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, description = "Help") boolean help;

    @Option(names = { "-i", "--info" }, description = "Info") boolean info;

    @Option(names = { "-f", "--find" }, description = "Find available JDK pkgs for given parameters") boolean find;

    @Option(names = { "-os", "--operating-system" }, description = "Operating System (windows, linux, macos)")
    private String os = null;

    @Option(names = { "-lc", "--libc-type" }, description = "Lib C type (libc, glibc, c_std_lib, musl)")
    private String lc = null;

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

    @Option(names = { "-p", "--path" }, description = "The path where the JDK pkg should be saved to")
    private String p = null;

    @Option(names = { "-fd", "--find-distros" }, description = "Find distributions in given path (e.g. \"./\")")
    private String fd = null;

    @Option(names = { "-fu", "--update-available" }, description = "Find update for given distribution DISTRO,VERSION,OPERATING SYSTEM,ARCHITECTURE,PACKAGE TYPE,FX(optional) (e.g. \"zulu,16.0.1,macos,aarch64,jdk\")")
    private String fu = null;

    @Option(names = { "-ea", "--early-access" }, description = "Include early access builds") boolean ea;

    @Option(names = { "-fx", "--javafx" }, description = "Bundled with JavaFX") boolean fx;

    @Option(names = { "-latest" }, description = "Latest available for given version number") boolean latest;

    private int downloadPkg(final String url, final String filename, final long size) {
        final Path path = Paths.get(filename);
        if (Files.exists(path)) { return 2; }

        try {
            final URL downloadUrl = new URL(url);
            try (InputStream in = downloadUrl.openStream()) {
                try (OutputStream out = new FileOutputStream(filename)) {
                    final byte data[]      = new byte[4096];
                    long       total       = 0;
                    int        count       = 0;
                    int        oldProgress = 0;
                    while ((count = in.read(data)) != -1) {
                        total += count;
                        if (size > 0) {
                            int progress = (int) (total * 100 / size);
                            IntStream.range(0, (progress - oldProgress)).forEach(i -> System.out.print('.'));
                            oldProgress = progress;
                        }
                        out.write(data, 0, count);
                    }
                }
            }
            return 0;
        } catch (IOException e) {
            return 1;
        }
    }


    @Override public Integer call() {
        try {
            if (help) {
                final String white  = "@|white";
                final String yellow = "@|yellow";
                final String end    = "|@";

                StringBuilder helpBuilder1 = new StringBuilder().append("\nusage ").append(white).append(",bold").append(" discocli").append(end).append(" ")
                                                                .append("[").append(yellow).append(" -d").append(end).append("=<d>]").append(" ")
                                                                .append("[").append(yellow).append(" -v").append(end).append("=<v>]").append(" ")
                                                                .append("[").append(yellow).append(" -os").append(end).append("=<os>]").append(" ")
                                                                .append("[").append(yellow).append(" -lc").append(end).append("=<lc>]").append(" ")
                                                                .append("[").append(yellow).append(" -arc").append(end).append("=<arc>]").append(" ")
                                                                .append("[").append(yellow).append(" -at").append(end).append("=<at>]").append(" ")
                                                                .append("[").append(yellow).append(" -pt").append(end).append("=<pt>]").append(" ")
                                                                .append("[").append(yellow).append(" -p").append(end).append("=<p>]").append(" ")
                                                                .append("[").append(yellow).append(" -ea").append(end).append("]").append(" ")
                                                                .append("[").append(yellow).append(" -fx").append(end).append("]").append(" ")
                                                                .append("[").append(yellow).append(" -latest").append(end).append("]").append(" ")
                                                                .append("[").append(yellow).append(" -i").append(end).append("]").append(" ")
                                                                .append("[").append(yellow).append(" -f").append(end).append("]").append(" ")
                                                                .append("[").append(" -fd").append(end).append("=<fd>]")
                                                                .append("[").append(" -fu").append(end).append("=<fu>]");
                StringBuilder helpBuilder2 = new StringBuilder().append("\nDownload a JDK pkg defined by the given parameters").append("\n")
                                                                .append(yellow).append(" -arc, --architecture").append(end).append("=<arc> Architecture (e.g. x64, aarch64)").append("\n")
                                                                .append(yellow).append(" -at,  --archive-type").append(end).append("=<at> Archive tpye (e.g. tar.gz, zip)").append("\n")
                                                                .append(yellow).append(" -d,   --distribution").append(end).append("=<d> Distribution (e.g. zulu, temurin, etc.)").append("\n")
                                                                .append(yellow).append(" -ea,  --early-access").append(end).append(" Include early access builds").append("\n")
                                                                .append(yellow).append(" -f,   --find").append(end).append(" Find available JDK pkgs for given parameters").append("\n")
                                                                .append(yellow).append(" -fx,  --javafx").append(end).append(" Bundled with JavaFX").append("\n")
                                                                .append(yellow).append(" -i,   --info").append(end).append(" Info about parameters").append("\n")
                                                                .append(yellow).append(" -latest").append(end).append(" Latest available for given version number").append("\n")
                                                                .append(yellow).append(" -lc,  --libc-type").append(end).append("=<lc> Lib C type (libc, glibc, c_std_lib, musl)").append("\n")
                                                                .append(yellow).append(" -os,  --operating-system").append(end).append("=<os> Operating system (e.g. windows, linux, macos)").append("\n")
                                                                .append(yellow).append(" -p,   --path").append(end).append("=<pt> The path where the JDK pkg should be saved to (e.g. /User/hansolo").append("\n")
                                                                .append(yellow).append(" -pt,  --package-type").append(end).append("=<pt> Package type (e.g. jdk, jre)").append("\n")
                                                                .append(yellow).append(" -fd,  --find-distros").append(end).append("=<fd> Find distributions in given path (e.g. \"./\")").append("\n")
                                                                .append(yellow).append(" -fu,  --find-update").append(end).append("=<fu> Find update for given distribution DISTRO,VERSION,OPERATING SYSTEM,ARCHITECTURE,PACKAGE TYPE,FX(optional) (e.g. \"zulu,16.0.1,macos,aarch64,jdk\")").append("\n")
                                                                .append(yellow).append(" -v,   --version").append(end).append("=<v> Version (e.g. 17.0.2)").append("\n");

                System.out.println(Ansi.AUTO.string(helpBuilder1.toString()));
                System.out.println(Ansi.AUTO.string(helpBuilder2.toString()));
                return 0;
            }

            if (info) {
                System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- DiscoCLI --------------- |@"));
                System.out.println("Supported parameters with their available values.");
                System.out.println("Keep in mind that not every distribution supports all operating systems, archive types, package types etc.");
                System.out.println();
                System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Distributions ---------- |@"));
                Distro.getAsListWithoutNoneAndNotFound().stream().sorted(Comparator.comparing(Distro::getUiString)).forEach(distro -> System.out.println(distro.getApiString() + distro.get().spacer() + "(" + distro.getUiString() + ")"));
                System.out.println();
                System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Operating systems ------ |@"));
                System.out.println("windows");
                System.out.println("linux");
                System.out.println("macos");
                System.out.println("alpine-linux");
                System.out.println();
                System.out.println(Ansi.AUTO.string("@|bold,cyan ---------- Lib C types ------------ |@"));
                LibCType.getAsList().stream().filter(libctype -> LibCType.NONE != libctype).filter(libctype -> LibCType.NOT_FOUND != libctype).forEach(libctype -> System.out.println(libctype.getApiString()));
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

            if (null != fd) {
                final Detector detector = new Detector();
                detector.detectDistributions(fd.split(","));
                return 0;
            }

            if (null != fu) {
                if (null == fu || fu.isEmpty()) {
                    System.out.println(Ansi.AUTO.string("@|red \nPlease specify the distribution you would like to check e.g. zulu,16.0.1,macos,x64,jdk,fx |@ \n"));
                    return 1;
                }
                String[] parts = fu.split(",");
                if (parts.length < 5) {
                    System.out.println(Ansi.AUTO.string("@|red \nPlease specify the distribution you would like to check e.g. zulu,16.0.1,macos,x64,jdk,fx |@ \n"));
                    return 1;
                }

                Distro          distro          = Distro.fromText(parts[0]);
                VersionNumber   versionNumber   = VersionNumber.fromText(parts[1]);
                OperatingSystem operatingSystem = OperatingSystem.fromText(parts[2]);
                Architecture    architecture    = Architecture.fromText(parts[3]);
                PackageType     packageType     = PackageType.fromText(parts[4]);

                if (Distro.NOT_FOUND          == distro          ||
                    OperatingSystem.NOT_FOUND == operatingSystem ||
                    Architecture.NOT_FOUND    == architecture    ||
                    PackageType.NOT_FOUND     == packageType) {
                    System.out.println(Ansi.AUTO.string("@|red \nPlease check your parameters e.g. DISTRO,VERSION,OPERATING SYSTEM,ARCHITECTURE,PACKAGE TYPE,FX(optional) |@ \n"));
                    return 1;
                }

                boolean         earlyAccess     = ReleaseStatus.EA == versionNumber.getReleaseStatus().orElse(ReleaseStatus.GA);
                boolean         javafxBundled;
                if (parts.length == 6) {
                    javafxBundled = parts[5].equalsIgnoreCase("fx");
                } else {
                    javafxBundled = false;
                }

                List<Pkg> pkgs = Helper.getPkgsForDistributionAndMajorVersion(distro.get(), versionNumber.getFeature().getAsInt(), operatingSystem, operatingSystem.getLibCType(), architecture, packageType, null, earlyAccess);

                List<Pkg> updates;
                if (earlyAccess) {
                    updates = pkgs.stream()
                                  .filter(pkg -> pkg.isJavaFXBundled() == javafxBundled)
                                  .filter(pkg -> pkg.getJavaVersion().getVersionNumber().compareTo(versionNumber) > 0)
                                  .collect(Collectors.toList());
                } else {
                    VersionNumber withoutBuild = VersionNumber.fromText(versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, false));
                    updates = pkgs.stream()
                                  .filter(pkg -> pkg.isJavaFXBundled() == javafxBundled)
                                  .filter(pkg -> VersionNumber.fromText(pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, false)).compareTo(withoutBuild) > 0)
                                  .collect(Collectors.toList());
                }
                if (updates.isEmpty()) {
                    System.out.println(Ansi.AUTO.string("@|cyan \nNo update found |@ \n"));
                } else {
                    System.out.println(Ansi.AUTO.string("@|cyan \nUpdates found |@ \n"));
                    updates.forEach(pkg -> System.out.println(pkg.toCliString()));
                }
                return 0;
            }

            final OsArcMode sysInfo = eu.hansolo.jdktools.util.Helper.getOperaringSystemArchitectureOperatingMode();

            // Parse distro
            final Distro parsedDistro = null == d ? Distro.ZULU : Distro.fromText(d);
            final Distro distro;
            if (Distro.NONE == parsedDistro || Distro.NOT_FOUND == parsedDistro) {
                distro = Distro.ZULU;
            } else {
                distro = parsedDistro;
            }
            if (Distro.NOT_FOUND == distro || Distro.NONE == distro) {
                System.out.println(Ansi.AUTO.string("@|red \nDistribution cannot be found |@ \n"));
                return 1;
            }

            // Parse operating system
            final OperatingSystem parsedOperatingSystem = null == os ? sysInfo.operatingSystem() : OperatingSystem.fromText(os);
            final OperatingSystem operatingSystem;
            if (find && null == os) {
                operatingSystem = OperatingSystem.NONE;
            } else if (OperatingSystem.NONE == parsedOperatingSystem || OperatingSystem.NOT_FOUND == parsedOperatingSystem) {
                operatingSystem = sysInfo.operatingSystem();
            } else {
                operatingSystem = parsedOperatingSystem;
            }
            if (!find && (OperatingSystem.NOT_FOUND == operatingSystem || OperatingSystem.NONE == operatingSystem)) {
                System.out.println(Ansi.AUTO.string("@|red \nOperating system cannot be found |@ \n"));
                return 1;
            }

            // Parse lib c type
            final LibCType parsedLibcType = null == lc ? operatingSystem.getLibCType() : LibCType.fromText(lc);
            final LibCType libcType;
            if (find && OperatingSystem.NONE == operatingSystem) {
                libcType = LibCType.NONE;
            } else if (LibCType.NONE == parsedLibcType || LibCType.NOT_FOUND == parsedLibcType) {
                libcType = operatingSystem.getLibCType();
            } else {
                libcType = parsedLibcType;
            }
            if (!find && (LibCType.NONE == libcType || LibCType.NOT_FOUND == libcType)) {
                System.out.println(Ansi.AUTO.string("@|red \nLib C type cannot be found |@ \n"));
                return 1;
            }

            // Parse architecture
            final boolean      rosetta2           = OperatingSystem.MACOS == sysInfo.operatingSystem() && OperatingMode.EMULATED == sysInfo.operatingMode();
            final Architecture parsedArchitecture = null == arc ? rosetta2 ? Architecture.AARCH64 : sysInfo.architecture() : Architecture.fromText(arc);
            final Architecture architecture;
            if (find && null == arc) {
                architecture = Architecture.NONE;
            } else if (Architecture.NONE == parsedArchitecture || Architecture.NOT_FOUND == parsedArchitecture) {
                architecture = Architecture.X64;
            } else {
                architecture = parsedArchitecture;
            }
            if (!find && (Architecture.NONE == architecture || Architecture.NOT_FOUND == architecture)) {
                System.out.println(Ansi.AUTO.string("@|red \nArchitecture cannot be found |@ \n"));
                return 1;
            }

            // Parse package type
            final PackageType parsedPackageType = null == pt ? PackageType.JDK : PackageType.fromText(pt);
            final PackageType packageType;
            if (find && null == pt) {
                packageType = PackageType.NONE;
            } else if (PackageType.NONE == parsedPackageType || PackageType.NOT_FOUND == parsedPackageType) {
                packageType = PackageType.JDK;
            } else {
                packageType = parsedPackageType;
            }
            if (!find && (PackageType.NOT_FOUND == packageType || PackageType.NONE == packageType)) {
                System.out.println(Ansi.AUTO.string("@|red \nPackage type cannot be found |@ \n"));
                return 1;
            }

            // Parse archive type
            final ArchiveType parsedArchiveType = null == at ? (WINDOWS == operatingSystem ? ArchiveType.ZIP : ArchiveType.TAR_GZ) : ArchiveType.fromText(at);
            final ArchiveType archiveType;
            if (find && null == at) {
                archiveType = ArchiveType.NONE;
            } else if (ArchiveType.NONE == parsedArchiveType || ArchiveType.NOT_FOUND == parsedArchiveType) {
                archiveType = (WINDOWS == operatingSystem ? ArchiveType.ZIP : ArchiveType.TAR_GZ);
            } else {
                archiveType = parsedArchiveType;
            }
            if (!find && (ArchiveType.NOT_FOUND == archiveType || ArchiveType.NONE == archiveType)) {
                System.out.println(Ansi.AUTO.string("@|red \nArchive type cannot be found |@ \n"));
                return 1;
            }

            // Parse path
            final String parsedPath = null == p ? null : (p.endsWith(File.separator) ? p : p + File.separator);
            final Path path;
            if (null != parsedPath) {
                path = Paths.get(parsedPath);
                if (!Files.exists(path)) {
                    System.out.println(Ansi.AUTO.string("@|red \nGiven path does not exists |@ \n"));
                    return 1;
                }
                if (!Files.isDirectory(path)) {
                    System.out.println(Ansi.AUTO.string("@|red \nGiven path is not a folder |@ \n"));
                    return 1;
                }
                if (!Files.isWritable(path)) {
                    System.out.println(Ansi.AUTO.string("@|red \nNo rights to write to given path |@ \n"));
                    return 1;
                }
            }

            // Parse version number
            VersionNumber versionNumber;
            if (null == v) {
                versionNumber = null;
            } else {
                if (v.startsWith("-")) {
                    System.out.println(Ansi.AUTO.string("@|red \nVersion number cannot be negative |@ \n"));
                    return 1;
                }
                try {
                    versionNumber = VersionNumber.fromText(v);
                } catch (IllegalArgumentException e) {
                    versionNumber = null;
                    System.out.println(Ansi.AUTO.string("@|red \nVersion number cannot be parsed |@ \n"));
                    return 1;
                }
            }

            if (null == versionNumber && latest) {
                System.out.println(Ansi.AUTO.string("@|red \n -latest only works with a given version number (e.g. -v 17 -latest) |@ \n"));
                return 1;
            }
            if (null == v && find) {
                System.out.println(Ansi.AUTO.string("@|red \n -find only works with a given version number and distribution (e.g. -d zulu -v 17) |@ \n"));
                return 1;
            }
            if ((null == d || Distro.NONE == distro || Distro.NOT_FOUND == distro) && find) {
                System.out.println(Ansi.AUTO.string("@|red \n -find only works with a given version number and distribution (e.g. -d zulu -v 17) |@ \n"));
                return 1;
            }

            final boolean majorVersionOnly = null != versionNumber && (versionNumber.getInterim().getAsInt() == 0 && versionNumber.getUpdate().getAsInt() == 0 && versionNumber.getPatch().getAsInt() == 0);

            final String distributionParam         = "?distro=" + distro.getApiString();
            final String versionParam              = null == versionNumber ? "" : "&version=" + URLEncoder.encode(versionNumber.toString(OutputFormat.FULL_COMPRESSED, true, true), StandardCharsets.UTF_8);
            final String operatingSystemParam      = OperatingSystem.NONE == operatingSystem ? "" : "&operating_system=" + operatingSystem.getApiString();
            final String libcTypeParam             = LibCType.NONE == libcType ? "" : "&lib_c_type=" + libcType.getApiString();
            final String architectureParam         = Architecture.NONE == architecture ? "" : "&architecture=" + architecture.getApiString();
            final String archiveTypeParam          = ArchiveType.NONE == archiveType ? "" : "&archive_type=" + archiveType.getApiString();
            final String packageTypeParam          = PackageType.NONE == packageType ? "" : "&package_type=" + packageType.getApiString();
            final String latestParam               = find ? (majorVersionOnly ? "&latest=all_of_version" : "") : ((null == versionNumber || latest) ? "&latest=available" : "");
            final String javafxBundledParam        = fx ? "&javafx_bundled=true" : "";
            final String releaseStatusParam        = ea ? "&release_status=ea&release_status=ga" : "&release_status=ga";
            final String directlyDownloadableParam = "&directlyDownloadable=true";

            final String request = new StringBuilder().append(Constants.DISCO_API_URL)
                                                      .append(Constants.PACKAGES_ENDPOINT)
                                                      .append(distributionParam)
                                                      .append(operatingSystemParam)
                                                      .append(libcTypeParam)
                                                      .append(architectureParam)
                                                      .append(versionParam)
                                                      .append(latestParam)
                                                      .append(archiveTypeParam)
                                                      .append(javafxBundledParam)
                                                      .append(packageTypeParam)
                                                      .append(directlyDownloadableParam)
                                                      .append(releaseStatusParam)
                                                      .toString();

            HttpResponse<String> response = Helper.get(request);
            if (null == response) {
                System.out.println(Ansi.AUTO.string(Constants.DEFAULT_ERROR_MSG));
                return 1;
            } else if (response.statusCode() != 200) {
                if (400 == response.statusCode()) {
                    System.out.println(Ansi.AUTO.string("@|red \nSorry, defined pkg not found in Disco API |@ \n"));
                    if (null != versionNumber) {
                        List<Pkg> availablePkgs = Helper.getPkgsForDistributionAndMajorVersion(distro.get(), versionNumber.getFeature().getAsInt(), operatingSystem, libcType, architecture, packageType, archiveType, ea);
                        System.out.println(Ansi.AUTO.string("@|cyan,bold \nPackages available for " + distro.getUiString() + " for version " + versionNumber.getFeature().getAsInt() + ": |@"));
                        availablePkgs.stream()
                                     .sorted(Comparator.comparing(Pkg::getOperatingSystem).thenComparing(Pkg::getJavaVersion).reversed().thenComparing(Pkg::getArchitecture).thenComparing(Pkg::getArchiveType).thenComparing(Pkg::getPackageType))
                                     .forEach(pkg -> System.out.println(pkg.toCliString()));
                        System.out.println();
                    }
                } else {
                    System.out.println(Ansi.AUTO.string(Constants.DEFAULT_ERROR_MSG));
                }
                return 1;
            } else {
                List<Pkg>   pkgs      = new LinkedList<>();
                Set<Pkg>    pkgsFound = new HashSet<>();
                Gson        gson      = new Gson();
                JsonElement element   = gson.fromJson(response.body(), JsonElement.class);
                if (element instanceof JsonObject) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                        pkgsFound.add(new Pkg(pkgJsonObj.toString()));
                    }
                }
                pkgs.addAll(pkgsFound);
                if (pkgs.isEmpty()) {
                    System.out.println(Ansi.AUTO.string("@|red \nSorry, defined pkg not found in Disco API |@ \n"));
                    if (null != versionNumber) {
                        List<Pkg> availablePkgs = Helper.getPkgsForDistributionAndMajorVersion(distro.get(), versionNumber.getFeature().getAsInt(), operatingSystem, libcType, architecture, packageType, archiveType, ea);
                        System.out.println(Ansi.AUTO.string("@|cyan,bold \nPackages available for " + distro.getUiString() + " for version " + versionNumber.getFeature().getAsInt() + ": |@"));
                        availablePkgs.stream()
                                     .sorted(
                                     Comparator.comparing(Pkg::getOperatingSystem).thenComparing(Pkg::getJavaVersion).reversed().thenComparing(Pkg::getArchitecture).thenComparing(Pkg::getArchiveType).thenComparing(Pkg::getPackageType))
                                     .forEach(pkg -> System.out.println(pkg.toCliString()));
                        System.out.println();
                    }
                    return 1;
                }

                if (find) {
                    System.out.println(Ansi.AUTO.string("@|cyan,bold \nPackages found for " + distro.getUiString() + " for version " + versionNumber.getFeature().getAsInt() + ": |@"));
                    pkgs.stream().sorted(Comparator.comparing(Pkg::getOperatingSystem).thenComparing(Pkg::getJavaVersion).reversed().thenComparing(Pkg::getArchitecture).thenComparing(Pkg::getArchiveType).thenComparing(Pkg::getPackageType)).forEach(pkg -> System.out.println(pkg.toCliString()));
                    System.out.println();
                    return 1;
                } else {
                    Collections.sort(pkgs, Comparator.comparing(Pkg::getJavaVersion).reversed());
                }

                // Get first package found
                Pkg pkg = pkgs.get(0);

                // Get direct download link
                String               urlRequest  = new StringBuilder().append(Constants.DISCO_API_URL).append(Constants.IDS_ENDPOINT).append(pkg.getId()).toString();
                HttpResponse<String> urlResponse = Helper.get(urlRequest);
                if (null == urlResponse) {
                    System.out.println(Ansi.AUTO.string(Constants.DEFAULT_ERROR_MSG));
                    return 1;
                } else if (urlResponse.statusCode() != 200) {
                    System.out.println(Ansi.AUTO.string("@|red \nError retrieving pkg info from Disco API with status code " + response.statusCode() + " |@ \n"));
                    return 1;
                } else {
                    String      packageInfoBody    = urlResponse.body();
                    Gson        packageInfoGson    = new Gson();
                    JsonElement packageInfoElement = packageInfoGson.fromJson(packageInfoBody, JsonElement.class);
                    if (packageInfoElement instanceof JsonObject) {
                        JsonObject jsonObject = packageInfoElement.getAsJsonObject();
                        JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                        if (jsonArray.size() > 0) {
                            final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                            final String     filename          = packageInfoJson.has(Constants.FIELD_FILENAME) ? packageInfoJson.get(Constants.FIELD_FILENAME).getAsString() : "";
                            final String     directDownloadUri = packageInfoJson.has(Constants.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(Constants.FIELD_DIRECT_DOWNLOAD_URI).getAsString() : "";
                            if (null == filename) { return 1; }
                            System.out.println("\nDownloading " + pkg.getFilename() + ":");
                            int downloadResponse = downloadPkg(directDownloadUri, (null == parsedPath ? pkg.getFilename() : parsedPath + pkg.getFilename()), pkg.getSize());
                            if (0 == downloadResponse) {
                                if (null == parsedPath) {
                                    System.out.println("\nSuccessfully downloaded JDK pkg to current folder\n");
                                } else {
                                    System.out.println("\nSuccessfully downloaded JDK pkg to " + (parsedPath + pkg.getFilename()) + "\n");
                                }
                                return 0;
                            } else if (2 == downloadResponse) {
                                System.out.println("\nSelected JDK pkg already exists: " + filename + " \n");
                                return 0;
                            } else {
                                System.out.println(Ansi.AUTO.string("@|red \nError downloading " + pkg.getFilename() + " from " + directDownloadUri + " |@ \n"));
                                return 1;
                            }
                        } else {
                            System.out.println(Ansi.AUTO.string("@|red \nError retrieving direct download uri |@ \n"));
                            return 1;
                        }
                    }
                }
            }
            return 1;
        } catch (Exception e) {
            if (e instanceof UncheckedIOException && e.getMessage().endsWith("Operation not permitted")) {
                System.out.println(Ansi.AUTO.string("@|red \nScanning of given path not permitted |@ \n"));
            } else {
                System.out.println(Ansi.AUTO.string("@|red \nSomething went wrong, please check your parameters |@ \n"));
            }
            return 1;
        }
    }


    public static void main(final String... args) {
        if (null == args || args.length == 0) {
            System.out.println("\nA command line client for the foojay Disco API");
            System.out.println(Ansi.AUTO.string("Type in @|cyan discocli|@ @|yellow -h|@ for help\n"));
            System.exit(0);
        }
        int exitCode = new CommandLine(new DiscoCLI()).execute(args);
        System.exit(exitCode);
    }
}
