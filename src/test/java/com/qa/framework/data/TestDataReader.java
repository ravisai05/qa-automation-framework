package com.qa.framework.data;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.framework.api.request.BookingRequest;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads static test data from src/test/resources/testdata:
 * JSON (users, booking payloads) via Jackson and Excel (checkout form cases) via Apache POI.
 * Stateless apart from the thread-safe ObjectMapper, so it can be used from parallel scenarios.
 */
public final class TestDataReader {

    private static final String DATA_DIR = "testdata/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataReader() {
    }

    public static UserAccount user(String alias) {
        return entry(readJsonMap("users.json", UserAccount.class), alias, "users.json");
    }

    public static BookingRequest booking(String key) {
        return entry(readJsonMap("bookings.json", BookingRequest.class), key, "bookings.json");
    }

    public static CheckoutDataRow checkoutRow(String dataKey) {
        return readExcelSheet("checkout-data.xlsx", "checkout").stream()
                .filter(row -> dataKey.equals(row.get("dataKey")))
                .findFirst()
                .map(row -> new CheckoutDataRow(dataKey,
                        new CheckoutCustomer(row.get("firstName"), row.get("lastName"), row.get("postalCode")),
                        row.get("expectedError")))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No row with dataKey '" + dataKey + "' in checkout-data.xlsx"));
    }

    /** Top-level JSON object whose values all map to {@code type}. */
    static <T> Map<String, T> readJsonMap(String fileName, Class<T> type) {
        JavaType mapType = MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, type);
        try (InputStream in = open(fileName)) {
            return MAPPER.readValue(in, mapType);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse " + DATA_DIR + fileName, e);
        }
    }

    /** First row = column headers; every following non-empty row becomes header -> cell text. */
    static List<Map<String, String>> readExcelSheet(String fileName, String sheetName) {
        DataFormatter formatter = new DataFormatter();
        try (InputStream in = open(fileName); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalStateException("Sheet '" + sheetName + "' not found in " + fileName);
            }
            Row header = sheet.getRow(0);
            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    values.put(formatter.formatCellValue(header.getCell(c)).trim(),
                            formatter.formatCellValue(row.getCell(c)).trim());
                }
                rows.add(values);
            }
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + DATA_DIR + fileName, e);
        }
    }

    private static InputStream open(String fileName) {
        InputStream in = TestDataReader.class.getClassLoader().getResourceAsStream(DATA_DIR + fileName);
        if (in == null) {
            throw new IllegalStateException(DATA_DIR + fileName + " not found on the classpath");
        }
        return in;
    }

    private static <T> T entry(Map<String, T> data, String key, String fileName) {
        T value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("'" + key + "' not found in " + fileName + ". Available: " + data.keySet());
        }
        return value;
    }
}
