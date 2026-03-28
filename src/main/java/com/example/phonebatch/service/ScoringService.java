package com.example.phonebatch.service;

import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    public List<ScoredPhoneNumber> scoreAll(List<PhoneNumberRawData> rawList) {
        double minLoans   = rawList.stream().mapToInt(PhoneNumberRawData::getPreviousLoans).min().orElse(0);
        double maxLoans   = rawList.stream().mapToInt(PhoneNumberRawData::getPreviousLoans).max().orElse(0);
        double minPtp     = rawList.stream().mapToInt(PhoneNumberRawData::getPromiseToPay).min().orElse(0);
        double maxPtp     = rawList.stream().mapToInt(PhoneNumberRawData::getPromiseToPay).max().orElse(0);
        double minBalance = rawList.stream().mapToDouble(PhoneNumberRawData::getBalanceOwed).min().orElse(0);
        double maxBalance = rawList.stream().mapToDouble(PhoneNumberRawData::getBalanceOwed).max().orElse(0);

        return rawList.stream()
            .map(raw -> score(raw, minLoans, maxLoans, minPtp, maxPtp, minBalance, maxBalance))
            .collect(Collectors.toList());
    }

    public ScoredPhoneNumber score(PhoneNumberRawData raw,
                                    double minLoans, double maxLoans,
                                    double minPtp, double maxPtp,
                                    double minBalance, double maxBalance) {
        double normLoans   = normalize(raw.getPreviousLoans(), minLoans, maxLoans);
        double normPtp     = normalize(raw.getPromiseToPay(), minPtp, maxPtp);
        double normBalance = normalize(raw.getBalanceOwed(), minBalance, maxBalance);

        double reachScore = raw.getHistoricalAnswerRate() * 0.40
            + (raw.isAppInstalled() ? 1.0 : 0.0) * 0.20
            + (1.0 / (1 + raw.getDaysSinceLastContact())) * 0.20
            + normLoans * 0.10
            + (1 - normPtp) * 0.10;

        double recoveryScore = normBalance * 0.30
            + (raw.isBrokenPtp() ? 0.0 : 1.0) * 0.25
            + normPtp * 0.20
            + raw.getHistoricalAnswerRate() * 0.15
            + normLoans * 0.10;

        return new ScoredPhoneNumber(raw, reachScore, recoveryScore);
    }

    /** Used when all values in the batch are identical, placing each item at the midpoint. */
    static final double MIDPOINT = 0.5;

    private double normalize(double value, double min, double max) {
        if (max == min) return MIDPOINT;
        return (value - min) / (max - min);
    }
}
