//
//
////with Excel read
//package org.example;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//
//import javax.net.ssl.HttpsURLConnection;
//import java.io.*;
//        import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Scanner;
//
//import static java.lang.System.exit;
//
//
//class APIUtility {
//    static final String baseurl = "https://tm-qa.seemymachines.com";
//    static String bearerToken;
//    static String companyId;
//    static int testInterval;
//    static String email;
//
//    public static String getCompanyId() {
//        return companyId;
//    }
//
//    public static void setCompanyId(String companyId) {
//        APIUtility.companyId = companyId;
//    }
//
//    static String password;
//
//    public static synchronized void setToken(String token) {
//        bearerToken = token;
//    }
//
//    public static synchronized String getToken() {
//        return bearerToken;
//    }
//
//}
//
//
///*Track =========*/
//
//class Track extends Thread{
//
//    public void run() {
//        String filePath = "C:\\Users\\DELL\\Downloads\\demo2X.xlsx";
//        try (FileInputStream file = new FileInputStream(new File(filePath));
//             Workbook workbook = new XSSFWorkbook(file)){
//
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                String email = row.getCell(0).getStringCellValue();    // Read email from column A
//                String password = row.getCell(1).getStringCellValue(); // Read password from column B
//                System.out.println("Email: " + email + " password :" + password);
//                System.out.println("Testing -**********-TRACK-***********");
//                tracLogin(email,password);
//                tracLogout();
//                try {
//                    Thread.sleep(APIUtility.testInterval);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//        }catch (Exception e)
//        {}
//    }
//
//    static String bToken;
//    static String companyId;
//    static String unitId;
//    static String baseurl = "https://tm-qa.seemymachines.com";
//
//    void tracLogin(String email,String password) {
//
//        {
//            try {
//                String endpoint = "/api/v1/user/mfa-login";
//
//                URL url = new URL(baseurl + endpoint);
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                String filePath = "C:\\Users\\DELL\\Downloads\\demo2X.xlsx";
//                long startTime = System.nanoTime();  // Start time
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("Accept", "application/json");
//                connection.setDoOutput(true);
//                System.out.println("Credentials Loaded: Email = " + email + ", Password = " + password);
//                String jsonInputString = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"mfa_based_login\":false}";
//
//                try (OutputStream os = connection.getOutputStream()) {
//                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
//                    os.write(input, 0, input.length);
//                }
//
//                long endTime = System.nanoTime();  // End time after receiving the response
//                long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
//                System.out.println("\nAPI Response Time: " + duration + " ms");
//
//                int responseCode = connection.getResponseCode();
//                System.out.println("Response Code (login):  " + responseCode);
//
//                StringBuilder responseMessage = new StringBuilder();
//                responseMessage = new StringBuilder(connection.getResponseMessage());
//                System.out.println("Response Message (login):" + responseMessage);
//
//                if (responseCode == HttpsURLConnection.HTTP_OK) {
//                    StringBuilder body = new StringBuilder();
//                    Scanner scanner = new Scanner(connection.getInputStream());
//                    while (scanner.hasNext()) {
//                        body.append(scanner.nextLine());
//                    }
////                           System.out.println("Response Body:" + body);
//                    try {
//                        String responseBody = String.valueOf(body);
//                        ObjectMapper objectMapper = new ObjectMapper();
//// /* full body printing*/
////                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
////                        JsonNode jsonNode = objectMapper.readTree(responseBody);
////                        String prettyJson = objectMapper.writeValueAsString(jsonNode);
////                        System.out.println("Response Body :"+prettyJson);
//
//                        JsonNode rootNode = objectMapper.readTree(body.toString());
//                        String name = rootNode.path("name").asText();
//                        String id = rootNode.path("id").asText();
//
//                        bToken = rootNode.path("token").path("token").asText();
//                        APIUtility.setToken(bToken);
//
//                        companyId = rootNode.path("company").path("id").asText();
//                        APIUtility.setCompanyId(companyId);
//                        System.out.println("_______________________________________________________________________________________TRACK LOGIN");
//                        System.out.println("\nid: " + id + "\nname: " + name + "\ncompanyId: " + companyId + "\nbarerToken: " + bToken);
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    System.out.println("Error");
//                }
//                connection.disconnect();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//
//    void tracLogout(){
//        try {
//            String endpoint = "/api/v1/user/logout";
//            URL url = new URL(APIUtility.baseurl + endpoint);
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.setRequestProperty("Authorization","Bearer "+bToken);
//            long startTime = System.nanoTime();  // Start time
//            connection.setRequestMethod("PUT");
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "application/json");
//
//            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
//                dos.writeBytes("");
//            }
//            System.out.println("Response code (logout)" + connection.getResponseMessage());
//
//            long endTime = System.nanoTime();  // End time after receiving the response
//            long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
//            System.out.println("\n API Response Time: (logout)" + duration + " ms");
//
//            try (BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                String line;
//
//                while ((line = bf.readLine()) != null) {
//                    String responseBody= line;
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//                    JsonNode jsonNode = objectMapper.readTree(responseBody);
//                    String prettyJson = objectMapper.writeValueAsString(jsonNode);
//                    System.out.println("_______________________________________________________________________________________TRACK LOGOUT");
//                    System.out.println("Response Body :"+prettyJson);
//
//                }
//            }
//            connection.disconnect();}
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//}
//public class Main {
//    public static void main(String[] args) throws IOException {
//
//        Scanner input = new Scanner(System.in);
//        System.out.println("Api tester");
//        do {
//            System.out.println("1.Combined\n0.Exit");
//            System.out.println("Enter your option");
//            String option = input.nextLine();
//
//            switch (option) {
//                case "1": {
//
//                    System.out.println("Enter Test Interval");
//                    APIUtility.testInterval=input.nextInt();
//
//                    Track track = new Track();
//                    track.start();
//                    break;
//                }
//                case "0": {
//                    System.out.println("Closing Test...........");
//                    exit(0);
//                }
//                default:
//                    System.out.println("\nInvalid option");
//            }
//        } while (true);
//    }
//}
