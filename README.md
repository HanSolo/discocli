## Disco CLI

<img src="https://raw.githubusercontent.com/HanSolo/discocli/main/resources/banner.png" alt="DiscoCLI" style="width:200px;"/>

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/HanSolo/discocli/BuildNativeImage)
![GitHub all releases](https://img.shields.io/github/downloads/HanSolo/discocli/total)

Disco CLI is a command line interface for the [foojay.io](https://foojay.io) [Disco API](https://github.com/foojayio/discoapi)

You can either use the jar file and start it with java -jar discocli-17.0.3.jar (which needs JDK 17 to be
the current jdk) or
you can use the native image and call it via discocli.
At the moment there are binaries available for 
- Windows x64 (Intel)
- Linux x64 (Intel)
- Macos x64 (which also works on aarch64, thx to Rosetta2)

The available binaries can be found [here](https://github.com/HanSolo/discocli/releases)

From version 17.0.3 on now the native images will always be build on each push using [github actions](https://github.com/HanSolo/discocli/actions) and can be downloaded from the action itself.
Keep in mind that you might have to allow the downloaded binary to be executed on your local machine.
On Macos you need to open the binary by CTRL+RIGHT click and Open it once in the Finder and then it will work from the command line.
On Windows you have to double click the exe file and agree to execute it once, after that you can execute it also from the command line.

<b>Attention:</b> On Windows it might be needed to download and install the [Microsoft Visual C++ 2015 Redistributable Update 3 RC](https://www.microsoft.com/en-us/download/details.aspx?id=52685) to
get the VCRUNTIME140.dll for Java based native images.

The operating system parameter (-os) can be left out if you would like to get a JDK for
your current machine because it will be detected automatically.


#### Help:
Get help by using the ```-h or --help``` parameter as follows:

```shell
Using the jar:
java -jar discocli-17.0.3.jar -h

Using the binary:
discocli -h
```
<br>

#### Info:
If not specified, the distribution will default to Zulu and the 
archive type will default to ```tar.gz``` for Linux, Mac and to ```zip``` for Windows.
If you do not specify the operatings system discocli will try to detect
the current operating system and use it.
In case you would like to download a version for Alpine Linux, you can
either specify the ```-os``` parameter to ```-os alpine-linux``` or use
the lib c type parameter in combination with linux as os e.g. ```-os linux -lc musl```.
<br>

#### Example usaging the jar file (needs JDK 17):

Get Zulu with version 17.0.2 for windows including JavaFX:
```shell
java -jar discocli-17.0.3.jar -d zulu -v 17.0.2 -fx -os windows
```
Get the latest version of JDK 17 for Temurin on Linux:
```shell
java -jar discocli-17.0.3.jar -d temurin -v 17 -os linux -latest
```


#### Example using the binary:

Get Zulu with version 17.0.2 for the current operating system including JavaFX:
```shell
discocli -d zulu -v 17.0.2 -fx
```

Get the latest version of JDK 16 for Liberica on Windows:
```shell
discocli -d liberica -v 16 -os windows -latest
```

Get the JDK 17.0.2 of temurin for macos with aarch64 as a tar.gz and store it to a folder
```shell
discocli -d temurin -v 17.0.2 -os macos -arc aarch64 -at tar.gz -p /Users/hansolo
```

In case a JDK pkg cannot be found discocli will try to give you the available pkgs.
```shell
discocli -d liberica -v 12 -os linux -arc x64 -fx -latest

Sorry, defined pkg not found in Disco API

Packages available for Liberica for version 12:
discocli -d liberica -v 12.0.2 -os linux -lc glibc -arc amd64 -at tar.gz -pt jdk
discocli -d liberica -v 12.0.1 -os linux -lc glibc -arc amd64 -at tar.gz -pt jdk
discocli -d liberica -v 12 -os linux -lc glibc -arc amd64 -at tar.gz -pt jdk
```

<br>

#### Build native image with GraalVM:

- Make sure you have GraalVM installed (including the native image feature)
- Set GRAALVM_HOME environment variable
- Find more info related to operating system specific requirements [here](https://www.graalvm.org/22.0/reference-manual/native-image/)


```shell
Build the jar file using:
./gradlew clean build
```
```shell
Go to the build/libs folder:
cd /build/libs
```
```shell
Windows:
%GRAALVM_HOME%\bin\native-image.cmd -jar discocli-17.0.3.jar --no-server -H:Name=discocli --no-fallback --static --enable-http --enable-https

Linux:
$GRAALVM_HOME/bin/native-image -jar discocli-17.0.3.jar --no-server -H:Name=discocli --no-fallback --static --enable-http --enable-https

Macos:
$GRAALVM_HOME/bin/native-image -jar ./discocli-17.0.3.jar --no-server -H:Name=discocli --no-fallback --enable-http --enable-https
```

#### Usage
```
discocli [-h] [-d=<d>] [-v=<v>] [-os=<os>] [-lc=<lc>] [-arc=<arc>] [-at=<at>] [-pt=<pt>] [-p=<p>] [-ea] [-fx] [-f] [-latest] [-i]

Download a JDK pkg defined by the given parameters:

-h,   --help                  Show help

-d,   --distribution=<d>      Distribution (e.g. zulu, temurin, etc.)

-v,   --version=<v>           Version number (e.g. 17.0.2)

-os,  --operating-system=<os> Operating system (e.g. windows, linux, macos)

-lc,  --libc-type=<lc>        Lib C type (libc, glibc, c_std_lib, musl)

-arc, --architecture=<arc>    Architecture (e.g. x64, aarch64)

-at,  --archive-type=<at>     Archive tpye (e.g. tar.gz, zip)

-pt,  --package-type=<pt>     Package type (e.g. jdk, jre)

-p,   --path=<p>              The path where the JDK pkg should be saved to (e.g. /Users/hansolo)

-ea,  --early-access          Include early access builds

-fx,  --javafx                Bundled with JavaFX

-f,   --find                  Find available JDK pkgs for given parameters

-latest                       Latest available for given version number

-i,   --info                  Info about parameters
```

#### Parameters
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