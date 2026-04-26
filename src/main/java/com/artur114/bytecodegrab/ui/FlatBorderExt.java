package com.artur114.bytecodegrab.ui;

import com.formdev.flatlaf.ui.FlatBorder;

import java.awt.*;

public class FlatBorderExt extends FlatBorder {
    public FlatBorderExt setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
        return this;
    }

    public FlatBorderExt setInnerFocusWidth(float innerFocusWidth) {
        this.innerFocusWidth = innerFocusWidth;
        return this;
    }

    public FlatBorderExt setInnerOutlineWidth(float innerOutlineWidth) {
        this.innerOutlineWidth = innerOutlineWidth;
        return this;
    }

    public FlatBorderExt setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public FlatBorderExt setFocusColor(Color focusColor) {
        this.focusColor = focusColor;
        return this;
    }

    public FlatBorderExt setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public FlatBorderExt setDisabledBorderColor(Color disabledBorderColor) {
        this.disabledBorderColor = disabledBorderColor;
        return this;
    }

    public FlatBorderExt setFocusedBorderColor(Color focusedBorderColor) {
        this.focusedBorderColor = focusedBorderColor;
        return this;
    }

    public FlatBorderExt setErrorBorderColor(Color errorBorderColor) {
        this.errorBorderColor = errorBorderColor;
        return this;
    }

    public FlatBorderExt setErrorFocusedBorderColor(Color errorFocusedBorderColor) {
        this.errorFocusedBorderColor = errorFocusedBorderColor;
        return this;
    }

    public FlatBorderExt setWarningBorderColor(Color warningBorderColor) {
        this.warningBorderColor = warningBorderColor;
        return this;
    }

    public FlatBorderExt setWarningFocusedBorderColor(Color warningFocusedBorderColor) {
        this.warningFocusedBorderColor = warningFocusedBorderColor;
        return this;
    }

    public FlatBorderExt setSuccessBorderColor(Color successBorderColor) {
        this.successBorderColor = successBorderColor;
        return this;
    }

    public FlatBorderExt setSuccessFocusedBorderColor(Color successFocusedBorderColor) {
        this.successFocusedBorderColor = successFocusedBorderColor;
        return this;
    }

    public FlatBorderExt setCustomBorderColor(Color customBorderColor) {
        this.customBorderColor = customBorderColor;
        return this;
    }

    public FlatBorderExt setOutline(String outline) {
        this.outline = outline;
        return this;
    }

    public FlatBorderExt setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    public FlatBorderExt setOutlineFocusedColor(Color outlineFocusedColor) {
        this.outlineFocusedColor = outlineFocusedColor;
        return this;
    }
}
