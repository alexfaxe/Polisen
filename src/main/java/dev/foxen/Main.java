package dev.foxen;

import dev.foxen.data.CaseDTO;
import dev.foxen.data.DataManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DataManager manager = new DataManager();
        CaseDTO[] data = manager.getData(100, true);

        if (data == null) {
            System.out.println("Data is null");
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter("output.csv");
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (CaseDTO x : data) {
                printToCsv(x, printWriter);
            }
            printWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        // Print out all the cases
        for (CaseDTO x : data) {
            System.out.println("Case: " + x.getId() + " | " + x.getDate() + " | " + x.getSummary() + " | " + x.getType());
        }
    }

    private static String removeCommas(String data) {
        if (data.contains(",")) {
            data = data.replace(",", "");
        }
        return data;
    }

    private static String formatURL(String url) {
        StringBuilder builder = new StringBuilder(url);
        builder.insert(0, "https://polisen.se");
        return builder.toString();
    }

    private static void printToCsv(CaseDTO currentCase, PrintWriter printWriter) {
        printWriter.print(currentCase.getId() + ",");
        printWriter.print(currentCase.getDate() + ",");
        printWriter.print(removeCommas(currentCase.getType()) + ",");
        printWriter.print(removeCommas(currentCase.getSummary()) + ",");
        printWriter.print(removeCommas(currentCase.getLocationName()) + ",");
        printWriter.println(formatURL(currentCase.getUrl()) + ",");
    }
}
