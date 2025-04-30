package com.pars.financial;

import com.pars.financial.utils.ApiKeyEncryption;
import com.pars.financial.utils.RandomStringGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
//@SpringBootApplication(exclude = {FlywayAutoConfiguration.class, SecurityAutoConfiguration.class})
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

        SpringApplication.run(CreditApplication.class, args);
        String[] strs = new String[]{
                "TELKZWCWUCP64JQE",
                "5DFC9XTRWCWG5HP5",
                "2FTTI6ANF8TNCN5U",
                "IIQ4G8A1IPB8KA8S"
        };
        ApiKeyEncryption crypt = new ApiKeyEncryption();
        crypt.setAlgorithm("AES/GCM/NoPadding");
        crypt.setSecretKey("0123456789ABCDEF0123456789ABCDEF");
        for (String str : strs) {
            System.out.println(crypt.encrypt(str));
        }
    }

}
