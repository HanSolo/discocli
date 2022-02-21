module eu.hansolo.discocli {

    // Base
    requires java.base;
    requires java.net.http;

    // 3rd party
    requires eu.hansolo.jdktools;
    requires eu.hansolo.toolbox;
    requires com.google.gson;
    requires info.picocli;

    opens eu.hansolo.discocli.using.picoli;

    exports eu.hansolo.discocli.util;
    exports eu.hansolo.discocli;
}