package org.example.parser;

import java.util.Set;

public record ConfigDataRecordDefinition(String className, String configDataName, Set<ConfigDataPropertyDefinition> propertyDefinitions) {
}
