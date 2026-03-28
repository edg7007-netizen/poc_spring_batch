package com.poc.springbatch.batch.reader;

import com.poc.springbatch.model.PhoneRawData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads phone analytics data (historical answer rates) from a CSV file in S3.
 * Mocked for demonstration – replace buildMockS3Data() with actual S3 read logic
 * once the bucket and file path are configured.
 *
 * Expected CSV columns: phoneNumber, userId, answerRateSameHour, answerRateSameDow
 */
@Slf4j
@Component
public class S3PhoneDataReader implements ItemReader<PhoneRawData> {

    private final S3Client s3Client;
    private final String bucketName;
    private final String objectKey;
    private final boolean useS3;

    private Iterator<PhoneRawData> dataIterator;

    public S3PhoneDataReader(
            S3Client s3Client,
            @Value("${app.sources.s3.bucket-name:phone-analytics-bucket}") String bucketName,
            @Value("${app.sources.s3.object-key:analytics/answer-rates.csv}") String objectKey,
            @Value("${app.sources.s3.enabled:false}") boolean useS3) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.useS3 = useS3;
    }

    public void open() {
        log.info("S3PhoneDataReader: reading phone analytics (useS3={}, bucket={}, key={})", useS3, bucketName, objectKey);
        List<PhoneRawData> records = fetchFromS3();
        dataIterator = records.iterator();
        log.info("S3PhoneDataReader: loaded {} records", records.size());
    }

    @Override
    public PhoneRawData read() {
        if (dataIterator == null) {
            open();
        }
        return dataIterator.hasNext() ? dataIterator.next() : null;
    }

    // ---------------------------------------------------------------------------
    // Mock implementation – replace with real S3 CSV parsing once bucket is ready
    // ---------------------------------------------------------------------------
    private List<PhoneRawData> fetchFromS3() {
        if (useS3) {
            try {
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();
                List<PhoneRawData> data = new ArrayList<>();
                LocalDateTime now = LocalDateTime.now();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(s3Client.getObject(request)))) {
                    String line;
                    boolean header = true;
                    while ((line = reader.readLine()) != null) {
                        if (header) { header = false; continue; }
                        String[] parts = line.split(",");
                        data.add(PhoneRawData.builder()
                                .phoneNumber(parts[0].trim())
                                .userId(parts[1].trim())
                                .historicalAnswerRateAtSameHour(Double.parseDouble(parts[2].trim()))
                                .historicalAnswerRateAtSameDayOfWeek(Double.parseDouble(parts[3].trim()))
                                .fetchedAt(now)
                                .build());
                    }
                }
                return data;
            } catch (Exception e) {
                log.warn("S3 read failed, using mock data: {}", e.getMessage());
            }
        }
        return buildMockS3Data();
    }

    private List<PhoneRawData> buildMockS3Data() {
        LocalDateTime now = LocalDateTime.now();
        List<PhoneRawData> data = new ArrayList<>();
        Object[][] mock = {
                {"5551001001", "U001", 0.72, 0.68},
                {"5551001002", "U002", 0.35, 0.40},
                {"5551001003", "U003", 0.85, 0.80},
                {"5551001004", "U004", 0.55, 0.60},
                {"5551001005", "U005", 0.20, 0.25},
                {"5551001006", "U006", 0.65, 0.70},
                {"5551001007", "U007", 0.45, 0.38},
                {"5551001008", "U008", 0.78, 0.75},
                {"5551001009", "U009", 0.30, 0.28},
                {"5551001010", "U010", 0.60, 0.62},
        };
        for (Object[] row : mock) {
            data.add(PhoneRawData.builder()
                    .phoneNumber((String) row[0])
                    .userId((String) row[1])
                    .historicalAnswerRateAtSameHour((Double) row[2])
                    .historicalAnswerRateAtSameDayOfWeek((Double) row[3])
                    .fetchedAt(now)
                    .build());
        }
        return data;
    }
}
