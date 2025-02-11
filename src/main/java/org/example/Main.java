package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

class APIUtility {
    static final String BASE_URL = "https://tm-qa.seemymachines.com";
    static String bearerToken;
    static String companyId;
    static int testInterval;
    static String unitId;

    public static String getUnitId() {
        return unitId;
    }

    public static void setUnitId(String unitId) {
        APIUtility.unitId = unitId;
    }

    public static synchronized void setToken(String token) {
        bearerToken = token;
    }

    public static synchronized String getToken() {
        return bearerToken;
    }
}

class Track implements Runnable {
    private final String email;
    private final String password;

    public Track(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " processing User: " + email);
        tracLogin(email, password);
        tracLogout();
    }

    private void tracLogin(String email, String password) {
        try {
            String endpoint = "/api/v1/user/mfa-login";
            URL url = new URL(APIUtility.BASE_URL + endpoint);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"mfa_based_login\":false}";

            long startTime=System.nanoTime();
            try (OutputStream dos = connection.getOutputStream()) {
                dos.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = connection.getResponseCode();
            long endTime = System.nanoTime();
            long responseTime = (endTime - startTime) / 1_000_000;

            System.out.println(Thread.currentThread().getName() +
                    " Response Time (Login): " + responseTime + " ms | Response Code: " + responseCode);

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                StringBuilder body = new StringBuilder();
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    while (scanner.hasNext()) {
                        body.append(scanner.nextLine());
                    }
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseBody = objectMapper.readTree(body.toString());

                String bToken = responseBody.path("token").path("token").asText();
                APIUtility.setToken(bToken);

                String companyId = responseBody.path("company").path("id").asText();
                APIUtility.companyId = companyId;

                String unitId= responseBody.path("unit").path("id").asText();
                APIUtility.unitId=unitId;

                System.out.println(Thread.currentThread().getName() + "  Login Successful: Token = " + bToken);
                System.out.println(Thread.currentThread().getName()+"  CompanyId= "+companyId);
                System.out.println(Thread.currentThread().getName()+"  UnitId= "+unitId);
            } else {
                System.out.println(Thread.currentThread().getName() + " Login Failed");
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tracLogout() {
        try {
            String endpoint = "/api/v1/user/logout";
            URL url = new URL(APIUtility.BASE_URL + endpoint);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + APIUtility.getToken());
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            long startTime=System.nanoTime();

            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
                dos.writeBytes("");
            }
            int responseCode = connection.getResponseCode();
            long endTime = System.nanoTime();
            long responseTime = (endTime - startTime) / 1_000_000;

            System.out.println(Thread.currentThread().getName() +
                    " Response Time (Logout): " + responseTime + " ms | Response Code: " + responseCode);

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("API Tester");

        do {
            System.out.println("1. Start Multi-Threaded Test\n0. Exit");
            System.out.println("Enter your option:");
            String option = input.nextLine();

            switch (option) {
                case "1": {
                    System.out.println("Enter Test Interval (ms):");
                    APIUtility.testInterval = input.nextInt();
                    input.nextLine();  // Consume the newline character
                    executeMultiThreadedTest();
                    break;
                }
                case "0": {
                    System.out.println("Closing Test...");
                    exit(0);
                }
                default:
                    System.out.println("Invalid option. Try again.");
            }
        } while (true);
    }

    private static void executeMultiThreadedTest() {
        String filePath = "C:\\Users\\DELL\\Downloads\\demo2X.xlsx";
        List<Track> trackTasks = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String email = row.getCell(0).getStringCellValue();
                    String password = row.getCell(1).getStringCellValue();
                    trackTasks.add(new Track(email, password));
                }
            }

            // Create thread pool with dynamic size based on rows
            ExecutorService executorService = Executors.newFixedThreadPool(rowCount);

            for (Track task : trackTasks) {
                executorService.execute(task);
                try {
                    Thread.sleep(APIUtility.testInterval); // Adding slight delay for better tracking
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


