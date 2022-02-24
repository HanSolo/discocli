module eu.hansolo.discocli {

    // Base
    requires java.base;
    requires java.net.http;

    // 3rd party
    requires eu.hansolo.jdktools;
    requires com.google.gson;
    requires info.picocli;

    exports eu.hansolo.discocli.util;
    exports eu.hansolo.discocli;
}