package ru.skuptsov.differentiation;

import java.util.function.Consumer;

/**
 * @author Sergey Kuptsov
 */
public class Node {
    /**
     * actual data
     */
    private double data;
    /**
     * derivative value
     */
    private double derivative;
    /**
     * function for calculating derivative
     */
    private Consumer<Double> autoDifferentiationFn;
    /**
     * is requires derivative
     */
    private boolean requiresDerivative;

    private Node(double data, boolean requiresDerivative) {
        this.data = data;
        this.requiresDerivative = requiresDerivative;
        this.autoDifferentiationFn = (prevDerivative) -> derivative = prevDerivative;
    }

    public static Node variable(double data) {
        return new Node(data, true);
    }

    private static final Consumer<Double> emptyFn = v -> {
    };

    private Node(double data, boolean requiresDerivative, Consumer<Double> autoDifferentiationFn) {
        this.data = data;
        this.requiresDerivative = requiresDerivative;
        this.autoDifferentiationFn = autoDifferentiationFn;
    }

    public static Node param(double data) {
        return new Node(data, false, emptyFn);
    }

    public static Node multiply(Node node1, Node node2) {
        double data = node1.data * node2.data;
        boolean requiresDerivative = node1.requiresDerivative || node2.requiresDerivative;

        Consumer<Double> node1F = node1.requiresDerivative ?
                prevGrad -> node1.autoDifferentiationFn
                        .accept(prevGrad * node2.data)
                : emptyFn;

        Consumer<Double> node2F = node2.requiresDerivative ?
                prevGrad -> node2.autoDifferentiationFn
                        .accept(prevGrad * node1.data)
                : emptyFn;

        Consumer<Double> autoDifferentiationFn = v -> {
            node1F.accept(v);
            node2F.accept(v);
        };

        return new Node(data, requiresDerivative, autoDifferentiationFn);
    }

    public static Node plus(Node node1, Node node2) {
        boolean requiresDerivative = node1.requiresDerivative || node2.requiresDerivative;
        double data = node1.data + node2.data;

        Consumer<Double> node1F = node1.requiresDerivative ?
                prevGrad -> node1.autoDifferentiationFn
                        .accept(prevGrad)
                : emptyFn;

        Consumer<Double> node2F = node2.requiresDerivative ?
                prevGrad -> node2.autoDifferentiationFn
                        .accept(prevGrad)
                : emptyFn;

        Consumer<Double> autoDifferentiationFn = prevGrad -> {
            node1F.accept(prevGrad);
            node2F.accept(prevGrad);
        };

        return new Node(data, requiresDerivative, autoDifferentiationFn);
    }

    public static Node minus(Node node1, Node node2) {
        boolean requiresDerivative = node1.requiresDerivative || node2.requiresDerivative;
        double data = node1.data - node2.data;

        Consumer<Double> node1F = node1.requiresDerivative ?
                prevGrad -> node1.autoDifferentiationFn
                        .accept(prevGrad)
                : emptyFn;

        Consumer<Double> node2F = node2.requiresDerivative ?
                prevGrad -> node2.autoDifferentiationFn
                        .accept(prevGrad)
                : emptyFn;

        Consumer<Double> autoDifferentiationFn = prevGrad -> {
            node1F.accept(prevGrad);
            node2F.accept(prevGrad);
        };

        return new Node(data, requiresDerivative, autoDifferentiationFn);
    }

    public static Node pow_2(Node node) {
        double data = node.data * node.data;
        boolean requiresDerivative = node.requiresDerivative;

        Consumer<Double> autoDifferentiationFn = node.requiresDerivative
                ? prevGrad -> node.autoDifferentiationFn.accept(prevGrad * 2 * node.data)
                : emptyFn;

        return new Node(data, requiresDerivative, autoDifferentiationFn);
    }

    public double getDerivative() {
        return derivative;
    }

    public double getData() {
        return data;
    }

    public void autoDifferentiation() {
        autoDifferentiationFn.accept(1.0);
    }
}
