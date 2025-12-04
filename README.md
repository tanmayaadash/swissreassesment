# Employee Salary & Reporting Line Validator

This project validates employees from a CSV file based on two business rules:

1. **Salary Validation**  
2. **Reporting Line Validation**

It also includes CSV parsing, hierarchy building, underpaid/overpaid manager detection, and reporting-line depth checks.

---

## 📌 Features

### ✔ CSV Parsing & Validation
- Validates structure, columns, salary format, and uniqueness.
- Detects missing CEO, duplicate IDs, and unknown managers.
- Ensures non-negative salary values.

### ✔ Salary Compliance Rules
- Only employees **who manage others** are considered managers.
- Average manager salary is computed across all managers.
- A manager is:
  - **Underpaid** if salary < 120% of average subordinate salary  
  - **Overpaid** if salary > 150% of average subordinate salary  

### ✔ Reporting Line Validation
- Counts managers **between** an employee and the CEO.
- Flags employees with a reporting depth greater than a configurable limit (default = 4).

### ✔ Modular Architecture
- CSV reading, parsing, validation, and hierarchy-building are separated for clarity.
- Well-structured `EmployeeService` interface + implementation.
- Ready for extension (e.g., JSON, database, multiple input formats).

---

Assumptions

1. CSV Structure

Must include a header row.
Rows represent one employee each.
No embedded commas allowed in fields.

2. Employee Rules

ID must not be empty.
IDs must be unique.
Salary must be a valid number ≥ 0.
Only one CEO allowed.

3. Hierarchy Rules

Each employee’s managerId must reference an existing employee.
No cycles assumed (cycle detection not implemented).

4. Salary Validation Rules

Only employees with subordinates are considered managers.
Average salary computed across all subordinates of a manager.
Underpaid → < 120% of average subordinate salary.
Overpaid → > 150% of average manager salary.

5. Reporting Line Validation

A reporting line is too long if manager depth exceeds the given limit.
Default limit = 4

