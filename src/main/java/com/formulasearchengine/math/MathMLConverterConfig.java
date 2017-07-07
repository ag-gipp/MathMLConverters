package com.formulasearchengine.math;

import com.formulasearchengine.math.latexml.LateXMLConfig;
import com.formulasearchengine.math.mathoid.MathoidConfig;

/**
 * General settings for math related transformations.
 *
 * @author Vincent Stange
 */
public class MathMLConverterConfig {

    /**
     * Configuration of Encoplot algorithm
     */
    private LateXMLConfig latexml;

    /**
     * Configuration of Sherlock algorithm
     */
    private MathoidConfig mathoid;

    public LateXMLConfig getLatexml() {
        return latexml;
    }

    public MathMLConverterConfig setLatexml(LateXMLConfig latexml) {
        this.latexml = latexml;
        return this;
    }

    public MathoidConfig getMathoid() {
        return mathoid;
    }

    public MathMLConverterConfig setMathoid(MathoidConfig mathoid) {
        this.mathoid = mathoid;
        return this;
    }
}
