package com.example.phonebatch.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feature_store")
public class FeatureStoreEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private int previousLoans;
    private int promiseToPay;
    private double historicalAnswerRate;
    private int daysSinceLastContact;
    private boolean brokenPtp;
    private boolean appInstalled;
    private double balanceOwed;
    private double reachScore;
    private double recoveryScore;
    private LocalDateTime jobDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public int getPreviousLoans() { return previousLoans; }
    public void setPreviousLoans(int previousLoans) { this.previousLoans = previousLoans; }
    public int getPromiseToPay() { return promiseToPay; }
    public void setPromiseToPay(int promiseToPay) { this.promiseToPay = promiseToPay; }
    public double getHistoricalAnswerRate() { return historicalAnswerRate; }
    public void setHistoricalAnswerRate(double historicalAnswerRate) { this.historicalAnswerRate = historicalAnswerRate; }
    public int getDaysSinceLastContact() { return daysSinceLastContact; }
    public void setDaysSinceLastContact(int daysSinceLastContact) { this.daysSinceLastContact = daysSinceLastContact; }
    public boolean isBrokenPtp() { return brokenPtp; }
    public void setBrokenPtp(boolean brokenPtp) { this.brokenPtp = brokenPtp; }
    public boolean isAppInstalled() { return appInstalled; }
    public void setAppInstalled(boolean appInstalled) { this.appInstalled = appInstalled; }
    public double getBalanceOwed() { return balanceOwed; }
    public void setBalanceOwed(double balanceOwed) { this.balanceOwed = balanceOwed; }
    public double getReachScore() { return reachScore; }
    public void setReachScore(double reachScore) { this.reachScore = reachScore; }
    public double getRecoveryScore() { return recoveryScore; }
    public void setRecoveryScore(double recoveryScore) { this.recoveryScore = recoveryScore; }
    public LocalDateTime getJobDate() { return jobDate; }
    public void setJobDate(LocalDateTime jobDate) { this.jobDate = jobDate; }
}
