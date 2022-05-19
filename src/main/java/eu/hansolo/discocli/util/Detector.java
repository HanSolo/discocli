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

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingMode;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.scopes.BuildScope;
import eu.hansolo.jdktools.util.Helper.OsArcMode;
import eu.hansolo.jdktools.util.OutputFormat;
import eu.hansolo.jdktools.versioning.VersionNumber;
import picocli.CommandLine.Help.Ansi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.hansolo.jdktools.OperatingSystem.WINDOWS;


public class Detector {
    public static final  String          MACOS_JAVA_INSTALL_PATH   = "/System/Volumes/Data/Library/Java/JavaVirtualMachines/";
    public static final  String          WINDOWS_JAVA_INSTALL_PATH = "C:\\Program Files\\Java\\";
    public static final  String          LINUX_JAVA_INSTALL_PATH   = "/usr/lib/jvm";
    public  static final String          SDKMAN_FOLDER             = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(".sdkman").append(File.separator).append("candidates").append(File.separator).append("java").toString();
    private static final Pattern         GRAALVM_VERSION_PATTERN   = Pattern.compile("(.*graalvm\\s)(.*)(\\s\\(.*)");
    private static final Matcher         GRAALVM_VERSION_MATCHER   = GRAALVM_VERSION_PATTERN.matcher("");
    private static final Pattern         ZULU_BUILD_PATTERN        = Pattern.compile("\\((build\\s)(.*)\\)");
    private static final Matcher         ZULU_BUILD_MATCHER        = ZULU_BUILD_PATTERN.matcher("");
    private static final String[]        MAC_JAVA_HOME_CMDS        = { "/bin/sh", "-c", "echo $JAVA_HOME" };
    private static final String[]        LINUX_JAVA_HOME_CMDS      = { "/usr/bin/sh", "-c", "echo $JAVA_HOME" };
    private static final String[]        WIN_JAVA_HOME_CMDS        = { "cmd.exe", "/c", "echo %JAVA_HOME%" };
    private              ExecutorService service                   = Executors.newSingleThreadExecutor();
    private              Properties      releaseProperties         = new Properties();
    private              OsArcMode       osArcMode                 = eu.hansolo.jdktools.util.Helper.getOperaringSystemArchitectureOperatingMode();
    private              String          javaFile                  = WINDOWS == osArcMode.operatingSystem() ? "java.exe" : "java";
    private              String          javaHome                  = "";


    public Detector() {
        getJavaHome();
        if (null == this.javaHome || this.javaHome.isEmpty()) {
            if (System.getProperties() != null) {
                if (System.getProperties().get("java.home") != null) {
                    this.javaHome = System.getProperties().get("java.home").toString();
                }
            }
        }
    }


