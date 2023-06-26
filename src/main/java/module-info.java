module com.example {
    requires java.compiler;
    requires com.swirlds.config;
    requires roaster.api;

    exports com.example.processor;

    provides javax.annotation.processing.Processor with com.example.processor.ConfigDataAnnotationProcessor;
}