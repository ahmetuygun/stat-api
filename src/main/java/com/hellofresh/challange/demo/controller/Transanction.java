package com.hellofresh.challange.demo.controller;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;

public class Transanction {

    private IntSummaryStatistics y;
    private DoubleSummaryStatistics x;

    public Transanction(IntSummaryStatistics y, DoubleSummaryStatistics x) {
        this.y = y;
        this.x = x;
    }

    public IntSummaryStatistics getY() {
        return y;
    }

    public void setY(IntSummaryStatistics y) {
        this.y = y;
    }

    public DoubleSummaryStatistics getX() {
        return x;
    }

    public void setX(DoubleSummaryStatistics x) {
        this.x = x;
    }
}
