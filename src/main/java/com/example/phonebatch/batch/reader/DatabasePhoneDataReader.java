package com.example.phonebatch.batch.reader;

import com.example.phonebatch.domain.PhoneNumberRawData;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabasePhoneDataReader implements ItemReader<PhoneNumberRawData> {

    private final List<PhoneNumberRawData> data;
    private int index = 0;

    public DatabasePhoneDataReader() {
        data = Arrays.asList(
            create("555-0001", 2, false),
            create("555-0002", 0, false),
            create("555-0003", 3, true),
            create("555-0004", 1, false),
            create("555-0005", 4, true),
            create("555-0006", 0, false),
            create("555-0007", 2, true),
            create("555-0008", 5, false),
            create("555-0009", 1, false),
            create("555-0010", 3, true)
        );
    }

    private PhoneNumberRawData create(String phone, int promiseToPay, boolean brokenPtp) {
        PhoneNumberRawData d = new PhoneNumberRawData(phone);
        d.setPromiseToPay(promiseToPay);
        d.setBrokenPtp(brokenPtp);
        return d;
    }

    @Override
    public PhoneNumberRawData read() {
        if (index < data.size()) {
            return data.get(index++);
        }
        return null;
    }

    public List<PhoneNumberRawData> readAll() {
        return data;
    }
}
