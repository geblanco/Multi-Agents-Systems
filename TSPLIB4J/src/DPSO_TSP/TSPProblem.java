package DPSO_TSP;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.problem.tsplib.TSP2OptHeuristic;
import org.moeaframework.problem.tsplib.TSPInstance;
import org.moeaframework.problem.tsplib.Tour;

import static DPSO_TSP.Utils.fromTour;
import static DPSO_TSP.Utils.toTour;

/**
 * Created by Guillermo Echegoyen Blanco on 2018.
 */
public class TSPProblem extends AbstractProblem {

    /**
     * The TSP problem instance.
     */
    private final TSPInstance instance;

    /**
     * The TSP heuristic for aiding the optimization process.
     */
    private final TSP2OptHeuristic heuristic;

    /**
     * Constructs a new optimization problem for the given TSP problem
     * instance.
     *
     * @param instance the TSP problem instance
     */
    public TSPProblem(TSPInstance instance) {
        super(1, 1);
        this.instance = instance;

        heuristic = new TSP2OptHeuristic(instance);
    }

    @Override
    public void evaluate(Solution solution) {
        Tour tour = toTour(solution);

        // apply the heuristic and save the modified tour
        heuristic.apply(tour);
        fromTour(solution, tour);

        solution.setObjective(0, tour.distance(instance));
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(1, 1);

        solution.setVariable(0, EncodingUtils.newPermutation(instance.getDimension()));

        return solution;
    }

}