    public Set<Distro> detectDistributions() {
        return detectDistributions("");
    }
    public Set<Distro> detectDistributions(final String... searchPaths) {
        System.out.println(Ansi.AUTO.string("@|cyan \nDistributions found |@"));
        final List<String> pathsToScan = new ArrayList<>();
        if (null == searchPaths || searchPaths.length == 0 || (searchPaths.length == 1 && (null == searchPaths[0] || searchPaths[0].isEmpty()))) {
            switch (eu.hansolo.jdktools.util.Helper.getOperatingSystem()) {
                case MACOS  : pathsToScan.add(Detector.MACOS_JAVA_INSTALL_PATH); break;
                case WINDOWS: pathsToScan.add(Detector.WINDOWS_JAVA_INSTALL_PATH); break;
                case LINUX  : pathsToScan.add(Detector.LINUX_JAVA_INSTALL_PATH); break;
                default     : pathsToScan.add(WINDOWS == osArcMode.operatingSystem() ? ".\\" : "./"); break;
            }
        } else {
            pathsToScan.addAll(Arrays.asList(searchPaths).stream().filter(Objects::nonNull).filter(path -> !path.isEmpty()).collect(Collectors.toList()));
        }
        Set<Distro> distros = new HashSet<>();

        if (service.isShutdown()) {
            service = Executors.newSingleThreadExecutor();
        }

        pathsToScan.forEach(searchPath -> {
            final Path path      = Paths.get(searchPath);
            Set<Path>  javaFiles = findByFileNameWithoutException(path, javaFile);
            javaFiles.stream().filter(java -> !java.toString().contains("jre")).forEach(java -> checkForDistribution(java.toString()));
        });

        service.shutdown();
        try {
            service.awaitTermination(20000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println(Ansi.AUTO.string("@|red \nError detecting distributions |@ \n"));
        }
        return distros;
    }

    /*
    public Map<Distro, List<Pkg>> getAvailableUpdates(final List<Distro> distributions) {
        Map<Distro, List<Pkg>> distrosToUpdate = new ConcurrentHashMap<>();
        //List<CompletableFuture<Void>> updateFutures   = Collections.synchronizedList(new ArrayList<>());
        //distributions.forEach(distribution -> updateFutures.add(discoclient.updateAvailableForAsync(DiscoClient.getDistributionFromText(distribution.getApiString()), Semver.fromText(distribution.getVersion()).getSemver1(), Architecture.fromText(distribution.getArchitecture()), distribution.getFxBundled(), null).thenAccept(pkgs -> distrosToUpdate.put(distribution, pkgs))));
        //CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[updateFutures.size()])).join();

        distributions.forEach(distribution -> {
            List<Pkg> availableUpdates = discoclient.updateAvailableFor(DiscoClient.getDistributionFromText(distribution.getApiString()), Semver.fromText(distribution.getVersion()).getSemver1(), operatingSystem, Architecture.fromText(distribution.getArchitecture()), distribution.getFxBundled(), null, distribution.getFeature());
            if (null != availableUpdates) {
                distrosToUpdate.put(distribution, availableUpdates);
            }

            if (OperatingSystem.ALPINE_LINUX == operatingSystem) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getLibCType() == LibCType.MUSL).collect(Collectors.toList());
            } else if (OperatingSystem.LINUX == operatingSystem) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getLibCType() != LibCType.MUSL).collect(Collectors.toList());
            }
            if (Architecture.NOT_FOUND != architecture) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getArchitecture() == architecture).collect(Collectors.toList());
            }

            distrosToUpdate.put(distribution, availableUpdates);
        });

        // Check if there are newer versions from other distributions
        distrosToUpdate.entrySet()
                       .stream()
                       .filter(entry -> !entry.getKey().getApiString().startsWith("graal"))
                       .filter(entry -> !entry.getKey().getApiString().equals("mandrel"))
                       .filter(entry -> !entry.getKey().getApiString().equals("liberica_native"))
                       .forEach(entry -> {
                           if (entry.getValue().isEmpty()) {
                               Distro distro = entry.getKey();
                               entry.setValue(discoclient.updateAvailableFor(null, Semver.fromText(distro.getVersion()).getSemver1(), Architecture.fromText(distro.getArchitecture()), distro.getFxBundled()));
                           }
                       });

        LinkedHashMap<Distro, List < Pkg >> sorted = new LinkedHashMap<>();
        distrosToUpdate.entrySet()
                       .stream()
                       .sorted(Map.Entry.comparingByKey(Comparator.comparing(Distro::getName)))
                       .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return sorted;
    }
    */
    public OperatingSystem getOperatingSystem() { return osArcMode.operatingSystem(); }

    public Architecture getArchitecture() { return osArcMode.architecture(); }

    public OperatingMode getOperatingMode() { return osArcMode.operatingMode(); }

    public boolean isSDKMANInstalled() { return new File(SDKMAN_FOLDER).exists(); }

    private JdkInfo getJDKFromJar(final String jarFileName) {
        try {
            final JarFile                        jarFile      = new JarFile(jarFileName);
            final Manifest                       manifest     = jarFile.getManifest();
            final Attributes                     attributes   = manifest.getMainAttributes();
            final Optional<Entry<Object,Object>> optCreatedBy = attributes.entrySet().stream().filter(entry -> entry.getKey().toString().equalsIgnoreCase("Created-By")).findFirst();
            final Optional<Entry<Object,Object>> optBuildJdk  = attributes.entrySet().stream().filter(entry -> entry.getKey().toString().equalsIgnoreCase("Build-Jdk")).findFirst();
            final String                         createdBy    = optCreatedBy.isPresent() ? optCreatedBy.get().getValue().toString() : "";
            final String                         buildJdk     = optBuildJdk.isPresent()  ? optBuildJdk.get().getValue().toString()  : "";
            return new JdkInfo(createdBy, buildJdk);
        } catch(IOException e) {
            return new JdkInfo("", "");
        }
    }

    private List<Path> findByFileName(final Path path, final String fileName) {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> pathStream = Files.find(path, Integer.MAX_VALUE, (p, basicFileAttributes) -> {
            // if directory or no-read permission, ignore
            if(Files.isDirectory(p) || !Files.isReadable(p)) { return false; }
            return p.getFileName().toString().equalsIgnoreCase(fileName);
        })) {
            result.addAll(pathStream.collect(Collectors.toList()));
        } catch (Exception e) {
            System.out.println(e);
            //result = new ArrayList<>();
        }
        return result;
    }

