package com.example.phonebatch.dialer;

import com.example.phonebatch.domain.ScoredPhoneNumber;
import java.util.List;

public interface DialerStrategy {
    void sendPhoneList(List<ScoredPhoneNumber> phoneNumbers);
}
