package command.dto;

import lombok.Data;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

@Data
public class ResultDto {

    private final List<RealVector> optimalY;

    private final List<RealVector> nonOptimalY;

}
