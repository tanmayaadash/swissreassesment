package com.company.service;

import com.company.exceptions.ValidationException;
import com.company.model.Employee;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface EmployeeService {

    void loadEmployees(Path path) throws IOException, ValidationException;

    List<Employee> getEmployees();

    Map<String, List<String>> validateSalaries();

    List<String> validateReportingLines(int limit);

    Employee getCeo();
}
