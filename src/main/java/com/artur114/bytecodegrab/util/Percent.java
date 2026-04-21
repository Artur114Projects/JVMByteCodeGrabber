package com.artur114.bytecodegrab.util;

import javax.swing.*;

public class Percent {
    private boolean isIndeterminate = false;
    private float percent = 0;

    public Percent setPercent(float percent) {
        if (percent > 100.0F) {
            percent = 100.0F;
        }

        this.percent = percent;

        if (this.isIndeterminate) {
            this.isIndeterminate = false;
        }

        return this;
    }

    public Percent setPercent(int currentValue, int maxValue) {
        currentValue++;
        if (currentValue > maxValue) {
            currentValue = maxValue;
        }

        this.percent = (float) currentValue / maxValue;

        if (this.isIndeterminate) {
            this.isIndeterminate = false;
        }

        return this;
    }

    public Percent setIndeterminate(boolean indeterminate) {
        this.isIndeterminate = indeterminate;

        return this;
    }

    public boolean isIndeterminate() {
        return this.isIndeterminate;
    }

    public int x1k() {
        return (int) (this.percent * 1000.0F);
    }

    public int x10k() {
        return (int) (this.percent * 10000.0F);
    }

    public int x100k() {
        return (int) (this.percent * 100000.0F);
    }
}
