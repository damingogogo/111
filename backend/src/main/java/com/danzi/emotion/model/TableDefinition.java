package com.danzi.emotion.model;

import java.util.List;

public record TableDefinition(String name, String label, List<String> writableColumns, String defaultOrder) {
}
