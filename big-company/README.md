# big-company
Simple Maven project to validate company org structure and salary rules.

How to build:
```
mvn clean package
```

How to run:
```
java -cp target/big-company-1.0-SNAPSHOT.jar com.company.Main path/to/employees.csv
```

Notes:
- Logging uses java.util.logging (no external dependencies).
- Validation includes malformed lines, duplicate IDs, missing manager references, cycles and negative salaries.
