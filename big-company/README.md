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
  - **Underpaid** if salary < 80% of average manager salary  
  - **Overpaid** if salary > 120% of average manager salary  

### ✔ Reporting Line Validation
- Counts managers **between** an employee and the CEO.
- Flags employees with a reporting depth greater than a configurable limit (default = 2).

### ✔ Modular Architecture
- CSV reading, parsing, validation, and hierarchy-building are separated for clarity.
- Well-structured `EmployeeService` interface + implementation.
- Ready for extension (e.g., JSON, database, multiple input formats).

---

## 📂 Project Structure

