package com.rs.attendanceManager.Dto;

import java.util.List;

public class BulkUserImportResponse {

    private final String message;
    private final int importedCount;
    private final List<String> importedGrNos;

    public BulkUserImportResponse(String message, int importedCount, List<String> importedGrNos) {
        this.message = message;
        this.importedCount = importedCount;
        this.importedGrNos = importedGrNos;
    }

    public String getMessage() {
        return message;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<String> getImportedGrNos() {
        return importedGrNos;
    }
}
