package com.poc.springbatch.batch.reader;

import com.poc.springbatch.model.PhoneRawData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads phone data from an external API endpoint.
 * Mocked for demonstration – replace the REST call body with the real endpoint.
 *
 * Data returned: userId, phoneNumber, previousLoansCount, currentBalance, appInstalled.
 */
@Slf4j
@Component
public class ApiPhoneDataReader implements ItemReader<PhoneRawData> {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    private Iterator<PhoneRawData> dataIterator;

    public ApiPhoneDataReader(
            RestTemplate restTemplate,
            @Value("${app.sources.api.base-url:http://localhost:8081}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    /**
     * Initialise or refresh the internal iterator by fetching data from the API.
     * Called once per job execution.
     */
    public void open() {
        log.info("ApiPhoneDataReader: fetching phone data from {}", apiBaseUrl);
        List<PhoneRawData> records = fetchFromApi();
        dataIterator = records.iterator();
        log.info("ApiPhoneDataReader: loaded {} records", records.size());
    }

    @Override
    public PhoneRawData read() {
        if (dataIterator == null) {
            open();
        }
        return dataIterator.hasNext() ? dataIterator.next() : null;
    }

    // ---------------------------------------------------------------------------
    // Mock implementation – replace with actual HTTP call once endpoint is ready
    // ---------------------------------------------------------------------------
    private List<PhoneRawData> fetchFromApi() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl)
                    .path("/api/v1/phone-data")
                    .toUriString();
            log.debug("Calling API: {}", url);
            // In production: restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PhoneRawData>>() {}).getBody()
        } catch (Exception e) {
            log.warn("API call failed, using mock data: {}", e.getMessage());
        }
        return buildMockApiData();
    }

    private List<PhoneRawData> buildMockApiData() {
        List<PhoneRawData> data = new ArrayList<>();
        Object[][] mock = {
                {"5551001001", "U001", 3, new BigDecimal("1500.00"), true},
                {"5551001002", "U002", 1, new BigDecimal("320.50"),  false},
                {"5551001003", "U003", 5, new BigDecimal("4200.75"), true},
                {"5551001004", "U004", 2, new BigDecimal("890.00"),  true},
                {"5551001005", "U005", 0, new BigDecimal("75.00"),   false},
                {"5551001006", "U006", 4, new BigDecimal("2100.00"), true},
                {"5551001007", "U007", 1, new BigDecimal("560.25"),  false},
                {"5551001008", "U008", 3, new BigDecimal("1100.00"), true},
                {"5551001009", "U009", 6, new BigDecimal("3800.00"), false},
                {"5551001010", "U010", 2, new BigDecimal("250.00"),  true},
        };
        LocalDateTime now = LocalDateTime.now();
        for (Object[] row : mock) {
            data.add(PhoneRawData.builder()
                    .phoneNumber((String) row[0])
                    .userId((String) row[1])
                    .previousLoansCount((Integer) row[2])
                    .currentBalance((BigDecimal) row[3])
                    .appInstalled((Boolean) row[4])
                    .fetchedAt(now)
                    .build());
        }
        return data;
    }
}
