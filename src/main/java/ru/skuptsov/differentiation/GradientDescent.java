package ru.skuptsov.differentiation;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * @author Sergey Kuptsov
 */
public class GradientDescent {
    public static void main(String[] args) {
        Random random = new Random();
        DecimalFormat format = new DecimalFormat("#0.000");

        // y = ax + b
        double a = 2;
        double b = 3.5;
        System.out.println("predicting a=" + a + " b=" + b);

        // fill series with scatter
        int seriesCount = 1000;
        double[][] xy = new double[seriesCount][2];
        for (int i = 0; i < seriesCount; i++) {
            double x = random.nextInt(10);
            double y = a * x + b + random.nextDouble();

            xy[i][0] = x;
            xy[i][1] = y;
        }

        // start with random values
        Node α = Node.variable(random.nextDouble());
        Node β = Node.variable(random.nextDouble());
        System.out.println("at start α=" + format.format(α.getData()) + " β=" + format.format(β.getData()));

        int epochs = 50;
        double η = 0.0001;
        for (int i = 0; i < epochs; i++) {

            double avg_loss = 0;
            for (int j = 0; j < xy.length; j++) {

                Node x = Node.param(xy[j][0]);
                Node y = Node.param(xy[j][1]);

                // count loss function
                Node w5 = Node.multiply(α, x);
                Node w6 = Node.plus(w5, β);
                Node w7 = Node.minus(w6, y);
                Node loss = Node.pow_2(w7);

                // count average loss on every serie
                avg_loss += loss.getData() / seriesCount;

                // make differentiation
                loss.autoDifferentiation();

                // adjust α and β in respect to average gradient descent
                α = Node.variable(α.getData() - η * α.getDerivative());
                β = Node.variable(β.getData() - η * β.getDerivative());
            }

            System.out.println("epoch " + i + " α=" + format.format(α.getData()) + " β=" + format.format(β.getData()) + " avg loss=" + format.format(avg_loss));

        }
    }
}
