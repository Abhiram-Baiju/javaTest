package org.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.exit;


class APIUtility {
    static final String baseurl = "https://tm-qa.seemymachines.com";
    static String bearerToken;
    static String companyId;
    static int repetition;
    static int testInterval;
    static String email;
    static String password;

    public static synchronized void setToken(String token) {
        bearerToken = token;
    }

    public static synchronized String getToken() {
        return bearerToken;
    }

    // Method to read credentials from Excel
    public static void readCredentialsFromExcel(String filePath) {
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1); // Read second row (index 1)

            email = row.getCell(0).getStringCellValue();    // Read email from column A
            password = row.getCell(1).getStringCellValue(); // Read password from column B

            System.out.println("Credentials Loaded: Email = " + email + ", Password = " + password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



class Ot extends Thread{
   static String bToken;

   public void run(){
       for(int i=0;i<APIUtility.repetition;i++){
       otLogin();
           try {
               Thread.sleep(APIUtility.testInterval);
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
       }
   }


    static void otLogin(){
        try {
            String endpoint="/api/v1/user/ot-login";
            URL url=new URL(APIUtility.baseurl+endpoint);
            HttpsURLConnection connection=(HttpsURLConnection) url.openConnection();

            long startTime=System.nanoTime();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept","application/json");
            connection.setDoOutput(true);
//            String jsonInputString="{\"email\":\"abhiramot@seemymachines.com\",\"password\":\"Abhiram@123\"}";
            String jsonInputString = "{\"email\":\"" + APIUtility.email + "\",\"password\":\"" + APIUtility.password + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code (ot login):  " + responseCode);

            StringBuilder responseMessage = new StringBuilder();
            responseMessage = new StringBuilder(connection.getResponseMessage());
            System.out.println("Response Message (ot login):" + responseMessage);

            long endTime = System.nanoTime();  // End time after receiving the response
            long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
            System.out.println("\nAPI Response Time(ot login): " + duration + " ms");

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                StringBuilder body = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    body.append(scanner.nextLine());
                }

                try {
                    String responseBody= String.valueOf(body);
                    ObjectMapper objectMapper = new ObjectMapper();

                    /* full body printing*/
//                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//                    JsonNode jsonNode = objectMapper.readTree(responseBody);
//                    String prettyJson = objectMapper.writeValueAsString(jsonNode);
//                    System.out.println("Response Body :"+prettyJson);

                    JsonNode rootNode = objectMapper.readTree(body.toString());
                    String name = rootNode.path("name").asText();
                    String id = rootNode.path("id").asText();

                    bToken = rootNode.path("token").path("token").asText();
                    APIUtility.setToken(bToken);

                    String companyId=rootNode.path("company").path("id").asText();
                    System.out.println("_______________________________________________________________________________________OT LOGIN");
                    System.out.println("\nid: " + id + "\nname: " + name + "\ncompanyId: " + companyId + "\nbarerToken: " + bToken);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Error");
            }
            connection.disconnect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


/*Track =========*/

class Track extends Thread{

    public void run() {
        for (int i=0;i<APIUtility.repetition;i++){
            System.out.println("Testing -**********-TRACK-***********");
            tracLogin();
            getter();
            getterWithPayload();
            tracLogout();
            try {
                Thread.sleep(APIUtility.testInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static String bToken;
    static String companyId;
    static String unitId;
    static String baseurl = "https://tm-qa.seemymachines.com";

     void tracLogin() {

        {
            try {
                String endpoint = "/api/v1/user/mfa-login";

                URL url = new URL(baseurl + endpoint);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();


                long startTime = System.nanoTime();  // Start time
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
//                String jsonInputString = "{\"email\":\"abhiram@seemymachines.com\",\"password\":\"Abhiram@123\",\"mfa_based_login\":false}";
                String jsonInputString = "{\"email\":\"" + APIUtility.email + "\",\"password\":\"" + APIUtility.password + "\",\"mfa_based_login\":false}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                long endTime = System.nanoTime();  // End time after receiving the response
                long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
                System.out.println("\nAPI Response Time: " + duration + " ms");

                int responseCode = connection.getResponseCode();
                System.out.println("Response Code (login):  " + responseCode);

                StringBuilder responseMessage = new StringBuilder();
                responseMessage = new StringBuilder(connection.getResponseMessage());
                System.out.println("Response Message (login):" + responseMessage);

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    StringBuilder body = new StringBuilder();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    while (scanner.hasNext()) {
                        body.append(scanner.nextLine());
                    }
//                    System.out.println("Response Body:" + body);
                    try {
                        String responseBody= String.valueOf(body);
                        ObjectMapper objectMapper = new ObjectMapper();
// /* full body printing*/
//                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//                        JsonNode jsonNode = objectMapper.readTree(responseBody);
//                        String prettyJson = objectMapper.writeValueAsString(jsonNode);
//                        System.out.println("Response Body :"+prettyJson);

                        JsonNode rootNode = objectMapper.readTree(body.toString());
                        String name = rootNode.path("name").asText();
                        String id = rootNode.path("id").asText();

                        bToken = rootNode.path("token").path("token").asText();
                        APIUtility.setToken(bToken);

                        APIUtility.companyId=rootNode.path("company").path("id").asText();
                        System.out.println("_______________________________________________________________________________________TRACK LOGIN");
                        System.out.println("\nid: " + id + "\nname: " + name + "\ncompanyId: " + companyId + "\nbarerToken: " + bToken);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Error");
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

     void getter() {
                try {
                    String endpoint = "/api/v1/dashboard/web/5c357b3826e2660ccde349b0/5c35c58626e2660ccde35a53/all/get-shifter-trips-status-dashboard-data";
                    URL url = new URL(APIUtility.baseurl + endpoint);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", "Bearer " + APIUtility.getToken());

                    long startTime = System.nanoTime();  // Start time

                    connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");

                    int responseCode = connection.getResponseCode();
                    System.out.println("Response Code (Getter): " + responseCode);
                    System.out.println("Response Message (Getter): " + connection.getResponseMessage());

                    long endTime = System.nanoTime();  // End time after receiving the response
                    long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
                    System.out.println("\nAPI Response Time (getter): " + duration + " ms");



                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder responseBody = new StringBuilder();
                        String line;
                        while ((line = bf.readLine()) != null) {
                            responseBody.append(line);
                        }

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                        JsonNode jsonNode = objectMapper.readTree(responseBody.toString());
                        String getterBody = objectMapper.writeValueAsString(jsonNode);
                        System.out.println("_______________________________________________________________________________________TRACK GETTER");
                        System.out.println("Response Body (Getter): " + getterBody);
                    } else {
                        System.out.println("Getter API Error");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

     void getterWithPayload(){

            try {
                String endpoint = "/api/v1/shifter/clubbed/trip/web/5c357b3826e2660ccde349b0/5c35c58626e2660ccde35a53/other/get-clubbed-trips" +
                        "?origin=&status=all&shifterId=&page=1&size=25&from=&to=&transportType=all&priority=all" +
                        "&tripType=outgoing&equipment=all&movementType=all&autoGenerated=all&assignmentType=all" +
                        "&tripNumber=&transportTypeId=&hasMultipleDestinations=false&hasMultipleTasks=false";

                URL url = new URL(APIUtility.baseurl + endpoint);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                long startTime = System.nanoTime();  // Start time
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + APIUtility.getToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(false);  // No request body for GET requests

                int responseCode = connection.getResponseCode();
                System.out.println("Response Code (getterWithPayload): " + responseCode);
                System.out.println("Response Message (getterWithPayload): " + connection.getResponseMessage());

                long endTime = System.nanoTime();  // End time
                long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
                System.out.println("\nAPI Response Time (getterWithPayload): " + duration + " ms");

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    try (BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder responseBody = new StringBuilder();
                        String line;
                        while ((line = bf.readLine()) != null) {
                            responseBody.append(line);
                        }

                        // Convert response to pretty JSON
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                        JsonNode jsonNode = objectMapper.readTree(responseBody.toString());
                        String getterWP = objectMapper.writeValueAsString(jsonNode);
                        System.out.println("_______________________________________________________________________________________TRACK GETTER WITH PAYLOAD");
                        System.out.println("Response Body (Getter): " + getterWP);
                    }
                } else {
                    System.out.println("Getter API Error");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

    }

     void tracLogout(){
        try {
            String endpoint = "/api/v1/user/logout";
            URL url = new URL(APIUtility.baseurl + endpoint);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization","Bearer "+bToken);

            long startTime = System.nanoTime();  // Start time
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
                dos.writeBytes("");
            }
            System.out.println("Response code (logout)" + connection.getResponseMessage());

            long endTime = System.nanoTime();  // End time after receiving the response
            long duration = (endTime - startTime) / 1000000;  // Convert to milliseconds
            System.out.println("\n API Response Time: (logout)" + duration + " ms");

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;

                while ((line = bf.readLine()) != null) {
                    String responseBody= String.valueOf(line);
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String prettyJson = objectMapper.writeValueAsString(jsonNode);
                    System.out.println("_______________________________________________________________________________________TRACK LOGOUT");
                    System.out.println("Response Body :"+prettyJson);

                }
            }
            connection.disconnect();}
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\DELL\\Downloads\\demo.xlsx";
        APIUtility.readCredentialsFromExcel(filePath);



        Scanner input = new Scanner(System.in);
        System.out.println("Api tester");
        do {
            System.out.println("\n1.Trac\n2.Ot\n3.Combined\n0.Exit");
            System.out.println("Enter your option");
            String option = input.nextLine();

            switch (option) {
                case "1": {
                    Track track = new Track();
                    track.tracLogin();
                    track.getter();
                    track.getterWithPayload();
                    track.tracLogout();
                    break;

                }
                case "2": {
                    Ot ot=new Ot();
                    ot.otLogin();
                    break;
                }
                case "3": {
                    System.out.println("Enter Thread count");
                    APIUtility.repetition= input.nextInt();

                    System.out.println("Enter Test Interval");
                    APIUtility.testInterval=input.nextInt();

                    Track track = new Track();
                    Ot ot = new Ot();

                    ot.start();
                    track.start();

                    break;
                }
                case "0": {
                    System.out.println("Closing Test...........");
                    exit(0);
                }
                default:
                    System.out.println("\nInvalid option");
            }
        }
        while (true);
    }
}