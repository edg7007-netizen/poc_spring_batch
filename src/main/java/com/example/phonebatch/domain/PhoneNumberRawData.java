package com.example.phonebatch.domain;

public class PhoneNumberRawData {
    private String phoneNumber;
    private int previousLoans;
    private int promiseToPay;
    private double historicalAnswerRate;
    private int daysSinceLastContact;
    private boolean brokenPtp;
    private boolean appInstalled;
    private double balanceOwed;

    public PhoneNumberRawData() {}

    public PhoneNumberRawData(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

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
}
