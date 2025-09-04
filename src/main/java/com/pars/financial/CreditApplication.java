package com.pars.financial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pars.financial.utils.ApiKeyEncryption;
import com.pars.financial.utils.RandomStringGenerator;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAsync
@EnableScheduling
public class CreditApplication {

    public static void main(String[] args) {



//        int validityDays = 183;
//        long amount = 120000000;
//        int count = 20;
//        StringBuilder s = new StringBuilder();
//        s.append("INSERT INTO public.gift_card \r\n" +
//                "(expiry_date, issue_date, balance, initial_amount, identifier, serial_no) \r\n" +
//                "values\r\n");
//        for (int i = 0; i < count; i++) {
//            s.append("('");
//            s.append(LocalDate.now().plusDays(validityDays));
//            s.append("', '");
//            s.append(LocalDate.now());
//            s.append("', ");
//            s.append(amount);
//            s.append(", ");
//            s.append(amount);
//            s.append(", ");
//            var s1 = RandomStringGenerator.generateRandomUppercaseStringWithNumbers(8);
//            var s2 = RandomStringGenerator.generateRandomNumericString(8);
//            s.append(s2);
//            s.append(", '");
//            s.append(s1);
//            s.append("'), \r\n");
//        }
//        s.append(";\r\n");
//        System.out.println(s);

        System.out.println(RandomStringGenerator.generateRandomHexString(32));

        SpringApplication.run(CreditApplication.class, args);
        String[] ops_apiKeys = new String[]{
                "TELKZWCWUCP64JQE",
                "5DFC9XTRWCWG5HP5",
                "2FTTI6ANF8TNCN5U",
                "IIQ4G8A1IPB8KA8S"
        };

        String[] test_apiKeys = new String[]//[4];
        {
                "1A1603AE1C9B62A3",
                "AC004E2BC35D92A4",
                "E9893A2122463696",
                "F8B47401F2E3BD91",

        };

//        for (int i = 0; i < ops_apiKeys.length; i++) {
//            ops_apiKeys[i] = RandomStringGenerator.generateRandomHexString(16);
//            System.out.println("\"" + ops_apiKeys[i] + "\",");
//        }
        var debug_SecretKey = "0123456789ABCDEF0123456789ABCDEF";
        var ops_SecretKey = "969382DCD1578F69B1C983AE0A18397E";
        var test_SecretKey = "888882DCD1578F69B1C983AE0A18397E";

        showEncryptedApiKey(ops_SecretKey, ops_apiKeys);
        showEncryptedApiKey(debug_SecretKey, ops_apiKeys);
        //showEncryptedApiKey(test_SecretKey, test_apiKeys);
    }
    private static void showEncryptedApiKey(String secretKey, String[] apiKeys) {
        ApiKeyEncryption crypt = new ApiKeyEncryption();
        crypt.setAlgorithm("AES/GCM/NoPadding");
        crypt.setSecretKey(secretKey);
        for (String str : apiKeys) {
            System.out.println(crypt.encrypt(str));
        }
    }
}
