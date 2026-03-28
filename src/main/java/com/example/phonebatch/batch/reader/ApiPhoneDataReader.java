package com.example.phonebatch.batch.reader;

import com.example.phonebatch.domain.PhoneNumberRawData;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ApiPhoneDataReader implements ItemReader<PhoneNumberRawData> {

    private final List<PhoneNumberRawData> data;
    private int index = 0;

    public ApiPhoneDataReader() {
        data = Arrays.asList(
            create("555-0001", 3, true),
            create("555-0002", 1, false),
            create("555-0003", 5, true),
            create("555-0004", 2, true),
            create("555-0005", 7, false),
            create("555-0006", 0, true),
            create("555-0007", 4, false),
            create("555-0008", 6, true),
            create("555-0009", 2, true),
            create("555-0010", 8, false)
        );
    }

    private PhoneNumberRawData create(String phone, int previousLoans, boolean appInstalled) {
        PhoneNumberRawData d = new PhoneNumberRawData(phone);
        d.setPreviousLoans(previousLoans);
        d.setAppInstalled(appInstalled);
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
