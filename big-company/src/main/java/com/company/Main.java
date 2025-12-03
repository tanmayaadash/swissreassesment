package com.company;

import com.company.service.EmployeeService;
import com.company.service.EmployeeServiceImpl;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the path to the employees CSV file:");
        System.out.print("> ");

        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            System.err.println("No file path provided. Exiting.");
            return;
        }

        try {
            Path csv = Paths.get(filePath);
            if (!csv.toFile().exists()) {
                throw new FileNotFoundException("File not found: " + csv.toAbsolutePath());
            }

            EmployeeService employeeService = new EmployeeServiceImpl();
            employeeService.loadEmployees(csv);

            System.out.println("\n=== Salary Violations ===");

            Map<String, List<String>> salaryMap = employeeService.validateSalaries();

            List<String> underpaid = salaryMap.get("underpaid");
            List<String> overpaid = salaryMap.get("overpaid");

            System.out.println("UNDERPAID employees: " + underpaid.size());
            if (underpaid.isEmpty()) {
                System.out.println("No employees are underpaid.");
            } else {
                underpaid.forEach(System.out::println);
            }

            System.out.println("\nOVERPAID employees: " + overpaid.size());
            if (overpaid.isEmpty()) {
                System.out.println("No employees are overpaid.");
            } else {
                overpaid.forEach(System.out::println);
            }

            System.out.println("\n=== Reporting Line Issues ===");
            List<String> longLines = employeeService.validateReportingLines(4);
            if (longLines.isEmpty()) System.out.println("None");
            else longLines.forEach(System.out::println);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error: " + ex.getMessage(), ex);
            System.err.println("Failure: " + ex.getMessage());
        }
    }
}
