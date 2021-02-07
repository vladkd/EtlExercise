import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.csv.*;

class Main {
  public static void main(String[] args) {
    File inFile = null, outFile = null;
    for(int i = 0; i < args.length; ){
      switch (args[i]){
        case "--in":
        case "-i": {
          if (args.length <= i + 1){
            System.out.println("No value for '--in' argument provided");
            System.exit(128);
          } 
          inFile = new File(args[i + 1]);
          if (!inFile.exists()) {
            System.out.println("'" + inFile.getAbsolutePath() + "' does not exist");
            System.exit(128);
          }
          if (inFile.isDirectory()) {
            System.out.println("'" + inFile.getAbsolutePath() + "' is a directory");
            System.exit(128);
          }
          if (!inFile.canRead()) {
            System.out.println("Cannot read from '" + inFile.getAbsolutePath() + "'");
            System.exit(128);
          }
          i += 2;
          continue;
        }
        case "--out":
        case "-o": {
          if (args.length <= i + 1){
            System.out.println("No value for '--out' argument provided");
            System.exit(128);
          } 
          outFile = new File(args[i + 1]);
          i += 2;
          continue;
        }
        default: {
          System.out.println("Incorrect argument '" + args[i] + "'");
          System.exit(128);
        }
      }
    }
    if (inFile == null) {
      System.out.println("Input file name is not provided. Please provide input file name in '-i' argument.");
      System.exit(128);
    }
    if (outFile == null) {
      String filename = DateTimeFormatter.BASIC_ISO_DATE.format(LocalDateTime.now()) + ".csv";
      outFile = new File(filename);
    }
    if (outFile.exists()) {
      System.out.println("'" + outFile.getAbsolutePath() + "' already exists");
      System.exit(128);
    }
    if (outFile.isDirectory()) {
      System.out.println("'" + outFile.getAbsolutePath() + "' is a directory");
      System.exit(128);
    }
    try {outFile.createNewFile();}
    catch (IOException ioe) {
      System.out.println("Could not create out file '" + outFile.getAbsolutePath() + "'. " + ioe.getMessage());
      System.exit(128);
    }
    //========
    ObjectMapper objectMapper = new ObjectMapper();
    try ( 
      FileWriter out = new FileWriter(outFile);
      CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE))
    ){
      try {
        JsonNode root = objectMapper.readTree(inFile);
        if (!root.isArray()) {
          System.out.println("Root of the JSON is not an array");
          System.exit(128);
        }
        ArrayNode rootArray = (ArrayNode) root;
        Map<String, String> resultMap = new HashMap<String,String>(rootArray.size());
        Iterator<JsonNode> rowIterator = rootArray.iterator();
        JsonNode row, name, number;
        while (rowIterator.hasNext()) {
          row = rowIterator.next();
          name = row.get("name");
          if (name == null || name.isNull()) continue;
          number = row.get("creditcard");
          if (number == null || number.isNull()) continue;
          resultMap.put(name.toString(), number.toString());
        }
        // printer.printRecords(resultMap);
        for (Map.Entry<String, String> entry: resultMap.entrySet()) {
          printer.printRecord(entry.getKey(), entry.getValue());
        }
        System.out.println("Processed " + resultMap.size() + " rows.");
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
}