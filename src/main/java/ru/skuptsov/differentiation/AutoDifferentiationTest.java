package ru.skuptsov.differentiation;

/**
 * @author Sergey Kuptsov
 */
public class AutoDifferentiationTest {

    public static void main(String[] args) {
        // x=2, y=3, α = 4, β=5, then
        Node x = Node.param(2);
        Node y = Node.param(3);
        Node α = Node.variable(4);
        Node β = Node.variable(5);

        // loss = (αx+β-y)^2
        Node w5 = Node.multiply(α, x);
        Node w6 = Node.plus(w5, β);
        Node w7 = Node.minus(w6, y);
        Node loss = Node.pow_2(w7);

        loss.autoDifferentiation();

        System.out.println("loss value: " + loss.getData());
        System.out.println("α derivative: " + α.getDerivative());
        System.out.println("β derivative: " + β.getDerivative());
        System.out.println("x derivative: " + x.getDerivative());
    }
}
