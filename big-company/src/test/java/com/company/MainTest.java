package com.company;

import com.company.service.EmployeeService;
import com.company.service.EmployeeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MainTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private final PrintStream originalErr = System.err;

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        System.setErr(originalErr);
    }

    @Test
    public void testMainPrintsUnderAndOverAndLongLines_whenServiceReportsThem() throws Exception {

        Path tempFile = Files.createTempFile("emps", ".csv");
        Files.writeString(tempFile, "Id,firstName,lastName,salary,managerId\n100,John,CEO,150000,\n");
        tempFile.toFile().deleteOnExit();

        EmployeeService mockedService = mock(EmployeeService.class);

        Map<String, List<String>> salaryMap = Map.of(
                "underpaid", List.of("UNDERPAID: Alice (300) is underpaid by 42000.00"),
                "overpaid", List.of("OVERPAID: Paul (700) is overpaid by 84000.00")
        );
        when(mockedService.validateSalaries()).thenReturn(salaryMap);
        when(mockedService.validateReportingLines(4)).thenReturn(List.of("LONG: Olivia (600) by 1"));
        doNothing().when(mockedService).loadEmployees(any());

        try (MockedConstruction<EmployeeServiceImpl> mc = mockConstruction(EmployeeServiceImpl.class,
                (constructed, context) -> {
                    when(constructed.validateSalaries()).thenAnswer(inv -> mockedService.validateSalaries());
                    when(constructed.validateReportingLines(4)).thenAnswer(inv -> mockedService.validateReportingLines(4));
                    doAnswer(inv -> {
                        mockedService.loadEmployees(inv.getArgument(0));
                        return null;
                    }).when(constructed).loadEmployees(any());
                })) {
            String input = tempFile.toString() + System.lineSeparator();
            ByteArrayInputStream inContent = new ByteArrayInputStream(input.getBytes());
            System.setIn(inContent);

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream psOut = new PrintStream(outContent);
            System.setOut(psOut);

            Main.main(new String[0]);
            String output = outContent.toString();

            assertTrue(output.contains("UNDERPAID employees: 1"), "Should show 1 underpaid");
            assertTrue(output.contains("OVERPAID employees: 1"), "Should show 1 overpaid");
            assertTrue(output.contains("UNDERPAID: Alice (300)"), "Should print underpaid detail");
            assertTrue(output.contains("OVERPAID: Paul (700)"), "Should print overpaid detail");
            assertTrue(output.contains("=== Reporting Line Issues ==="));
            assertTrue(output.contains("LONG: Olivia (600) by 1"));
            assertEquals(1, mc.constructed().size());
        }
    }

    @Test
    public void testMainFileNotFoundMessage_whenFileMissing() throws Exception {
        String fakePath = Path.of("nonexistent-file-xyz.csv").toAbsolutePath().toString();
        ByteArrayInputStream inContent = new ByteArrayInputStream((fakePath + System.lineSeparator()).getBytes());
        System.setIn(inContent);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        Main.main(new String[0]);

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("File not found") || errOutput.toLowerCase().contains("failure"),
                "Should print file-not-found related error to stderr");
    }
}
