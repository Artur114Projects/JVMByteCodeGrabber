package com.artur114.bytecodegrab.util;

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

    public float x1F() {
        return this.percent;
    }

    public int x1I() {
        return (int) this.percent;
    }

    public float x100F() {
        return this.percent * 100.0F;
    }

    public int x100I() {
        return (int) ((int) this.percent * 100.0F);
    }

    public int x1kI() {
        return (int) (this.percent * 1000.0F);
    }

    public float x1kF() {
        return this.percent * 1000.0F;
    }

    public int x10kI() {
        return (int) (this.percent * 10000.0F);
    }

    public float x10kF() {
        return this.percent * 10000.0F;
    }

    public int x100kI() {
        return (int) (this.percent * 100000.0F);
    }
}
