## Disco CLI

Disco CLI is a command line interface for the foojay.io Disco API

You can either use the jar file and start it with java -jar discocli-17.0.0.jar or
you can use GraalVM to create a native image and call it via discocli.

The operating system parameter (-os) can be left out if you would like to get a JDK for
your current machine because it will be detected automatically.


####Help:
java -jar discocli-17.0.0.jar -h

discocli -h

<br>

####Info:
java -jar discocli-17.0.0.jar -i

discocli -i
<br>

####Example usage of jar file (needs JDK 17):
java -jar discocli-17.0.0.jar -d zulu -v 17.0.2 -fx -os macos

discocli -d zulu -v 17.0.2 -fx
<br>

####Build native image with GraalVM:
```shell
./gradlew clean build
```
```shell
cd /build/libs
```
```shell
/PATH/TO/GRALLVM_FOLDER/bin/native-image -cp classes:discocli-17.0.0.jar --no-server -H:Name=discocli eu.hansolo.discocli.DiscoCLI --no-fallback --enable-http --enable-https
```

####Usage
```
discocli [-d=<d>] [-v=<v>] [-os=<os>] [-lc=<lc>] [-arc=<arc>] [-at=<at>] [-pt=<pt>] [-ea] [-fx] [-i]

Download a JDK pkg defined by the given parameters
-d,   --distribution=<d> Distribution (e.g. zulu, temurin, etc.)

-v,   --version=<v> Version (e.g. 17.0.2)

-os,  --operating-system=<os> Operating system (e.g. windows, linux, macos)

-lc,  --libc-type=<lc> Lib C type (libc, glibc, c_std_lib, musl)

-arc, --architecture=<arc> Architecture (e.g. x64, aarch64)

-at,  --archive-type=<at> Archive tpye (e.g. tar.gz, zip)

-pt,  --package-type=<pt> Package type (e.g. jdk, jre)

-ea,  --early-access Include early access builds

-fx,  --javafx Bundled with JavaFX

-i,   --info Info about parameters
```

####Parameters
```
---------- Distributions ----------
aoj              (AdoptOpenJDK)
aoj_openj9       (AdoptOpenJDK OpenJ9)
bisheng          (Bi Sheng)
corretto         (Corretto)
dragonwell       (Dragonwell)
graalvm_ce11     (GraalVM CE11)
graalvm_ce16     (GraalVM CE16)
graalvm_ce17     (GraalVM CE17)
graalvm_ce8      (GraalVM CE8)
jetbrains        (JetBrains)
kona             (Kona)
liberica         (Liberica)
liberica_native  (Liberica Native)
mandrel          (Mandrel)
microsoft        (Microsoft)
ojdk_build       (OJDK Build)
openlogic        (OpenLogic)
oracle           (Oracle)
oracle_open_jdk  (Oracle OpenJDK)
sap_machine      (SAP Machine)
semeru           (Semeru)
semeru_certified (Semeru certified)
temurin          (Temurin)
trava            (Trava)
zulu             (Zulu)
zulu_prime       (Zulu Prime)

---------- Operating systems ------
windows
linux
macos
alpine-linux

---------- Lib C types ------------
glibc
musl
libc
c_std_lib

---------- Architectures ----------
aarch64
aarch32
arm
arm32
armhf
armel
arm64
mips
mipsel
ppc
ppc64
ppc64le
riscv64
s390x
sparc
sparcv9
x64
x32
i386
i386
i386
x86
x86_64
amd64
ia64

---------- Archive types ----------
apk
bin
cab
deb
dmg
msi
pkg
rpm
src_tar
tar
tar.gz
tgz
tar.z
zip
exe

---------- Package types ----------
jdk
jre
```