    private Set<Path> findByFileNameWithoutException(final Path path, final String fileName) {
        Set<Path> result = new HashSet<>();
        try (Stream<Path> stream = Files.walk(path, Integer.MAX_VALUE)) {
            stream.filter(p -> Files.isRegularFile(p))
                  .filter(p -> Files.isReadable(p))
                  .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                  .forEach(p -> result.add(p));
        } catch (Exception e) {
            // Silence "Operation not permitted" exception
        }
        return result;
    }

    private void checkForDistribution(final String java) {
        AtomicBoolean inUse = new AtomicBoolean(false);
        try {
            List<String> commands = new ArrayList<>();
            commands.add(java);
            commands.add("-version");

            final String fileSeparator = File.separator;
            final String binFolder     = new StringBuilder(fileSeparator).append("bin").append(fileSeparator).append(".*").toString();

            ProcessBuilder builder  = new ProcessBuilder(commands).redirectErrorStream(true);
            Process        process  = builder.start();
            Streamer streamer = new Streamer(process.getInputStream(), d -> {
                final String parentPath       = WINDOWS == osArcMode.operatingSystem() ? java.replaceAll("bin\\\\java.exe", "") : java.replaceAll(binFolder, fileSeparator);
                final File   releaseFile      = new File(parentPath + "release");
                String[]     lines            = d.split("\\|");
                String       name             = "Unknown build of OpenJDK";
                String       apiString        = "";
                //String       operatingSystem  = "";
                String       architecture     = "";
                String       feature          = "";
                Boolean      fxBundled        = Boolean.FALSE;
                //FPU          fpu              = FPU.UNKNOWN;

                if (!this.javaHome.isEmpty() && !inUse.get() && parentPath.contains(javaHome)) {
                    inUse.set(true);
                }

                final File   jreLibExtFolder  = new File(new StringBuilder(parentPath).append("jre").append(fileSeparator).append("lib").append(fileSeparator).append("ext").toString());
                if (jreLibExtFolder.exists()) {
                    fxBundled = Stream.of(jreLibExtFolder.listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet()).stream().filter(filename -> filename.equalsIgnoreCase("jfxrt.jar")).count() > 0;
                }
                final File   jmodsFolder      = new File(new StringBuilder(parentPath).append("jmods").toString());
                if (jmodsFolder.exists()) {
                    fxBundled = Stream.of(jmodsFolder.listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet()).stream().filter(filename -> filename.startsWith("javafx")).count() > 0;
                }

                VersionNumber version    = null;
                VersionNumber jdkVersion = null;
                BuildScope    buildScope = BuildScope.BUILD_OF_OPEN_JDK;

                String        line1         = lines[0];
                String        line2         = lines[1];
                String        withoutPrefix = line1;
                if (line1.startsWith("openjdk")) {
                    withoutPrefix = line1.replaceFirst("openjdk version", "");
                } else if (line1.startsWith("java")) {
                    withoutPrefix = line1.replaceFirst("java version", "");
                    name          = "Oracle";
                    apiString     = "oracle";
                }
                if (line2.contains("Zulu")) {
                    name      = "Zulu";
                    apiString = "zulu";
                    ZULU_BUILD_MATCHER.reset(line2);
                    final List<MatchResult> results = ZULU_BUILD_MATCHER.results().collect(Collectors.toList());
                    if (!results.isEmpty()) {
                        MatchResult result = results.get(0);
                        version = VersionNumber.fromText(result.group(2));
                    }
                } else if (line2.contains("Semeru")) {
                    if (line2.contains("Certified")) {
                        name      = "Semeru certified";
                        apiString = "semeru_certified";
                    } else {
                        name      = "Semeru";
                        apiString = "semeru";
                    }
                } else if (line2.contains("Tencent")) {
                    name      = "Kona";
                    apiString = "kona";
                } else if (line2.contains("Bisheng")) {
                    name      = "Bishenq";
                    apiString = "bisheng";
                }

                if (null == version) { version = VersionNumber.fromText(withoutPrefix.substring(withoutPrefix.indexOf("\"") + 1, withoutPrefix.lastIndexOf("\""))); }
                VersionNumber graalVersion = version;

                releaseProperties.clear();
                if (releaseFile.exists()) {
                    try (FileInputStream propFile = new FileInputStream(releaseFile)) {
                        releaseProperties.load(propFile);
                    } catch (IOException ex) {
                        System.out.println("Error reading release properties file. " + ex);
                    }
                    if (!releaseProperties.isEmpty()) {
                        if (releaseProperties.containsKey("IMPLEMENTOR") && name.equals("Unknown build of OpenJDK")) {
                            switch(releaseProperties.getProperty("IMPLEMENTOR").replaceAll("\"", "")) {
                                case "AdoptOpenJDK"      : name = "Adopt OpenJDK";  apiString = "aoj";            break;
                                case "Alibaba"           : name = "Dragonwell";     apiString = "dragonwell";     break;
                                case "Amazon.com Inc."   : name = "Corretto";       apiString = "corretto";       break;
                                case "Azul Systems, Inc.": name = "Zulu";           apiString = "zulu";           break;
                                case "mandrel"           : name = "Mandrel";        apiString = "mandrel";        break;
                                case "Microsoft"         : name = "Microsoft";      apiString = "microsoft";      break;
                                case "ojdkbuild"         : name = "OJDK Build";     apiString = "ojdk_build";     break;
                                case "Oracle Corporation": name = "Oracle OpenJDK"; apiString = "oracle_openjdk"; break;
                                case "Red Hat, Inc."     : name = "Red Hat";        apiString = "redhat";         break;
                                case "SAP SE"            : name = "SAP Machine";    apiString = "sap_machine";    break;
                                case "OpenLogic"         : name = "OpenLogic";      apiString = "openlogic";      break;
                                case "JetBrains s.r.o."  : name = "JetBrains";      apiString = "jetbrains";      break;
                                case "Eclipse Foundation": name = "Temurin";        apiString = "temurin";        break;
                                case "Tencent"           : name = "Kona";           apiString = "kona";           break;
                                case "Bisheng"           : name = "Bisheng";        apiString = "bisheng";        break;
                                case "Debian"            : name = "Debian";         apiString = "debian";         break;
                                case "N/A"               : /* GraalVM */ break;
                            }
                        }
                        if (releaseProperties.containsKey("OS_ARCH")) {
                            architecture = releaseProperties.getProperty("OS_ARCH").toLowerCase().replaceAll("\"", "");
                        }
                        if (releaseProperties.containsKey("JVM_VARIANT")) {
                            if (name == "Adopt OpenJDK") {
                                String jvmVariant = releaseProperties.getProperty("JVM_VARIANT").toLowerCase().replaceAll("\"", "");
                                if (jvmVariant.equals("dcevm")) {
                                    name      = "Trava OpenJDK";
                                    apiString = "trava";
                                } else if (jvmVariant.equals("openj9")) {
                                    name      = "Adopt OpenJDK J9";
                                    apiString = "aoj_openj9";
                                }
                            }
                        }
                        /*
                        if (releaseProperties.containsKey("OS_NAME")) {
                            switch(releaseProperties.getProperty("OS_NAME").toLowerCase().replaceAll("\"", "")) {
                                case "darwin" : operatingSystem = "macos"; break;
                                case "linux"  : operatingSystem = "linux"; break;
                                case "windows": operatingSystem = "windows"; break;
                            }
                        }
                        */
                        if (releaseProperties.containsKey("MODULES") && !fxBundled) {
                            fxBundled = (releaseProperties.getProperty("MODULES").contains("javafx"));
                        }
                        /*
                        if (releaseProperties.containsKey("SUN_ARCH_ABI")) {
                            String abi = releaseProperties.get("SUN_ARCH_ABI").toString();
                            switch (abi) {
                                case "gnueabi"   -> fpu = FPU.SOFT_FLOAT;
                                case "gnueabihf" -> fpu = FPU.HARD_FLOAT;
                            }
                        }
                        */
                    }
                }

                if (lines.length > 2) {
                    String line3 = lines[2].toLowerCase();
                    for (String feat : Constants.FEATURES) {
                        feat = feat.trim().toLowerCase();
                        if (line3.contains(feat)) {
                            feature = feat;
                            break;
                        }
                    }

                }

                if (name.equalsIgnoreCase("Mandrel")) {
                    buildScope = BuildScope.BUILD_OF_GRAALVM;
                    if (releaseProperties.containsKey("JAVA_VERSION")) {
                        final String javaVersion = releaseProperties.getProperty("JAVA_VERSION");
                        if (null == jdkVersion) { jdkVersion = VersionNumber.fromText(javaVersion); }
                    }
                }

                if (name.equals("Unknown build of OpenJDK") && lines.length > 2) {
                    String line3      = lines[2].toLowerCase();
                    File   readmeFile = new File(parentPath + "readme.txt");
                    if (readmeFile.exists()) {
                        try {
                            List<String> readmeLines = Helper.readTextFileToList(readmeFile.getAbsolutePath());
                            if (readmeLines.stream().filter(l -> l.toLowerCase().contains("liberica native image kit")).count() > 0) {
                                name       = "Liberica Native";
                                apiString  = "liberica_native";
                                buildScope = BuildScope.BUILD_OF_GRAALVM;

                                GRAALVM_VERSION_MATCHER.reset(line3);
                                final List<MatchResult> results = GRAALVM_VERSION_MATCHER.results().collect(Collectors.toList());
                                if (!results.isEmpty()) {
                                    MatchResult result = results.get(0);
                                    version = VersionNumber.fromText(result.group(2));
                                }
                                if (releaseProperties.containsKey("JAVA_VERSION")) {
                                    final String javaVersion = releaseProperties.getProperty("JAVA_VERSION");
                                    if (null == jdkVersion) { jdkVersion = VersionNumber.fromText(javaVersion); }
                                }
                            } else if (readmeLines.stream().filter(l -> l.toLowerCase().contains("liberica")).count() > 0) {
                                name      = "Liberica";
                                apiString = "liberica";
                            }
                        } catch (IOException e) {

                        }
                    } else {
                        if (line3.contains("graalvm")) {
                            name       = "GraalVM";
                            apiString  = graalVersion.getMajorVersion().getAsInt() >= 8 ? "graalvm_ce" + graalVersion.getMajorVersion().getAsInt() : "";
                            buildScope = BuildScope.BUILD_OF_GRAALVM;

                            GRAALVM_VERSION_MATCHER.reset(line3);
                            final List<MatchResult> results = GRAALVM_VERSION_MATCHER.results().collect(Collectors.toList());
                            if (!results.isEmpty()) {
                                MatchResult result = results.get(0);
                                version = VersionNumber.fromText(result.group(2));
                            }

                            if (releaseProperties.containsKey("VENDOR")) {
                                final String vendor = releaseProperties.getProperty("VENDOR").toLowerCase().replaceAll("\"", "");
                                if (vendor.equalsIgnoreCase("Gluon")) {
                                    name      = "Gluon GraalVM";
                                    apiString = "gluon_graalvm";
                                }
                            }
                            if (releaseProperties.containsKey("JAVA_VERSION")) {
                                final String javaVersion = releaseProperties.getProperty("JAVA_VERSION");
                                if (null == jdkVersion) { jdkVersion = VersionNumber.fromText(javaVersion); }
                            }
                        } else if (line3.contains("microsoft")) {
                            name      = "Microsoft";
                            apiString = "microsoft";
                        } else if (line3.contains("corretto")) {
                            name      = "Corretto";
                            apiString = "corretto";
                        } else if (line3.contains("temurin")) {
                            name      = "Temurin";
                            apiString = "temurin";
                        }
                    }
                }

                if (null == jdkVersion) { jdkVersion = version; }

                if (architecture.isEmpty()) { architecture = osArcMode.architecture().name().toLowerCase(); }

                final String jkdString = new StringBuilder().append(inUse.get() ? "*" : "")
                                                            .append(apiString).append(",")
                                                            .append(version.toString(OutputFormat.REDUCED_COMPRESSED, true, true)).append(",")
                                                            .append(osArcMode.operatingSystem().getApiString()).append(",")
                                                            .append(architecture)
                                                            .append((fxBundled ? ",fx" : ""))
                                                            .append(feature.isEmpty() ? "" : ",")
                                                            .append(feature)
                                                            .append(" (").append(parentPath).append(")")
                                                            .toString();
                System.out.println(jkdString);
            });
            service.submit(streamer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getJavaHome() {
        try {
            ProcessBuilder processBuilder = WINDOWS == osArcMode.operatingSystem() ? new ProcessBuilder(WIN_JAVA_HOME_CMDS) : OperatingSystem.MACOS == osArcMode.operatingSystem() ? new ProcessBuilder(MAC_JAVA_HOME_CMDS) : new ProcessBuilder(LINUX_JAVA_HOME_CMDS);
            Process        process        = processBuilder.start();
            Streamer       streamer       = new Streamer(process.getInputStream(), d -> this.javaHome = d);
            service.submit(streamer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class Streamer implements Runnable {
        private InputStream      inputStream;
        private Consumer<String> consumer;

        public Streamer(final InputStream inputStream, final Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer    = consumer;
        }

        @Override public void run() {
            final StringBuilder builder = new StringBuilder();
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(line -> builder.append(line).append("|"));
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            consumer.accept(builder.toString());
        }
    }
}
