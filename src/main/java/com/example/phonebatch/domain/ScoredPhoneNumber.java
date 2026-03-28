package com.example.phonebatch.domain;

public class ScoredPhoneNumber {
    private PhoneNumberRawData rawData;
    private double reachScore;
    private double recoveryScore;

    public ScoredPhoneNumber() {}

    public ScoredPhoneNumber(PhoneNumberRawData rawData, double reachScore, double recoveryScore) {
        this.rawData = rawData;
        this.reachScore = reachScore;
        this.recoveryScore = recoveryScore;
    }

    public PhoneNumberRawData getRawData() { return rawData; }
    public void setRawData(PhoneNumberRawData rawData) { this.rawData = rawData; }
    public double getReachScore() { return reachScore; }
    public void setReachScore(double reachScore) { this.reachScore = reachScore; }
    public double getRecoveryScore() { return recoveryScore; }
    public void setRecoveryScore(double recoveryScore) { this.recoveryScore = recoveryScore; }
}
