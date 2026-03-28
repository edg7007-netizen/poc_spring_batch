package com.example.phonebatch.service;

/**
 * Holds dataset-wide min/max values used for min-max normalisation in {@link ScoringService}.
 * Populated once per job run by {@link ScoringStatsListener} before the scoring step starts.
 */
public class ScoringStats {

    private double minLoans;
    private double maxLoans;
    private double minPtp;
    private double maxPtp;
    private double minBalance;
    private double maxBalance;

    public ScoringStats() {}

    public ScoringStats(double minLoans, double maxLoans,
                        double minPtp, double maxPtp,
                        double minBalance, double maxBalance) {
        this.minLoans = minLoans;
        this.maxLoans = maxLoans;
        this.minPtp = minPtp;
        this.maxPtp = maxPtp;
        this.minBalance = minBalance;
        this.maxBalance = maxBalance;
    }

    public double getMinLoans()   { return minLoans; }
    public double getMaxLoans()   { return maxLoans; }
    public double getMinPtp()     { return minPtp; }
    public double getMaxPtp()     { return maxPtp; }
    public double getMinBalance() { return minBalance; }
    public double getMaxBalance() { return maxBalance; }

    public void setMinLoans(double v)   { this.minLoans = v; }
    public void setMaxLoans(double v)   { this.maxLoans = v; }
    public void setMinPtp(double v)     { this.minPtp = v; }
    public void setMaxPtp(double v)     { this.maxPtp = v; }
    public void setMinBalance(double v) { this.minBalance = v; }
    public void setMaxBalance(double v) { this.maxBalance = v; }
}
