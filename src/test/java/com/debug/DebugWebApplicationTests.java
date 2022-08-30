package com.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("development")
class DebugWebApplicationTests {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void contextLoads() throws JsonProcessingException {
//        String a = "{\n" +
//                "  \"userId\" : \"test\",\n" +
//                "  \"username\" : \"test_name\",\n" +
//                "  \"password1\" : \"test1\",\n" +
//                "  \"password2\" : \"test1\",\n" +
//                "  \"email\" : \"test@gmail.com\"\n" +
//                "}";
//        Map<String, String> content = objectMapper.readValue(a, new TypeReference<>() {
//        });

        LocalDateTime a = LocalDateTime.of(2022, 12, 12, 21, 21);
        LocalDateTime b = LocalDateTime.of(2022, 12, 12, 21, 23);
        LocalDateTime c = LocalDateTime.of(2022, 12, 12, 21, 24);
        System.out.println(a.equals(b));
        System.out.println(b.isAfter(a));
        System.out.println(compareBetweenTimes(c, a, b));

    }

    @Test
    public void test2() throws JsonProcessingException {
        Map<String, String> content = new HashMap<>();
        content.put("1", "1");
        content.put("2", "2");
        content.put("3", "3");

        System.out.println();
    }

    private boolean compareBetweenTimes(LocalDateTime timeForCompare, LocalDateTime startDate, LocalDateTime endDate) {
        boolean compareStartDate = timeForCompare.equals(startDate) || timeForCompare.isAfter(startDate);
        boolean compareEndDate = timeForCompare.equals(endDate) || timeForCompare.isBefore(endDate);

        return compareStartDate && compareEndDate;
    }

}
