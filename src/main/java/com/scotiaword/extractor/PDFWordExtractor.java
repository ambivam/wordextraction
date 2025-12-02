package com.scotiaword.extractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * PDF Word Extractor using Apache PDFBox
 * Extracts all words from PDF documents and outputs them in TXT and JSON formats
 */
public class PDFWordExtractor {
    
    private static final String OUTPUT_DIR = "output";
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[\\p{L}\\p{M}]+\\b", Pattern.UNICODE_CHARACTER_CLASS);
    
    public static void main(String[] args) {
        PDFWordExtractor extractor = new PDFWordExtractor();
        
        // Look for PDF files in the current directory
        File currentDir = new File(".");
        File[] pdfFiles = currentDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf"));
        
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.err.println("No PDF files found in the current directory.");
            return;
        }
        
        // Process each PDF file found
        for (File pdfFile : pdfFiles) {
            System.out.println("Processing: " + pdfFile.getName());
            try {
                extractor.extractWordsFromPDF(pdfFile.getAbsolutePath());
                System.out.println("Successfully processed: " + pdfFile.getName());
            } catch (Exception e) {
                System.err.println("Error processing " + pdfFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Extract words from a PDF file and generate output in TXT and JSON formats
     */
    public void extractWordsFromPDF(String pdfFilePath) throws IOException {
        // Create output directory if it doesn't exist
        createOutputDirectory();
        
        // Extract text from PDF
        String extractedText = extractTextFromPDF(pdfFilePath);
        
        // Extract words from the text
        List<String> words = extractWords(extractedText);
        
        // Generate statistics
        WordStatistics stats = generateStatistics(words);
        
        // Get base filename without extension
        String baseFileName = getBaseFileName(pdfFilePath);
        
        // Generate outputs
        generateTxtOutput(words, stats, baseFileName);
        generateJsonOutput(words, stats, baseFileName);
        
        System.out.println("Extracted " + words.size() + " words from " + pdfFilePath);
        System.out.println("Unique words: " + stats.getUniqueWordCount());
    }
    
    /**
     * Extract text from PDF using PDFBox with UTF-8 encoding
     */
    private String extractTextFromPDF(String pdfFilePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String extractedText = stripper.getText(document);
            // Ensure proper UTF-8 handling
            return new String(extractedText.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Extract individual words from text using regex with Unicode support
     */
    private List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        
        // Use the pattern to find all matches in the entire text
        words.addAll(WORD_PATTERN.matcher(text)
            .results()
            .map(matchResult -> matchResult.group().toLowerCase())
            .filter(word -> !word.isEmpty() && word.length() > 1) // Filter out single characters
            .toList());
        
        return words;
    }
    
    /**
     * Generate word statistics
     */
    private WordStatistics generateStatistics(List<String> words) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (String word : words) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }
        
        return new WordStatistics(
            words.size(),
            wordFrequency.size(),
            wordFrequency
        );
    }
    
    /**
     * Generate TXT output format with UTF-8 encoding
     */
    private void generateTxtOutput(List<String> words, WordStatistics stats, String baseFileName) 
            throws IOException {
        String txtFileName = OUTPUT_DIR + File.separator + baseFileName + "_words.txt";
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(txtFileName), StandardCharsets.UTF_8))) {
            writer.println("PDF Word Extraction Results");
            writer.println("===========================");
            writer.println("Total words: " + stats.getTotalWordCount());
            writer.println("Unique words: " + stats.getUniqueWordCount());
            writer.println("Generated on: " + new Date());
            writer.println();
            
            writer.println("All Words (in order of appearance):");
            writer.println("-----------------------------------");
            for (int i = 0; i < words.size(); i++) {
                writer.printf("%d. %s%n", i + 1, words.get(i));
            }
            
            writer.println();
            writer.println("Word Frequency (sorted by frequency):");
            writer.println("------------------------------------");
            
            stats.getWordFrequency().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.printf("%s: %d%n", entry.getKey(), entry.getValue()));
        }
        
        System.out.println("TXT output generated: " + txtFileName);
    }
    
    /**
     * Generate JSON output format with UTF-8 encoding
     */
    private void generateJsonOutput(List<String> words, WordStatistics stats, String baseFileName) 
            throws IOException {
        String jsonFileName = OUTPUT_DIR + File.separator + baseFileName + "_words.json";
        
        // Create JSON structure
        Map<String, Object> jsonOutput = new HashMap<>();
        jsonOutput.put("metadata", Map.of(
            "totalWords", stats.getTotalWordCount(),
            "uniqueWords", stats.getUniqueWordCount(),
            "generatedOn", new Date().toString(),
            "sourceFile", baseFileName + ".pdf",
            "encoding", "UTF-8"
        ));
        
        jsonOutput.put("words", words);
        jsonOutput.put("wordFrequency", stats.getWordFrequency());
        
        // Write JSON file with UTF-8 encoding
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(jsonFileName), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, jsonOutput);
        }
        
        System.out.println("JSON output generated: " + jsonFileName);
    }
    
    /**
     * Create output directory if it doesn't exist
     */
    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            System.out.println("Created output directory: " + OUTPUT_DIR);
        }
    }
    
    /**
     * Get base filename without extension
     */
    private String getBaseFileName(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
    
    /**
     * Inner class to hold word statistics
     */
    public static class WordStatistics {
        private final int totalWordCount;
        private final int uniqueWordCount;
        private final Map<String, Integer> wordFrequency;
        
        public WordStatistics(int totalWordCount, int uniqueWordCount, Map<String, Integer> wordFrequency) {
            this.totalWordCount = totalWordCount;
            this.uniqueWordCount = uniqueWordCount;
            this.wordFrequency = wordFrequency;
        }
        
        public int getTotalWordCount() { return totalWordCount; }
        public int getUniqueWordCount() { return uniqueWordCount; }
        public Map<String, Integer> getWordFrequency() { return wordFrequency; }
    }
}
