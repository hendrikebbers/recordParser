package org.example.parser;

import com.swirlds.config.api.ConfigData;
import com.swirlds.config.api.ConfigProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Annotation;
import org.jboss.forge.roaster.model.JavaRecord;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class Sample {

    public static void main(String[] args) throws Exception {
        JavaType<?> type = Roaster.parse(Sample.class.getResourceAsStream("EventConfig.java"));

        Annotation<?> annotation = type.getAnnotation(ConfigData.class);
        String configDataValue = annotation.getStringValue("value");


        Map<String, String> paramDoc = new HashMap<>();
        type.getJavaDoc().getTags("@param").forEach(tag -> {
            paramDoc.put(tag.getName(), tag.getValue());
        });

        JavaRecord<?> record = (JavaRecord<?>) type;
        Set<ConfigDataPropertyDefinition> propertyDefinitions = record.getRecordComponents().stream().map(javaRecordComponent -> {
            String name = javaRecordComponent.getName();
            String propertyDefaultValue = null;
            String propertyDescription = paramDoc.get(name);
            Annotation configPropertyAnnotation = javaRecordComponent.getAnnotation(ConfigProperty.class);
            if (configPropertyAnnotation != null) {
                String annotationValue = configPropertyAnnotation.getStringValue("value");
                if (annotationValue != null) {
                    name = annotationValue;
                }
                String annotationDefaultValue = configPropertyAnnotation.getStringValue("defaultValue");
                if (annotationDefaultValue != null && !Objects.equals(annotationDefaultValue,
                        ConfigProperty.NULL_DEFAULT_VALUE)) {
                    propertyDefaultValue = annotationDefaultValue;
                }
            }
            String propertyName = name;
            if (configDataValue != null) {
                propertyName = configDataValue + "." + name;
            }
            String propertyType = javaRecordComponent.getType().getQualifiedName();

            return new ConfigDataPropertyDefinition(
                    propertyName,
                    propertyType,
                    propertyDefaultValue,
                    propertyDescription
            );
        }).collect(Collectors.toSet());

        ConfigDataRecordDefinition configDataRecordDefinition = new ConfigDataRecordDefinition(
                type.getQualifiedName(),
                configDataValue,
                propertyDefinitions
        );

        System.out.println("Result for config data record '" + configDataRecordDefinition.className()   +   "':");
        configDataRecordDefinition.propertyDefinitions().forEach(propertyDefinition -> {
            System.out.println("  " + propertyDefinition.name() + " (" + propertyDefinition.type() + "):");
            System.out.println("    default value: " + propertyDefinition.defaultValue());
            System.out.println("    description: " + propertyDefinition.description());
        });





        JavaClassSource javaClassSource = Roaster.create(JavaClassSource.class);
        javaClassSource.setPackage(type.getPackage())
                .setName(type.getName() + "Constants")
                .setFinal(true);

        configDataRecordDefinition.propertyDefinitions().forEach(propertyDefinition -> {
            String name = toConstantName(propertyDefinition.name().replace(configDataRecordDefinition.configDataName()+".",""));
            javaClassSource.addField()
                    .setName(name)
                    .setType("java.lang.String")
                    .setLiteralInitializer("\"" + propertyDefinition.name() + "\"")
                    .setPublic()
                    .setStatic(true);
        });

        System.out.println("TestFixture constants class for config data record '" + configDataRecordDefinition.className()   +   "':");
        System.out.println(javaClassSource);
    }

    public static String toConstantName(String propertyName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < propertyName.length(); i++) {
            char character = propertyName.charAt(i);
            if(Character.isUpperCase(character)) {
                builder.append("_");
                builder.append(character);
            } else {
                builder.append(Character.toUpperCase(character));
            }
        }
        return builder.toString();
    }

}
