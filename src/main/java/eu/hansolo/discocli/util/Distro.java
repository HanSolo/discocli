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

import eu.hansolo.jdktools.Api;
import eu.hansolo.jdktools.util.OutputFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public enum Distro implements Api {
    AOJ(new Distribution("AOJ", "AdoptOpenJDK", "aoj", false, List.of("aoj", "AOJ", "adopt"), "              ")),
    AOJ_OPENJ9(new Distribution("AOJ_OPENJ9", "AdoptOpenJDK OpenJ9", "aoj_openj9", false, List.of("aoj_openj9","AOJ_OpenJ9","AOJ_OPENJ9","AOJ OpenJ9","AOJ OPENJ9","aoj openj9"), "       ")),
    BISHENG(new Distribution("BISHENG", "Bi Sheng", "bisheng", true, List.of("bisheng", "BISHENG", "BiSheng", "bi_sheng", "BI_SHENG", "bi-sheng", "BI-SHENG", "bi sheng", "BI SHENG", "Bi Sheng"), "          ")),
    CORRETTO(new Distribution("CORRETTO", "Corretto", "corretto", true, List.of("corretto","CORRETTO","Corretto"), "         ")),
    DRAGONWELL(new Distribution("DRAGONWELL", "Dragonwell", "dragonwell", true, List.of("dragonwell","DRAGONWELL","Dragonwell"), "       ")),
    GLUON_GRAALVM(new Distribution("GLUON_GRAALVM", "Gluon GraalVM", "gluon_graalvm", true, List.of("gluon_graalvm", "GLUON_GRAALVM", "gluongraalvm", "GLUONGRAALVM", "gluon graalvm", "Gluon GraalVM", "GluonGraalVM"), "    ")),
    GRAALVM_CE8(new Distribution("GRAALVM_CE8", "GraalVM CE8", "graalvm_ce8", false, List.of("GraalVM CE 8","GraalVMCE8","GraalVM_CE8"), "      ")),
    GRAALVM_CE11(new Distribution("GRAALVM_CE11", "GraalVM CE11", "graalvm_ce11", true, List.of("GraalVM CE 11","GraalVMCE11","GraalVM_CE11"), "     ")),
    GRAALVM_CE16(new Distribution("GRAALVM_CE16", "GraalVM CE16", "graalvm_ce16", true, List.of("GraalVM CE 16","GraalVMCE16","GraalVM_CE16"), "     ")),
    GRAALVM_CE17(new Distribution("GRAALVM_CE17", "GraalVM CE17", "graalvm_ce17", true, List.of("GraalVM CE 17","GraalVMCE17","GraalVM_CE17"), "     ")),
    JETBRAINS(new Distribution("JETBRAINS", "JetBrains", "jetbrains", true, List.of("jetbrains","JETBRAINS","JetBrains"), "        ")),
    KONA(new Distribution("KONA", "Kona", "kona", true, List.of("kona","KONA","Kona"), "             ")),
    LIBERICA(new Distribution("LIBERICA", "Liberica", "liberica", true, List.of("liberica","LIBERICA","Liberica"), "         ")),
    LIBERICA_NATIVE(new Distribution("LIBERICA_NATIVE", "Liberica Native", "liberica_native", true, List.of("liberica_native","LIBERICA_NATIVE","libericaNative","LibericaNative","liberica native","LIBERICA NATIVE","Liberica Native"), "  ")),
    MANDREL(new Distribution("MANDREL", "Mandrel", "mandrel", true, List.of("mandrel", "MANDREL", "Mandrel"), "          ")),
    MICROSOFT(new Distribution("MICROSOFT", "Microsoft", "microsoft", true, List.of("microsoft", "MICROSOFT", "Microsoft"), "        ")),
    OJDK_BUILD(new Distribution("OJDK_BUILD", "OJDK Build", "ojdk_build", true, List.of("ojdk_build","OJDK_BUILD","OJDK Build","ojdk build","ojdkbuild","OJDKBuild"), "       ")),
    OPEN_LOGIC(new Distribution("OPEN_LOGIC", "OpenLogic", "openlogic", true, List.of("openlogic","OPENLOGIC","OpenLogic","open_logic","OPEN_LOGIC","Open Logic","OPEN LOGIC","open logic"), "        ")),
    ORACLE_OPEN_JDK(new Distribution("ORACLE_OPEN_JDK", "Oracle OpenJDK", "oracle_open_jdk", true, List.of("oracle_open_jdk","ORACLE_OPEN_JDK","oracle_openjdk","ORACLE_OPENJDK","Oracle_OpenJDK","Oracle OpenJDK","oracle openjdk","ORACLE OPENJDK","open_jdk","openjdk","OpenJDK","Open JDK","OPEN_JDK","open-jdk","OPEN-JDK","Oracle-OpenJDK","oracle-openjdk","ORACLE-OPENJDK","oracle-open-jdk","ORACLE-OPEN-JDK"), "  ")),
    ORACLE(new Distribution("ORACLE", "Oracle", "oracle", true, List.of("oracle","ORACLE","Oracle"), "           ")),
    SAP_MACHINE(new Distribution("SAP_MACHINE", "SAP Machine", "sap_machine", true, List.of("sap_machine","sapmachine","SAPMACHINE","SAP_MACHINE","SAPMachine","SAP Machine","sap-machine","SAP-Machine","SAP-MACHINE"), "      ")),
    SEMERU(new Distribution("SEMERU", "Semeru", "semeru", true, List.of("semeru", "Semeru", "SEMERU"), "           ")),
    SEMERU_CERTIFIED(new Distribution("SEMERU_CERTIFIED", "Semeru certified", "semeru_certified", true, List.of("semeru_certified", "SEMERU_CERTIFIED", "Semeru_Certified", "Semeru_certified", "semeru certified", "SEMERU CERTIFIED", "Semeru Certified", "Semeru certified"), " ")),
    TEMURIN(new Distribution("TEMURIN", "Temurin", "temurin", true, List.of("temurin","TEMURIN","Temurin"), "          ")),
    TRAVA(new Distribution("TRAVA", "Trava", "trava", true, List.of("trava", "TRAVA", "Trava"), "            ")),
    ZULU(new Distribution("ZULU", "Zulu", "zulu", true, List.of("zulu","ZULU","Zulu","zulucore","ZULUCORE","ZuluCore","zulu_core","ZULU_CORE","Zulu_Core","zulu core","ZULU CORE","Zulu Core"), "             ")),
    ZULU_PRIME(new Distribution("ZULU_PRIME", "Zulu Prime", "zulu_prime", true, List.of("zing","ZING","Zing","prime","PRIME","Prime","zuluprime","ZULUPRIME","ZuluPrime","zulu_prime","ZULU_PRIME","Zulu_Prime","zulu prime","ZULU PRIME","Zulu Prime"), "       ")),
    NONE(null),
    NOT_FOUND(null);

    private final Distribution distribution;


    // ******************** Constructors **************************************
    Distro(final Distribution distribution) {
        this.distribution = distribution;
    }


    // ******************** Methods *******************************************
    @Override public String getUiString() { return get().uiString(); }

    @Override public String getApiString() { return get().apiString(); }

    @Override public Distro getDefault() { return Distro.NONE; }

    @Override public Distro getNotFound() { return Distro.NOT_FOUND; }

    @Override public Distro[] getAll() { return values(); }

    public final String getName() { return name().toUpperCase(); }

    public static final Distribution distributionFromText(final String text) { return fromText(text).get(); }

    public static final Distro fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
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
                return ZULU;
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
                return ZULU_PRIME;
            case "aoj":
            case "AOJ":
                return AOJ;
            case "aoj_openj9":
            case "AOJ_OpenJ9":
            case "AOJ_OPENJ9":
            case "AOJ OpenJ9":
            case "AOJ OPENJ9":
            case "aoj openj9":
                return AOJ_OPENJ9;
            case "corretto":
            case "CORRETTO":
            case "Corretto":
                return CORRETTO;
            case "dragonwell":
            case "DRAGONWELL":
            case "Dragonwell":
                return DRAGONWELL;
            case "gluon_graalvm":
            case "GLUON_GRAALVM":
            case "gluongraalvm":
            case "GLUONGRAALVM":
            case "gluon graalvm":
            case "GLUON GRAALVM":
            case "Gluon GraalVM":
            case "Gluon":
                return GLUON_GRAALVM;
            case "graalvm_ce8":
            case "graalvmce8":
            case "GraalVM CE 8":
            case "GraalVMCE8":
            case "GraalVM_CE8":
                return GRAALVM_CE8;
            case "graalvm_ce11":
            case "graalvmce11":
            case "GraalVM CE 11":
            case "GraalVMCE11":
            case "GraalVM_CE11":
                return GRAALVM_CE11;
            case "graalvm_ce16":
            case "graalvmce16":
            case "GraalVM CE 16":
            case "GraalVMCE16":
            case "GraalVM_CE16":
                return GRAALVM_CE16;
            case "graalvm_ce17":
            case "graalvmce17":
            case "GraalVM CE 17":
            case "GraalVMCE17":
            case "GraalVM_CE17":
                return GRAALVM_CE17;
            case "jetbrains":
            case "JetBrains":
            case "JETBRAINS":
                return JETBRAINS;
            case "liberica":
            case "LIBERICA":
            case "Liberica":
                return LIBERICA;
            case "liberica_native":
            case "LIBERICA_NATIVE":
            case "libericaNative":
            case "LibericaNative":
            case "liberica native":
            case "LIBERICA NATIVE":
            case "Liberica Native":
                return LIBERICA_NATIVE;
            case "mandrel":
            case "MANDREL":
            case "Mandrel":
                return MANDREL;
            case "microsoft":
            case "Microsoft":
            case "MICROSOFT":
            case "Microsoft OpenJDK":
            case "Microsoft Build of OpenJDK":
                return MICROSOFT;
            case "ojdk_build":
            case "OJDK_BUILD":
            case "OJDK Build":
            case "ojdk build":
            case "ojdkbuild":
            case "OJDKBuild":
                return OJDK_BUILD;
            case "openlogic":
            case "OPENLOGIC":
            case "OpenLogic":
            case "open_logic":
            case "OPEN_LOGIC":
            case "Open Logic":
            case "OPEN LOGIC":
            case "open logic":
                return OPEN_LOGIC;
            case "oracle":
            case "Oracle":
            case "ORACLE":
                return ORACLE;
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
                return ORACLE_OPEN_JDK;
            case "sap_machine":
            case "sapmachine":
            case "SAPMACHINE":
            case "SAP_MACHINE":
            case "SAPMachine":
            case "SAP Machine":
            case "sap-machine":
            case "SAP-Machine":
            case "SAP-MACHINE":
                return SAP_MACHINE;
            case "semeru":
            case "Semeru":
            case "SEMERU":
                return SEMERU;
            case "semeru_certified":
            case "SEMERU_CERTIFIED":
            case "Semeru_Certified":
            case "Semeru_certified":
            case "semeru certified":
            case "SEMERU CERTIFIED":
            case "Semeru Certified":
            case "Semeru certified":
                return SEMERU_CERTIFIED;
            case "temurin":
            case "Temurin":
            case "TEMURIN":
                return TEMURIN;
            case "trava":
            case "TRAVA":
            case "Trava":
            case "trava_openjdk":
            case "TRAVA_OPENJDK":
            case "trava openjdk":
            case "TRAVA OPENJDK":
                return TRAVA;
            case "kona":
            case "KONA":
            case "Kona":
                return KONA;
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
                return BISHENG;
            default:
                return NOT_FOUND;
        }
    }

    public final Distribution get() { return distribution; }

    public final boolean isMaintained() { return get().maintained(); }

    public final static List<Distribution> getDistributions() {
        return Arrays.stream(values())
                     .filter(distro -> Distro.NONE      != distro)
                     .filter(distro -> Distro.NOT_FOUND != distro)
                     .map(Distro::get).collect(Collectors.toList());
    }

    public final static List<Distro> getAsList() { return Arrays.asList(values()); }

    public final static List<Distro> getAsListWithoutNoneAndNotFound() {
        return getAsList().stream()
                          .filter(distro -> Distro.NONE != distro)
                          .filter(distro -> Distro.NOT_FOUND != distro)
                          .sorted(Comparator.comparing(Distro::name).reversed())
                          .collect(Collectors.toList());
    }

    public final static List<Distro> getMaintainedAsListWithoutNoneAndNotFound() {
        return getAsList().stream()
                          .filter(distro -> Distro.NONE != distro)
                          .filter(distro -> Distro.NOT_FOUND != distro)
                          .filter(Distro::isMaintained)
                          .sorted(Comparator.comparing(Distro::name).reversed())
                          .collect(Collectors.toList());
    }

    public final static List<Distro> getDistrosWithJavaVersioning() {
        return Arrays.stream(values())
                     .filter(distro -> Distro.NONE            != distro)
                     .filter(distro -> Distro.NOT_FOUND       != distro)
                     .filter(distro -> Distro.GRAALVM_CE17    != distro)
                     .filter(distro -> Distro.GRAALVM_CE16    != distro)
                     .filter(distro -> Distro.GRAALVM_CE11    != distro)
                     .filter(distro -> Distro.GRAALVM_CE8     != distro)
                     .filter(distro -> Distro.MANDREL         != distro)
                     .filter(distro -> Distro.LIBERICA_NATIVE != distro)
                     .collect(Collectors.toList());
    }

    public final String toString(final OutputFormat outputFormat) {
        return toString();
    }

    @Override public String toString() {
        return get().toString();
    }
}
