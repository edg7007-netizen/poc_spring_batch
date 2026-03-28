package com.poc.springbatch.batch.reader;

import com.poc.springbatch.model.PhoneRawData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads phone data from the collections service database read-replica.
 * Mocked for demonstration – replace buildMockDbData() with the real JdbcTemplate query.
 *
 * Data returned: ptpMadeCount, hasBrokenPtp, lastSuccessfulContactDate.
 */
@Slf4j
@Component
public class DatabasePhoneDataReader implements ItemReader<PhoneRawData> {

    private final JdbcTemplate replicaJdbcTemplate;
    private final boolean useReplica;

    private Iterator<PhoneRawData> dataIterator;

    public DatabasePhoneDataReader(
            JdbcTemplate replicaJdbcTemplate,
            @Value("${app.sources.db.use-replica:false}") boolean useReplica) {
        this.replicaJdbcTemplate = replicaJdbcTemplate;
        this.useReplica = useReplica;
    }

    public void open() {
        log.info("DatabasePhoneDataReader: fetching phone data (useReplica={})", useReplica);
        List<PhoneRawData> records = fetchFromDatabase();
        dataIterator = records.iterator();
        log.info("DatabasePhoneDataReader: loaded {} records", records.size());
    }

    @Override
    public PhoneRawData read() {
        if (dataIterator == null) {
            open();
        }
        return dataIterator.hasNext() ? dataIterator.next() : null;
    }

    // ---------------------------------------------------------------------------
    // Mock implementation – replace with real SQL once replica is accessible
    // ---------------------------------------------------------------------------
    private List<PhoneRawData> fetchFromDatabase() {
        if (useReplica) {
            try {
                return replicaJdbcTemplate.query(
                        "SELECT phone_number, user_id, ptp_made_count, has_broken_ptp, last_contact_date FROM collections.phone_ptp_history",
                        (rs, rowNum) -> PhoneRawData.builder()
                                .phoneNumber(rs.getString("phone_number"))
                                .userId(rs.getString("user_id"))
                                .ptpMadeCount(rs.getInt("ptp_made_count"))
                                .hasBrokenPtp(rs.getBoolean("has_broken_ptp"))
                                .lastSuccessfulContactDate(rs.getObject("last_contact_date", LocalDate.class))
                                .fetchedAt(LocalDateTime.now())
                                .build());
            } catch (Exception e) {
                log.warn("DB replica query failed, using mock data: {}", e.getMessage());
            }
        }
        return buildMockDbData();
    }

    private List<PhoneRawData> buildMockDbData() {
        LocalDate today = LocalDate.now();
        List<PhoneRawData> data = new ArrayList<>();
        Object[][] mock = {
                {"5551001001", "U001", 2, false, today.minusDays(5)},
                {"5551001002", "U002", 1, true,  today.minusDays(30)},
                {"5551001003", "U003", 3, false, today.minusDays(2)},
                {"5551001004", "U004", 0, false, today.minusDays(15)},
                {"5551001005", "U005", 1, true,  today.minusDays(60)},
                {"5551001006", "U006", 2, false, today.minusDays(7)},
                {"5551001007", "U007", 0, false, today.minusDays(45)},
                {"5551001008", "U008", 1, false, today.minusDays(3)},
                {"5551001009", "U009", 4, true,  today.minusDays(90)},
                {"5551001010", "U010", 1, false, today.minusDays(10)},
        };
        LocalDateTime now = LocalDateTime.now();
        for (Object[] row : mock) {
            data.add(PhoneRawData.builder()
                    .phoneNumber((String) row[0])
                    .userId((String) row[1])
                    .ptpMadeCount((Integer) row[2])
                    .hasBrokenPtp((Boolean) row[3])
                    .lastSuccessfulContactDate((LocalDate) row[4])
                    .fetchedAt(now)
                    .build());
        }
        return data;
    }
}
