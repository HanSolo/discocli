## Disco CLI

Disco CLI is a command line interface for the foojay.io Disco API


####Help:
java -jar discocli-17.0.0.jar -h
<br>

####Info:
java -jar discocli-17.0.0.jar -i
<br>

####Example usage of jar file (needs JDK 17):
java -jar discocli-17.0.0.jar -d zulu -v 17.0.2 -fx -os macos
<br>

####Build native image with GraalVM:
```shell
./gradlew clean build
```
```shell
cd /build/libs
```
```shell
/PATH/TO/GRALLVM_FOLDER/bin/native-image -cp classes:discocli-17.0.0.jar --no-server -H:Name=discocli eu.hansolo.discocli.DiscoCLI --enable-all-security-services --no-fallback --enable-http --enable-https
/Library/Java/JavaVirtualMachines/graalvm-svm-java17-darwin-gluon-22.0.0.3-Final/Contents/Home/bin/native-image -cp classes:discocli-17.0.0.jar --no-server -H:Name=discocli eu.hansolo.discocli.DiscoCLI --enable-all-security-services --no-fallback --enable-http --enable-https
```