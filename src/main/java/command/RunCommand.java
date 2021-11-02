package command;

import chart.ChartHelper;
import command.dto.MatricesDto;
import dao.FileSystemVectorXDao;
import dao.VectorXDao;
import framework.command.AbstractRunnableCommand;
import framework.utils.ConsoleUtils;
import framework.utils.MatrixUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunCommand extends AbstractRunnableCommand {

    private static final String NAME = "run";

    private static final Map<Integer, VectorXDao> DAOS = new HashMap<>();

    public RunCommand() {
        super(NAME);
        DAOS.put(1, new FileSystemVectorXDao(1));
        DAOS.put(2, new FileSystemVectorXDao(2));
    }

    @Override
    public void execute(String[] strings) {
        MatricesDto matricesDto = computeMatrices();
        List<RealVector> sequenceY = computeYsAndWriteXs(matricesDto);
        if (!sequenceY.isEmpty()) {
            double T = (double) applicationState.getVariable("T");
            ChartHelper.getInstance().showNextChart(sequenceY, T);
        } else {
            ConsoleUtils.println("No y-s were computed!");
        }
        System.gc();
    }

    private List<RealVector> computeYsAndWriteXs(MatricesDto dto) {
        int variant = (int) applicationState.getVariable("variant");
        VectorXDao dao = DAOS.get(variant);
        RealMatrix C = (RealMatrix) applicationState.getVariable("C");
        RealMatrix L = (RealMatrix) applicationState.getVariable("L");
        double deltaL = (double) applicationState.getVariable("delta_l");
        int K = (int) applicationState.getVariable("K");
        double T = (double) applicationState.getVariable("T");
        RealVector Uk = (RealVector) applicationState.getVariable("Uk");
        RealVector x = new ArrayRealVector(dto.getG().getRowDimension());

        List<RealVector> out = new ArrayList<>(K);
        double J = 0.0;
        double previousJ = J;
        for (int i = 0; i < K; i++) {
            RealVector y = C.operate(x);
            out.add(y);
            if (i % 25 == 0) {
                dao.write(i, x);
            }

            J += Math.abs(x.getEntry(0) - 1) * T;
            if (previousJ < J) {
                deltaL *= -1;
            }
            if (variant == 1) {
                L.setEntry(0, 1, L.getEntry(0, 1) + deltaL);
            } else {
                L.setEntry(0, 2, L.getEntry(0, 2) + deltaL);
            }
            previousJ = J;
            x = computeVectorX(dto, L, Uk, x);
        }
        return out;
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
