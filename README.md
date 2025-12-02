# PDF Word Extractor

A Java application that uses Apache PDFBox to extract every word from PDF documents and generates output in both TXT and JSON formats.

## Features

- Extracts all words from PDF documents with **UTF-8 encoding support**
- Generates comprehensive word statistics
- Outputs results in both TXT and JSON formats with UTF-8 encoding
- Handles multiple PDF files in the directory
- Creates organized output in the `output` folder
- Properly handles special characters and accented text

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Project Structure

```
scotiaword_extractor/
├── src/main/java/com/scotiaword/extractor/
│   └── PDFWordExtractor.java
├── output/                     (created automatically)
├── pom.xml
├── README.md
└── PDF for Automation Testing.pdf
```

## How to Run

1. **Compile the project:**
   ```bash
   mvn clean compile
   ```

2. **Run the application:**
   ```bash
   mvn exec:java
   ```

   Or alternatively:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.scotiaword.extractor.PDFWordExtractor"
   ```

3. **Build JAR file (optional):**
   ```bash
   mvn clean package
   java -jar target/pdf-word-extractor-1.0.0.jar
   ```

## Output

The application will automatically:

1. **Create an `output` folder** if it doesn't exist
2. **Process all PDF files** in the current directory
3. **Generate two files** for each PDF:
   - `{filename}_words.txt` - Human-readable text format
   - `{filename}_words.json` - Machine-readable JSON format

### TXT Output Format

- Summary statistics (total words, unique words, generation date)
- Complete list of words in order of appearance
- Word frequency analysis sorted by frequency

### JSON Output Format

```json
{
  "metadata": {
    "totalWords": 1234,
    "uniqueWords": 567,
    "generatedOn": "Mon Dec 02 11:07:00 IST 2025",
    "sourceFile": "PDF for Automation Testing.pdf"
  },
  "words": ["word1", "word2", "word3", ...],
  "wordFrequency": {
    "word1": 10,
    "word2": 8,
    "word3": 5
  }
}
```

## Dependencies

- **Apache PDFBox 2.0.29** - PDF text extraction
- **Jackson 2.15.2** - JSON processing
- **JUnit 4.13.2** - Testing framework

## Notes

- Words are converted to lowercase for consistency
- Extracts alphabetic words including accented characters (números, aceptación, etc.)
- The application processes all PDF files found in the current directory
- Output files are named based on the original PDF filename

# First compile the project
mvn clean compile

# Then run the application
mvn exec:java

# Build the JAR file
mvn clean package

# Run the JAR file
java -jar target/pdf-word-extractor-1.0.0.jar

# Compile first
mvn clean compile

# Run with classpath
java -cp "target/classes;target/dependency/*" com.scotiaword.extractor.PDFWordExtractor