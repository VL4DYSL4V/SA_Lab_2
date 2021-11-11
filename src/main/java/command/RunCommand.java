package command;

import chart.ChartHelper;
import command.dto.MatricesDto;
import command.dto.ResultDto;
import framework.command.AbstractRunnableCommand;
import framework.exception.LaboratoryFrameworkException;
import framework.utils.ConsoleUtils;
import framework.utils.MatrixUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunCommand extends AbstractRunnableCommand {

    private static final String NAME = "run";

    public RunCommand() {
        super(NAME);
    }

    @Override
    public void execute(String[] strings) {
        ResultDto result = getResult();
        double T = (double) applicationState.getVariable("T");
        ChartHelper.getInstance().showNextChart(result, T);
        System.gc();
    }

    private ResultDto getResult() {
        MatricesDto dto = computeMatrices();

        int variant = (int) applicationState.getVariable("variant");
        RealMatrix C = (RealMatrix) applicationState.getVariable("C");
        RealMatrix L = (RealMatrix) applicationState.getVariable("L");
        double deltaL = (double) applicationState.getVariable("delta_l");
        int K = (int) applicationState.getVariable("K");
        double T = (double) applicationState.getVariable("T");
        RealVector Uk = (RealVector) applicationState.getVariable("Uk");

        List<RealVector> listX = computeListX(dto, K, L, Uk);
        List<RealVector> nonOptimalX = listX;

        double zeroJ = calculateJ(listX, T);
        double currentJ = Double.NEGATIVE_INFINITY;
        while (zeroJ > currentJ) {
            if (variant == 1) {
                L.setEntry(0, 1, L.getEntry(0, 1) + deltaL);
            } else {
                L.setEntry(0, 2, L.getEntry(0, 2) + deltaL);
            }
            listX = computeListX(dto, K, L, Uk);
            currentJ = calculateJ(listX, T);
        }

        String template = "Optimal l = %f";
        if (variant == 1) {
            ConsoleUtils.println(String.format(template, L.getEntry(0, 1)));
        } else {
            ConsoleUtils.println(String.format(template, L.getEntry(0, 2)));
        }

        List<RealVector> nonOptimalY = nonOptimalX.stream().map(C::operate).collect(Collectors.toList());
        List<RealVector> optimalY = listX.stream().map(C::operate).collect(Collectors.toList());
        return new ResultDto(optimalY, nonOptimalY);
    }

    private List<RealVector> computeListX(MatricesDto dto, int K, RealMatrix L, RealVector Uk) {
        RealVector x = new ArrayRealVector(dto.getG().getRowDimension());
        List<RealVector> listX = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            listX.add(x);
            x = computeVectorX(dto, L, Uk, x);
        }
        return listX;
    }

    private static double calculateJ(List<? extends RealVector> listX, double T) {
        return listX.stream()
                .map((x) -> Math.abs(x.getEntry(0) - 1) * T)
                .reduce(Double::sum)
                .orElseThrow(LaboratoryFrameworkException::new);
    }

    private RealVector computeVectorX(MatricesDto dto, RealMatrix L, RealVector Uk, RealVector previousX) {
        RealMatrix F = dto.getF();
        RealMatrix G = dto.getG();
        return F.subtract(G.multiply(L)).operate(previousX).add(G.operate(Uk));
    }

    private MatricesDto computeMatrices() {
        RealMatrix A = (RealMatrix) applicationState.getVariable("A");
        int q = (int) applicationState.getVariable("q");
        Map<Integer, RealMatrix> powerToMatrixInThatPower = MatrixUtils.getPowerToMatrixInThatPower(A, q);
        double T = (double) applicationState.getVariable("T");
        RealMatrix F = computeMatrixF(A, T, q, powerToMatrixInThatPower);
        RealMatrix B = (RealMatrix) applicationState.getVariable("B");
        RealMatrix G = computeMatrixG(A, B, T, q, powerToMatrixInThatPower);
        return new MatricesDto(F, G);
    }

    private RealMatrix computeMatrixF(RealMatrix A, double T, int q,
                                      Map<Integer, RealMatrix> powerToMatrixInThatPower) {
        RealMatrix F = new Array2DRowRealMatrix(A.getRowDimension(), A.getColumnDimension());
        for (int i = 0; i <= q; i++) {
            RealMatrix matrixToAdd = powerToMatrixInThatPower.get(i)
                    .scalarMultiply(Math.pow(T, i)).scalarMultiply(1.0 / CombinatoricsUtils.factorial(i));
            F = F.add(matrixToAdd);
        }
        return F;
    }

    private RealMatrix computeMatrixG(RealMatrix A, RealMatrix B, double T, int q,
                                      Map<Integer, RealMatrix> powerToMatrixInThatPower) {
        RealMatrix G = new Array2DRowRealMatrix(A.getRowDimension(), A.getColumnDimension());
        for (int i = 0; i <= q - 1; i++) {
            RealMatrix matrixToAdd = powerToMatrixInThatPower.get(i)
                    .scalarMultiply(Math.pow(T, i)).scalarMultiply(1.0 / CombinatoricsUtils.factorial(i + 1));
            G = G.add(matrixToAdd);
        }
        G = G.scalarMultiply(T);
        G = G.multiply(B);
        return G;
    }
}
