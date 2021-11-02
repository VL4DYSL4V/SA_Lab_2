package state;

import framework.state.AbstractApplicationState;
import framework.state.StateHelper;
import framework.utils.ConsoleUtils;
import framework.utils.MatrixUtils;
import lombok.Getter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;

@Getter
public class LaboratoryState extends AbstractApplicationState {

    private final int K = 5000;

    private int variant = 1;

    private double a1 = 1.0;

    private double a2 = 3.0;

    private double b = 1.0;

    private double l = -0.25;

    private double T = 0.02;

    private int q = 10;

    private double delta_l = 0.05;

    @Override
    protected void initVariableNameToSettersMap() {
        variableNameToSetter.put("variant", StateHelper.getIntegerSetter("variant", this::setVariant));
        variableNameToSetter.put("a1", StateHelper.getDoubleSetter("a1", this::setA1));
        variableNameToSetter.put("a2", StateHelper.getDoubleSetter("a2", this::setA2));
        variableNameToSetter.put("b", StateHelper.getDoubleSetter("b", this::setB));
        variableNameToSetter.put("l", StateHelper.getDoubleSetter("l", this::setL));
        variableNameToSetter.put("T", StateHelper.getDoubleSetter("T", this::setT));
        variableNameToSetter.put("q", StateHelper.getIntegerSetter("q", this::setQ));
        variableNameToSetter.put("delta_l", StateHelper.getIntegerSetter("delta_l", this::setDelta_l));
    }

    @Override
    protected void initVariableNameToGettersMap() {
        variableNameToGetter.put("variant", this::getVariant);
        variableNameToGetter.put("K", this::getK);
        variableNameToGetter.put("Uk", () -> new ArrayRealVector(new double[]{1.0}));
        variableNameToGetter.put("a1", this::getA1);
        variableNameToGetter.put("a2", this::getA2);
        variableNameToGetter.put("b", this::getB);
        variableNameToGetter.put("l", this::getL);
        variableNameToGetter.put("T", this::getT);
        variableNameToGetter.put("q", this::getQ);
        variableNameToGetter.put("A", () -> MatrixUtils.getFrobeniusMatrix(new double[]{1, a1, a2}));
        variableNameToGetter.put("B", () -> new Array2DRowRealMatrix(new double[]{0, 0, b}));
        variableNameToGetter.put("C", () -> new Array2DRowRealMatrix(new double[][]{{1, 0, 0}}));
        variableNameToGetter.put("delta_l", this::getDelta_l);
        variableNameToGetter.put("L", () -> {
            if (variant == 1) {
                return new Array2DRowRealMatrix(new double[][]{{0, l, 0}});
            }
            return new Array2DRowRealMatrix(new double[][]{{0, 0, l}});
        });
    }

    public void setVariant(int variant) {
        if (variant == 1 || variant == 2) {
            this.variant = variant;
        } else {
            ConsoleUtils.println(String.format("Invalid variant: %d", variant));
        }
    }

    public void setA1(double a1) {
        if (a1 < 1 || a1 > 10) {
            ConsoleUtils.println("a1 \u2209 [1, 10]");
            return;
        }
        if (a1 * a2 <= 1) {
            ConsoleUtils.println("a1 * a2 <= 1");
            return;
        }
        this.a1 = a1;
    }

    public void setA2(double a2) {
        if (a2 < 1 || a2 > 10) {
            ConsoleUtils.println("a2 \u2209 [1, 10]");
            return;
        }
        if (a1 * a2 <= 1) {
            ConsoleUtils.println("a1 * a2 <= 1");
            return;
        }
        this.a2 = a2;
    }

    public void setB(double b) {
        this.b = b;
    }

    public void setL(double l) {
        this.l = l;
    }

    public void setT(double t) {
        if (T < 0.001 || T > 0.1) {
            ConsoleUtils.println("T \u2209 [0.001, 0.1]");
            return;
        }
        this.T = t;
    }

    public void setQ(int q) {
        if (q < 6 || q > 10) {
            ConsoleUtils.println("q \u2209 [6, 10]");
            return;
        }
        this.q = q;
    }

    public void setDelta_l(double delta_l) {
        this.delta_l = delta_l;
    }
}
