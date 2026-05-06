package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.BulkUserImportRequest;
import com.rs.attendanceManager.Dto.BulkUserImportResponse;
import com.rs.attendanceManager.Dto.CreateUserRequest;
import com.rs.attendanceManager.Dto.UpdateUserRequest;
import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Repo.UserRepo;
import com.rs.attendanceManager.exception.ConflictException;
import com.rs.attendanceManager.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Set<String> REQUIRED_IMPORT_HEADERS = Set.of(
            "grno",
            "name",
            "department",
            "subdepartment",
            "totalattendance",
            "mobilenumber",
            "area",
            "age",
            "isinitiated"
    );

    private final UserRepo userRepo;
    private final Validator validator;

    public UserServiceImpl(UserRepo userRepo, Validator validator) {
        this.userRepo = userRepo;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto fetchUserByGrNo(String grNo) {
        User user = userRepo.findById(grNo)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for grNo: " + grNo));
        return new UserDto(user);
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepo.existsById(request.getGrNo())) {
            throw new ConflictException("User already exists for grNo: " + request.getGrNo());
        }

        User user = new User();
        user.setGrNo(request.getGrNo().trim());
        applyUserFields(request, user);

        User saved = userRepo.save(user);
        return new UserDto(saved);
    }

    @Override
    public UserDto updateUser(String grNo, UpdateUserRequest request) {
        User existingUser = userRepo.findById(grNo)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for grNo: " + grNo));

        existingUser.setName(request.getName().trim());
        existingUser.setDepartment(request.getDepartment().trim());
        existingUser.setSubDepartment(request.getSubDepartment().trim());
        existingUser.setTotalAttendance(request.getTotalAttendance());
        existingUser.setMobileNumber(request.getMobileNumber().trim());
        existingUser.setArea(request.getArea().trim());
        existingUser.setAge(request.getAge());
        existingUser.setIsInitiated(request.getIsInitiated());
        existingUser.setRemarks(request.getRemarks());
        existingUser.setEmail(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase());

        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            existingUser.setPhoto(readPhotoBytes(request.getPhoto()));
        }

        User updated = userRepo.save(existingUser);
        return new UserDto(updated);
    }

    @Override
    public void deleteUser(String grNo) {
        if (!userRepo.existsById(grNo)) {
            throw new ResourceNotFoundException("User not found for grNo: " + grNo);
        }
        userRepo.deleteById(grNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> fetchAllUsers() {
        return userRepo.findAll().stream().map(UserDto::new).toList();
    }

    @Override
    public BulkUserImportResponse importUsers(MultipartFile file) {
        validateImportFile(file);

        List<ImportedUserRow> importedRows = parseImportFile(file);
        if (importedRows.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file does not contain any user rows");
        }

        validateImportedRows(importedRows);
        ensureNoDuplicateGrNosInFile(importedRows);
        ensureUsersDoNotAlreadyExist(importedRows);

        List<User> usersToSave = importedRows.stream()
                .map(importedRow -> toUser(importedRow.request()))
                .toList();

        List<User> savedUsers = userRepo.saveAll(usersToSave);
        List<String> importedGrNos = savedUsers.stream()
                .map(User::getGrNo)
                .toList();

        return new BulkUserImportResponse(
                "Imported " + savedUsers.size() + " users successfully",
                savedUsers.size(),
                importedGrNos
        );
    }

    private void applyUserFields(CreateUserRequest request, User user) {
        user.setName(request.getName().trim());
        user.setDepartment(request.getDepartment().trim());
        user.setSubDepartment(request.getSubDepartment().trim());
        user.setTotalAttendance(request.getTotalAttendance());
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setArea(request.getArea().trim());
        user.setAge(request.getAge());
        user.setIsInitiated(request.getIsInitiated());
        user.setRemarks(request.getRemarks());
        user.setEmail(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase());

        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            user.setPhoto(readPhotoBytes(request.getPhoto()));
        }
    }

    private User toUser(BulkUserImportRequest request) {
        User user = new User();
        user.setGrNo(request.getGrNo().trim());
        user.setName(request.getName().trim());
        user.setDepartment(request.getDepartment().trim());
        user.setSubDepartment(request.getSubDepartment().trim());
        user.setTotalAttendance(request.getTotalAttendance());
        user.setPhoto(request.getPhoto());
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setArea(request.getArea().trim());
        user.setAge(request.getAge());
        user.setIsInitiated(request.getIsInitiated());
        user.setRemarks(normalizeOptionalText(request.getRemarks()));
        user.setEmail(normalizeOptionalEmail(request.getEmail()));
        return user;
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty CSV or Excel file");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Uploaded file name is missing");
        }

        String normalizedFilename = filename.toLowerCase(Locale.ROOT);
        if (!normalizedFilename.endsWith(".csv")
                && !normalizedFilename.endsWith(".xlsx")
                && !normalizedFilename.endsWith(".xls")) {
            throw new IllegalArgumentException("Only .csv, .xlsx, and .xls files are supported");
        }
    }

    private List<ImportedUserRow> parseImportFile(MultipartFile file) {
        String filename = file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".csv")) {
            return parseCsvFile(file);
        }
        return parseExcelFile(file);
    }

    private List<ImportedUserRow> parseCsvFile(MultipartFile file) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVParser parser = csvFormat.parse(reader)
        ) {
            Map<String, String> headerLookup = normalizeHeaders(parser.getHeaderMap().keySet());
            validateHeaders(headerLookup.keySet());

            List<ImportedUserRow> importedRows = new ArrayList<>();
            for (CSVRecord record : parser) {
                BulkUserImportRequest request = mapImportedRow(
                        (int) record.getRecordNumber() + 1,
                        getCsvValue(record, headerLookup, "grno"),
                        getCsvValue(record, headerLookup, "name"),
                        getCsvValue(record, headerLookup, "department"),
                        getCsvValue(record, headerLookup, "subdepartment"),
                        getCsvValue(record, headerLookup, "totalattendance"),
                        getCsvValue(record, headerLookup, "photo"),
                        getCsvValue(record, headerLookup, "mobilenumber"),
                        getCsvValue(record, headerLookup, "area"),
                        getCsvValue(record, headerLookup, "age"),
                        getCsvValue(record, headerLookup, "isinitiated"),
                        getCsvValue(record, headerLookup, "remarks"),
                        getCsvValue(record, headerLookup, "email")
                );
                if (isBlankRow(request)) {
                    continue;
                }
                importedRows.add(new ImportedUserRow((int) record.getRecordNumber() + 1, request));
            }
            return importedRows;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the uploaded CSV file");
        }
    }

    private List<ImportedUserRow> parseExcelFile(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return List.of();
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("The uploaded Excel file does not contain a header row");
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerLookup = extractExcelHeaders(headerRow, formatter);
            validateHeaders(headerLookup.keySet());

            List<ImportedUserRow> importedRows = new ArrayList<>();
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                BulkUserImportRequest request = mapImportedRow(
                        rowIndex + 1,
                        getExcelValue(row, headerLookup, formatter, "grno"),
                        getExcelValue(row, headerLookup, formatter, "name"),
                        getExcelValue(row, headerLookup, formatter, "department"),
                        getExcelValue(row, headerLookup, formatter, "subdepartment"),
                        getExcelValue(row, headerLookup, formatter, "totalattendance"),
                        getExcelValue(row, headerLookup, formatter, "photo"),
                        getExcelValue(row, headerLookup, formatter, "mobilenumber"),
                        getExcelValue(row, headerLookup, formatter, "area"),
                        getExcelValue(row, headerLookup, formatter, "age"),
                        getExcelValue(row, headerLookup, formatter, "isinitiated"),
                        getExcelValue(row, headerLookup, formatter, "remarks"),
                        getExcelValue(row, headerLookup, formatter, "email")
                );
                if (isBlankRow(request)) {
                    continue;
                }
                importedRows.add(new ImportedUserRow(rowIndex + 1, request));
            }
            return importedRows;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the uploaded Excel file");
        }
    }

    private BulkUserImportRequest mapImportedRow(
            int rowNumber,
            String grNo,
            String name,
            String department,
            String subDepartment,
            String totalAttendance,
            String photo,
            String mobileNumber,
            String area,
            String age,
            String isInitiated,
            String remarks,
            String email
    ) {
        BulkUserImportRequest request = new BulkUserImportRequest();
        request.setGrNo(normalizeRequiredText(grNo));
        request.setName(normalizeRequiredText(name));
        request.setDepartment(normalizeRequiredText(department));
        request.setSubDepartment(normalizeRequiredText(subDepartment));
        request.setTotalAttendance(parseBigDecimal(totalAttendance, "totalAttendance", rowNumber));
        request.setPhoto(parsePhoto(photo, rowNumber));
        request.setMobileNumber(normalizeRequiredText(mobileNumber));
        request.setArea(normalizeRequiredText(area));
        request.setAge(parseInteger(age, "age", rowNumber));
        request.setIsInitiated(parseBoolean(isInitiated, rowNumber));
        request.setRemarks(normalizeOptionalText(remarks));
        request.setEmail(normalizeOptionalEmail(email));
        return request;
    }

    private void validateImportedRows(List<ImportedUserRow> importedRows) {
        List<String> validationErrors = new ArrayList<>();
        for (ImportedUserRow importedRow : importedRows) {
            Set<ConstraintViolation<BulkUserImportRequest>> violations = validator.validate(importedRow.request());
            for (ConstraintViolation<BulkUserImportRequest> violation : violations) {
                validationErrors.add("Row " + importedRow.rowNumber() + ": " + violation.getMessage());
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", validationErrors));
        }
    }

    private void ensureNoDuplicateGrNosInFile(List<ImportedUserRow> importedRows) {
        Set<String> seenGrNos = new LinkedHashSet<>();
        Set<String> duplicateGrNos = new LinkedHashSet<>();

        for (ImportedUserRow importedRow : importedRows) {
            String grNo = importedRow.request().getGrNo().trim();
            if (!seenGrNos.add(grNo)) {
                duplicateGrNos.add(grNo);
            }
        }

        if (!duplicateGrNos.isEmpty()) {
            throw new IllegalArgumentException("Duplicate grNo values found in the uploaded file: "
                    + String.join(", ", duplicateGrNos));
        }
    }

    private void ensureUsersDoNotAlreadyExist(List<ImportedUserRow> importedRows) {
        List<String> grNos = importedRows.stream()
                .map(importedRow -> importedRow.request().getGrNo().trim())
                .toList();

        Set<String> existingGrNos = new LinkedHashSet<>();
        for (User existingUser : userRepo.findAllById(grNos)) {
            existingGrNos.add(existingUser.getGrNo());
        }

        if (!existingGrNos.isEmpty()) {
            throw new ConflictException("Users already exist for grNos: " + String.join(", ", existingGrNos));
        }
    }

    private Map<String, String> normalizeHeaders(Collection<String> headers) {
        Map<String, String> normalizedHeaders = new LinkedHashMap<>();
        for (String header : headers) {
            if (header == null) {
                continue;
            }
            normalizedHeaders.put(normalizeHeader(header), header);
        }
        return normalizedHeaders;
    }

    private Map<String, Integer> extractExcelHeaders(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        int lastCellNumber = headerRow.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCellNumber; cellIndex++) {
            String value = formatter.formatCellValue(headerRow.getCell(cellIndex));
            if (value != null && !value.isBlank()) {
                headers.put(normalizeHeader(value), cellIndex);
            }
        }
        return headers;
    }

    private void validateHeaders(Set<String> providedHeaders) {
        List<String> missingHeaders = REQUIRED_IMPORT_HEADERS.stream()
                .filter(requiredHeader -> !providedHeaders.contains(requiredHeader))
                .sorted()
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException("Missing required columns: " + String.join(", ", missingHeaders));
        }
    }

    private String getCsvValue(CSVRecord record, Map<String, String> headerLookup, String header) {
        String actualHeader = headerLookup.get(header);
        if (actualHeader == null || !record.isMapped(actualHeader)) {
            return null;
        }
        return record.get(actualHeader);
    }

    private String getExcelValue(Row row, Map<String, Integer> headerLookup, DataFormatter formatter, String header) {
        if (row == null) {
            return null;
        }

        Integer cellIndex = headerLookup.get(header);
        if (cellIndex == null) {
            return null;
        }

        return formatter.formatCellValue(row.getCell(cellIndex));
    }

    private String normalizeHeader(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String normalizeOptionalEmail(String value) {
        String normalizedValue = normalizeOptionalText(value);
        return normalizedValue == null ? null : normalizedValue.toLowerCase(Locale.ROOT);
    }

    private BigDecimal parseBigDecimal(String value, String fieldName, int rowNumber) {
        String normalizedValue = normalizeOptionalText(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Row " + rowNumber + ": invalid " + fieldName + " value");
        }
    }

    private Integer parseInteger(String value, String fieldName, int rowNumber) {
        String normalizedValue = normalizeOptionalText(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return Integer.valueOf(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Row " + rowNumber + ": invalid " + fieldName + " value");
        }
    }

    private Boolean parseBoolean(String value, int rowNumber) {
        String normalizedValue = normalizeOptionalText(value);
        if (normalizedValue == null) {
            return null;
        }

        return switch (normalizedValue.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "y", "1" -> true;
            case "false", "no", "n", "0" -> false;
            default -> throw new IllegalArgumentException("Row " + rowNumber + ": invalid isInitiated value");
        };
    }

    private byte[] parsePhoto(String value, int rowNumber) {
        String normalizedValue = normalizeOptionalText(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Row " + rowNumber + ": photo must be a valid Base64 string");
        }
    }

    private boolean isBlankRow(BulkUserImportRequest request) {
        return request.getGrNo() == null
                && request.getName() == null
                && request.getDepartment() == null
                && request.getSubDepartment() == null
                && request.getTotalAttendance() == null
                && request.getPhoto() == null
                && request.getMobileNumber() == null
                && request.getArea() == null
                && request.getAge() == null
                && request.getIsInitiated() == null
                && request.getRemarks() == null
                && request.getEmail() == null;
    }

    private byte[] readPhotoBytes(MultipartFile photo) {
        try {
            return photo.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read uploaded photo");
        }
    }

    private record ImportedUserRow(int rowNumber, BulkUserImportRequest request) {
    }
}
