## Disco CLI

Disco CLI is a command line interface for the foojay.io Disco API

Supported distributions:
java -jar discocli-17.0.0.jar -i

Example usage:
java -jar discocli-17.0.0.jar -d zulu -v 17.0.2 -fx -os macos

Build native image with GraalVM:
/Library/Java/JavaVirtualMachines/graalvm-svm-java17-darwin-gluon-22.0.0.3-Final/Contents/Home/bin : ./native-image

/Library/Java/JavaVirtualMachines/graalvm-svm-java17-darwin-gluon-22.0.0.3-Final/Contents/Home/bin/native-image -cp classes:discocli-17.0.0.jar --no-server  -H:Name=discocli eu.hansolo.discocli.DiscoCLI -H:+PrintAnalysisCallTree