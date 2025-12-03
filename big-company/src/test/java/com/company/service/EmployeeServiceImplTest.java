package com.company.service;

import com.company.exceptions.ValidationException;
import com.company.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceImplTest {

    private static final String BASIC_CSV = """
        Id,firstName,lastName,salary,managerId
        100,John,CEO,150000,
        101,Sarah,Smith,110000,100
        102,Ravi,Kumar,105000,101
        103,Alice,White,80000,102
        104,Bob,Brown,60000,103
        """;

    @Test
    void testLoadEmployeesAndCeo() throws Exception {
        EmployeeServiceImpl svc = new EmployeeServiceImpl();
        svc.loadFromFile(new StringReader(BASIC_CSV));

        assertNotNull(svc.getCeo(), "CEO should not be null");
        assertEquals("100", svc.getCeo().getId(), "CEO ID mismatch");

        assertEquals(5, svc.getEmployees().size(), "Total employees mismatch");
    }

    @Test
    void testHierarchyLinking() throws Exception {
        EmployeeServiceImpl svc = new EmployeeServiceImpl();
        svc.loadFromFile(new StringReader(BASIC_CSV));

        Employee e104 = svc.getById("104").orElseThrow();
        assertEquals("103", e104.getManagerId());

        Employee e103 = svc.getById("103").orElseThrow();
        assertEquals(1, e103.getSubordinates().size(), "Manager 103 should have 1 subordinate");
    }

    @Test
    void testUnderpaidOverpaidLogic() throws Exception {
        String csv = """
                Id,firstName,lastName,salary,managerId
                100,John,CEO,150000,
                101,Sarah,Smith,20000,100
                102,Ravi,Kumar,300000,100
                103,Worker,One,50000,102
            """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();
        svc.loadFromFile(new StringReader(csv));

        Map<String, List<String>> result = svc.validateSalaries();

        assertEquals(1, result.get("underpaid").size());
        assertEquals(1, result.get("overpaid").size());
    }

    @Test
    void testReportingLineValidation() throws Exception {
        String csv = """
        Id,firstName,lastName,salary,managerId
        1,CEO,One,200000,
        2,L2,Manager,150000,1
        3,L3,Manager,140000,2
        4,L4,Manager,130000,3
        5,L5,Manager,120000,4
        6,Worker,Six,50000,5
        """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();
        svc.loadFromFile(new StringReader(csv));

        List<String> issues = svc.validateReportingLines(2);

        assertEquals(2, issues.size());
        assertTrue(issues.get(0).contains("LONG"));
        assertTrue(issues.get(1).contains("6")); // ensure correct employee flagged
    }


    @Test
    void testNegativeSalaryValidation() {
        String badCsv = """
            Id,firstName,lastName,salary,managerId
            1,A,B,-20000,
            """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();

        Exception ex = assertThrows(ValidationException.class,
                () -> svc.loadFromFile(new StringReader(badCsv)));

        assertTrue(ex.getMessage().toLowerCase().contains("negative salary"));
    }

    @Test
    void testInvalidSalaryFormat() {
        String badCsv = """
            Id,firstName,lastName,salary,managerId
            1,A,B,hello,
            """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();

        Exception ex = assertThrows(ValidationException.class,
                () -> svc.loadFromFile(new StringReader(badCsv)));

        assertTrue(ex.getMessage().toLowerCase().contains("invalid salary"));
    }

    @Test
    void testDuplicateIdValidation() {
        String badCsv = """
            Id,firstName,lastName,salary,managerId
            1,A,B,1000,
            1,C,D,2000,
            """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();

        Exception ex = assertThrows(ValidationException.class,
                () -> svc.loadFromFile(new StringReader(badCsv)));

        assertTrue(ex.getMessage().toLowerCase().contains("duplicate id"));
    }

    @Test
    void testUnknownManagerValidation() {
        String badCsv = """
            Id,firstName,lastName,salary,managerId
            1,A,B,1000,
            2,C,D,2000,99
            """;

        EmployeeServiceImpl svc = new EmployeeServiceImpl();

        Exception ex = assertThrows(ValidationException.class,
                () -> svc.loadFromFile(new StringReader(badCsv)));

        assertTrue(ex.getMessage().toLowerCase().contains("unknown manager"));
    }
}
