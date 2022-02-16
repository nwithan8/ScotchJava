module com.easypost.scotch {
    requires java.base;
    requires jdk.unsupported;
    exports com.easypost.scotch;
    opens com.easypost.scotch to org.jetbrains.annotations;
}
