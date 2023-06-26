package com.example.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.swirlds.config.api.ConfigData")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ConfigDataAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            annotatedElements.forEach(element -> {
                System.out.println("element: " + element);

                try {
                    FileObject resource = processingEnv.getFiler()
                            .getResource(StandardLocation.SOURCE_PATH, "", "FooConfig.java");
                    new BufferedReader(resource.openReader(true)).lines().forEach(System.out::println);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    JavaFileObject sourceFile = processingEnv.getFiler()
                            .createSourceFile("com.example.processor.generated.GeneratedClass");
                    sourceFile.openWriter().append("package com.example.processor.generated;\n"
                            + "\n"
                            + "public class GeneratedClass {\n"
                            + "    public static void main(String[] args) {\n"
                            + "        System.out.println(\"Hello, World!\");\n"
                            + "    }\n"
                            + "}\n").close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return true;
    }
}