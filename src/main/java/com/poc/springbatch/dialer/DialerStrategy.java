package com.poc.springbatch.dialer;

import com.poc.springbatch.model.PhoneScore;

import java.util.List;

/**
 * Strategy interface for sending a sorted phone list to an external dialler.
 *
 * Implementations are provided for Five9 and AWS Connect.
 * New diallers can be added by implementing this interface without touching
 * any existing code (Open/Closed Principle).
 */
public interface DialerStrategy {

    /**
     * Returns the name of this dialler (used for selection / logging).
     */
    String getDialerName();

    /**
     * Sends the ordered list of phone scores to the dialler.
     *
     * @param sortedScores phone entries sorted descending by reach score.
     */
    void sendPhoneList(List<PhoneScore> sortedScores);
}
