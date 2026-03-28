package com.example.phonebatch.batch.reader;

import com.example.phonebatch.domain.PhoneNumberRawData;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class S3PhoneDataReader implements ItemReader<PhoneNumberRawData> {

    private final List<PhoneNumberRawData> data;
    private int index = 0;

    public S3PhoneDataReader() {
        data = Arrays.asList(
            create("555-0001", 0.72, 14, 1200.50),
            create("555-0002", 0.45, 30, 3400.00),
            create("555-0003", 0.88, 5,  800.75),
            create("555-0004", 0.60, 21, 5500.00),
            create("555-0005", 0.33, 60, 9800.00),
            create("555-0006", 0.91, 2,  250.00),
            create("555-0007", 0.55, 45, 4200.00),
            create("555-0008", 0.78, 10, 1750.25),
            create("555-0009", 0.40, 35, 6700.00),
            create("555-0010", 0.25, 90, 12000.00)
        );
    }

    private PhoneNumberRawData create(String phone, double historicalAnswerRate,
                                       int daysSinceLastContact, double balanceOwed) {
        PhoneNumberRawData d = new PhoneNumberRawData(phone);
        d.setHistoricalAnswerRate(historicalAnswerRate);
        d.setDaysSinceLastContact(daysSinceLastContact);
        d.setBalanceOwed(balanceOwed);
        return d;
    }

    @Override
    public PhoneNumberRawData read() {
        if (index < data.size()) {
            return data.get(index++);
        }
        return null;
    }

    public void reset() {
        index = 0;
    }

    public List<PhoneNumberRawData> readAll() {
        return data;
    }
}
