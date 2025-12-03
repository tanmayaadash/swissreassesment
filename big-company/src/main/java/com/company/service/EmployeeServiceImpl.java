package com.company.service;

import com.company.exceptions.ValidationException;
import com.company.model.Employee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service that loads employees from a CSV file and performs validations & reports.
 *
 * Assumptions:
 * - CSV is expected to have a header with columns: Id,firstName,lastName,salary,managerId (not sorted)
 * - Fields must not contain embedded commas. (Simple CSV parser used for clarity)
 * - Salary must be a non-negative number.
 * - There must be exactly one CEO (employee with no manager).
 * - Manager references must point to existing employee IDs.
 * - Assuming there are no cycle in hierarchy. If a cycle is present then improvement needed to code to handle edge cases.
 * - Salary validation assumption: The range of min and max salary is calculated based average of all the employees who are managers.
 * Comparison of underpaid and overpaid is done with all managers and not just peers.
 * A manager is underpaid if their salary is less 20% than the average manager salary.
 * A manager is overpaid if their salary is more 20% than the average manager salary.
 * - Reporting line validation: A reporting line is considered long
 * if there are more than 2 managers between an employee and the CEO.
 * Currently passing the limit as 2 from main method. can take as an argument from console input
 * if needed.
 */
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger LOG = Logger.getLogger(EmployeeServiceImpl.class.getName());

    private final Map<String, Employee> employeeMap = new LinkedHashMap<>();
    private Employee ceo;

    @Override
    public void loadEmployees(Path path) throws IOException, ValidationException {
        try (BufferedReader r = Files.newBufferedReader(path)) {
            loadFromFile(r);
        }
    }

    @Override
    public List<Employee> getEmployees() {
        return employeeMap.values().stream().toList();
    }

    public void loadFromFile(Reader reader) throws IOException, ValidationException {
        employeeMap.clear();
        ceo = null;

        List<String> lines = readCsvLines(reader);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;

            String[] parts = parseRow(line, i);
            validateRow(parts, i);

            Employee e = createEmployee(parts);
            employeeMap.put(e.getId(), e);
        }

        linkHierarchy();
    }

    private List<String> readCsvLines(Reader reader) throws IOException, ValidationException {
        List<String> result = new ArrayList<>();

        try (BufferedReader br = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader)) {

            String line = br.readLine(); // header
            if (line == null) throw new ValidationException("CSV file is empty");

            result.add(line);

            while ((line = br.readLine()) != null) {
                result.add(line.trim());
            }
        }

        return result;
    }

    private String[] parseRow(String line, int lineNo) throws ValidationException {
        String[] parts = line.split(",", -1);

        if (parts.length < 4) {
            throw new ValidationException("Malformed line " + lineNo + ": expected at least 4 columns");
        }
        return parts;
    }

    private void validateRow(String[] parts, int lineNo) throws ValidationException {
        String id = parts[0].trim();
        String salaryStr = parts[3].trim();

        if (id.isEmpty()) {
            throw new ValidationException("Empty id at line " + lineNo);
        }

        if (employeeMap.containsKey(id)) {
            throw new ValidationException("Duplicate id '" + id + "' at line " + lineNo);
        }

        try {
            double salary = Double.parseDouble(salaryStr);
            if (salary < 0) throw new ValidationException("Negative salary at line " + lineNo);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Invalid salary for id " + id + " at line " + lineNo);
        }
    }
    private Employee createEmployee(String[] parts) {
        String id = parts[0].trim();
        String firstName = parts[1].trim();
        String lastName = parts[2].trim();
        double salary = Double.parseDouble(parts[3].trim());
        String managerId = (parts.length > 4 ? parts[4].trim() : null);

        return new Employee(id, firstName, lastName, salary, managerId);
    }
    private void linkHierarchy() throws ValidationException {
        for (Employee e : employeeMap.values()) {
            if (e.getManagerId() == null || e.getManagerId().isEmpty()) {
                if (ceo != null) {
                    throw new ValidationException("Multiple CEOs detected");
                }
                ceo = e;
            } else {
                Employee m = employeeMap.get(e.getManagerId());
                if (m == null) {
                    throw new ValidationException("Unknown manager '" + e.getManagerId() +
                            "' for employee " + e.getId());
                }
                m.addSubordinate(e);
            }
        }

        if (ceo == null) {
            throw new ValidationException("No CEO found");
        }
    }

    public Map<String, List<String>> validateSalaries() {
        List<String> underpaid = new ArrayList<>();
        List<String> overpaid = new ArrayList<>();
        double avg = employeeMap.values().stream()
                .filter(this::isManager)
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
        // salary range +/- 20%
        double minAllowed = avg * 0.80;
        double maxAllowed = avg * 1.20;

        for (Employee manager : employeeMap.values()) {
            if (!isManager(manager)){
                continue;
            }

            if (manager.getSalary() < minAllowed) {
                String msg = String.format(
                        "%s (%s) is underpaid by %.2f",
                        manager.getFullName(),
                        manager.getId(),
                        (minAllowed - manager.getSalary())
                );
                underpaid.add(msg);

            } else if (manager.getSalary() > maxAllowed) {
                String msg = String.format(
                        "%s (%s) is overpaid by %.2f",
                        manager.getFullName(),
                        manager.getId(),
                        (manager.getSalary() - maxAllowed)
                );
                overpaid.add(msg);
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("underpaid", underpaid);
        result.put("overpaid", overpaid);
        return result;
    }

    private boolean isManager(Employee e) {
        return !e.getSubordinates().isEmpty();
    }

    /**
     * Count managers between employee and CEO (does not include CEO).
     * For an employee directly reporting to CEO this returns 0.
     */
    public int countManagersBetween(Employee employee) {
        if (employee == null) throw new IllegalArgumentException("employee null");
        int count = 0;
        Employee current = employee;
        while (current.getManagerId() != null) {
            Employee manager = employeeMap.get(current.getManagerId());
            if (manager == null || manager == ceo) break;
            count++;
            current = manager;
        }
        return count;
    }

    public List<String> validateReportingLines(int limit) {
        List<String> output = new ArrayList<>();
        for (Employee e : employeeMap.values()) {
            int between = countManagersBetween(e);
            if (between > limit) {
                String msg = String.format("LONG: %s (%s) by %d", e.getFullName(), e.getId(), (between - limit));
                output.add(msg);
            }
        }
        return output;
    }

    public Employee getCeo() { return ceo; }
    public Optional<Employee> getById(String id) { return Optional.ofNullable(employeeMap.get(id)); }
    public Collection<Employee> getAll() { return Collections.unmodifiableCollection(employeeMap.values()); }
}
