package ru.skuptsov.differentiation;

/**
 * @author Sergey Kuptsov
 */
public class AutoDifferentiationTest {

    public static void main(String[] args) {
        // x=2, y=3, alpha = 4, beta=5, then
        Node x = Node.param(2);
        Node y = Node.param(3);
        Node alpha = Node.variable(4);
        Node beta = Node.variable(5);

        // loss = (alphax+beta-y)^2
        Node w5 = Node.multiply(alpha, x);
        Node w6 = Node.plus(w5, beta);
        Node w7 = Node.minus(w6, y);
        Node loss = Node.pow_2(w7);

        loss.autoDifferentiation();

        System.out.println("loss value: " + loss.getData());
        System.out.println("alpha derivative: " + alpha.getDerivative());
        System.out.println("beta derivative: " + beta.getDerivative());
        System.out.println("x derivative: " + x.getDerivative());
    }
}
