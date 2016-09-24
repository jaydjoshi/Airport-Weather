package com.crossover.trial.weather.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A collected point, including some information about the range of collected values
 * Changed the data types to double 
 * @author code test administrator
 */
public class DataPoint {

    private double mean = 0.0;

    private double first = 0;

    private double second = 0;

    private double third = 0;

    private double count = 0;

    /** private constructor, use the builder to create this object */
    @SuppressWarnings("unused")
	private DataPoint() { }
    

    public DataPoint(double first, double second, double mean, double third, double count) {
        this.setFirst(first);
        this.setMean(mean);
        this.setSecond(second);
        this.setThird(third);
        this.setCount(count);
    }

    /** the mean of the observations */
    public double getMean() {
        return mean;
    }

    public void setMean(double mean) { this.mean = mean; }

    /** 1st quartile -- useful as a lower bound */
    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    /** 2nd quartile -- median value */
    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    /** 3rd quartile value -- less noisy upper value */
    public double getThird() {
        return third;
    }

    public void setThird(double third) {
        this.third = third;
    }

    /** the total number of measurements */
    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    public boolean equals(Object that) {
        return this.toString().equals(that.toString());
    }

    static public class Builder {
        private double first;
        private double mean;
        private double median;
        private double last;
        private double count;

        public Builder() {
        	super();
        }

        public Builder withFirst(double first) {
            this.first= first;
            return this;
        }

        public Builder withMean(double mean) {
            this.mean = mean;
            return this;
        }

        public Builder withMedian(double median) {
            this.median = median;
            return this;
        }

        public Builder withCount(double count) {
            this.count = count;
            return this;
        }

        public Builder withLast(double last) {
            this.last = last;
            return this;
        }

        public DataPoint build() {
            return new DataPoint(this.first, this.mean, this.median, this.last, this.count);
        }
    }
}
