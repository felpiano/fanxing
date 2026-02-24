package com.ruoyi.system.weight;

public class Weighted<T> {
    private final T element;
    private final double weight;

    public Weighted(T element, double weight) {
        this.element = element;
        this.weight = weight;
    }

    public T getElement() {
        return element;
    }

    public double getWeight() {
        return weight;
    }
}
