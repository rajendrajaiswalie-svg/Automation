# Setup Maven and Run Tests

## ⚠️ Current Status
Maven is not installed on this system. Follow these steps to install and run tests.

## Step 1: Install Maven

### Option A: Using Chocolatey (Windows)
```powershell
# Run PowerShell as Administrator
choco install maven -y
```

### Option B: Manual Installation

1. **Download Maven**
   - Visit: https://maven.apache.org/download.cgi
   - Download latest Apache Maven (e.g., apache-maven-3.9.x-bin.zip)

2. **Extract to a folder**
   ```
   C:\Program Files\Apache\maven\
   ```

3. **Set Environment Variables**
   - Open: System Properties → Environment Variables
   - Add New Variable:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Program Files\Apache\maven`
   
   - Edit PATH variable:
     - Add: `C:\Program Files\Apache\maven\bin`

4. **Verify Installation**
   ```powershell
   mvn --version
   ```

### Option C: Using Maven Wrapper (If Available)
If the project has Maven wrapper:
```powershell
.\mvnw clean test
```

## Step 2: Set Java Environment Variables

### For Java 26 (Already Installed)

1. **Set JAVA_HOME**
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-26.0.1`

2. **Verify Java**
   ```powershell
   java -version
   javac -version
   ```

## Step 3: Run Tests

### Run All Tests
```powershell
cd d:\Work\code\Automation
mvn clean test
```

### Run Specific Test Suite
```powershell
# Run QA Agent Unit Tests
mvn test -Dtest=QAAgentTest

# Run Practical Examples
mvn test -Dtest=QAAgentPracticalExamplesTest

# Run Code Examples
mvn test -Dtest=QAAgentCodeExamplesTest
```

### Run Specific Test Method
```powershell
mvn test -Dtest=QAAgentTest#testCreateTestSuite
mvn test -Dtest=QAAgentCodeExamplesTest#example1BasicTest
```

## Step 4: Generate Test Report

### Generate with Maven
```powershell
# Run tests and generate report
mvn clean test

# Generate Surefire report
mvn surefire-report:report
```

### View Test Report
- After running: `mvn clean test`
- Report location: `target/surefire-reports/`
- HTML report: `target/site/surefire-report.html`

## Expected Output

When tests run successfully:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.automation.qa.agent.QAAgentTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s
[INFO] Running com.automation.qa.examples.QAAgentPracticalExamplesTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.8 s
[INFO] Running com.automation.qa.examples.QAAgentCodeExamplesTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.1 s
[INFO] -------------------------------------------------------
[INFO] Tests run: 37, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.4 s
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
```

## Troubleshooting

### Error: Maven command not found
```powershell
# Check PATH
$env:PATH -split ";"

# Or verify Maven installation
mvn -v

# If not in PATH, add it
$env:Path += ";C:\Program Files\Apache\maven\bin"
```

### Error: JAVA_HOME not set
```powershell
# Check Java location
where java

# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"

# Verify
echo $env:JAVA_HOME
```

### Error: Tests not found
```powershell
# Verify project structure
Get-ChildItem -Recurse -Filter "*.java" | Where-Object {$_.Name -like "*Test.java"}

# Check if tests compile
mvn clean compile test-compile
```

### Error: WireMock port already in use
- Close any process using the port
- Or let WireMock use dynamic port (default in our tests)

## Quick Start Commands

```powershell
# 1. Navigate to project
cd d:\Work\code\Automation

# 2. Clean and compile
mvn clean compile

# 3. Run all tests
mvn test

# 4. Run with detailed output
mvn test -X

# 5. Run single test class
mvn test -Dtest=QAAgentTest

# 6. Generate reports
mvn site

# 7. View coverage (if jacoco configured)
mvn clean test jacoco:report
```

## Integration with IDE

### IntelliJ IDEA
1. Open project
2. Right-click on test class
3. Select "Run" or "Run with Coverage"

### Eclipse
1. Right-click on test class
2. Run As → JUnit Test

### VS Code
1. Install Java Test Runner extension
2. Click "Run Test" above test method

## CI/CD Integration

### GitHub Actions
```yaml
name: Run Tests
on: [push]
jobs:
  test:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - uses: actions/setup-maven@v2
      - run: mvn clean test
```

### Jenkins
```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }
    }
}
```

## Next Steps

1. ✅ Install Maven
2. ✅ Set environment variables
3. ✅ Run `mvn clean test`
4. ✅ Review test results
5. ✅ Check TEST_RESULTS_REPORT.md
6. ✅ Integrate into CI/CD

## Additional Resources

- [Maven Official Documentation](https://maven.apache.org/guides/index.html)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [WireMock Documentation](http://wiremock.org/docs/)

---

**Once Maven is installed**, run:
```powershell
cd d:\Work\code\Automation
mvn clean test
```

This will execute all 37 tests! 🚀
