package lxx.utils;

/**
 * User: pipsi
 * Date: 30.10.2009
 */
public class RandomValue<T> {

    private T value;
    private double probability;

    public RandomValue(T value, double probability) {
        this.value = value;
        this.probability = probability;
    }

    public T getValue() {
        return value;
    }

    public double getProbability() {
        return probability;
    }

    public String toString() {
        return value.toString() + ":" + probability;
    }
}
