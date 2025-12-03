package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final double salary;
    private final String managerId;

    private final List<Employee> subordinates = new ArrayList<>();

    public Employee(String id, String firstName, String lastName, double salary, String managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = (managerId == null || managerId.isEmpty()) ? null : managerId;
    }

    public String getId() { return id; }
    public double getSalary() { return salary; }
    public String getManagerId() { return managerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }

    public List<Employee> getSubordinates() {
        return subordinates;
    }

    public void addSubordinate(Employee e) {
        subordinates.add(e);
    }
}
