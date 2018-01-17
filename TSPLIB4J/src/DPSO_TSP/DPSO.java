package DPSO_TSP;

import org.moeaframework.algorithm.AbstractAlgorithm;
import org.moeaframework.algorithm.AlgorithmInitializationException;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.variable.EncodingUtils;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Guillermo Echegoyen Blanco on 2018.
 */
public class DPSO extends AbstractAlgorithm {

    // ToDo := Setup rLoc, rGlob, rRand, pRand, bLoc, bGlob
    private int swarmSize;
    private double bLoc;
    private double bGlob;
    private Solution[] particles;
    private Solution[] localBestParticles;
    private PSOFitnessBasedArchive leaders;

    public DPSO( Problem problem, int swarmSize, int leaderSize, double bLoc, double bGlob) {
        super(problem);
        this.swarmSize = swarmSize;
        this.leaders = new PSOFitnessBasedArchive( leaderSize );
        this.particles = new Solution[swarmSize];
        this.localBestParticles = new Solution[swarmSize];
        this.bLoc = bLoc;
        this.bGlob = bGlob;
    }

    private void updatePositions() {
        for(int i = 0; i < this.swarmSize; ++i) {
            this.updatePosition(i);
        }

    }

    // From the paper:
    // dLoc = Xi(t) + rLoc * bLoc * (Pi - Xi(t))
    // dGlob = Xi(t) + rGlob * bGlob * (Pglob - Xi(t))
    // Vrand = rRand * bRand * (pRand - Xi(t))
    // Xi(t+1) = dGlob + 1/2 * (dLoc - dGlob) + Vrand
    //
    // Pi: Particles best position
    // bLoc and bGlob determine the influence of the local and global attractors (acceleration coefficients)
    // rLoc and rGlob are drawn from a Uniform(0, 1)
    // Pi - Xi(t): Local attractor
    // Pglob - Xi(t): Global attractor
    //
    // Example: dLoc = Xi(t) + rLoc * bLoc * (Pi - Xi(t))
    // Get EdgeExchanges from Pi to Xi(t) -> transformations
    // Reduce transformations rLoc * bLoc * T -> transformations
    // Apply edge transformations dLoc = Xi(t) + T

    private ArrayList<Integer[]> getAttractor(int[] particle, int[] attractor, double cte){
        // (rLoc * bLoc) * (Pi - Xi(t))
        return Utils.EdgeExchangesByConstant( Utils.getListOfEdgeExchanges( attractor, particle ), cte );
    }

    private void updatePosition(int i) {
        Random rnd = new Random();
        Solution parent = this.particles[i];
        Solution offspring = parent.copy();

        double rLoc = rnd.nextDouble();
        double rGlob = rnd.nextDouble();
        // ToDo := Just by now, this value should be set on instantiation

        int[] particles = EncodingUtils.getPermutation(this.particles[i].getVariable( 0 ));
        int[] localParticles = EncodingUtils.getPermutation(this.localBestParticles[i].getVariable( 0 ));

        // dLoc = Xi(t) + rLoc * bLoc * (Pi - Xi(t))
        int[] dloc = Utils.applyEdgeExchanges(particles, getAttractor( particles, localParticles, rLoc * bLoc ));
        // dGlob = Xi(t) + rGlob * bGlob * (Pglob - Xi(t))
        int[] globalBest = EncodingUtils.getPermutation(selectLeader().getVariable( 0 ));
        int[] dGlob = Utils.applyEdgeExchanges(particles, getAttractor(particles, globalBest, rGlob * bGlob));

        // Vrand = rRand * bRand * (pRand - Xi(t))
        double bRand = rnd.nextDouble();
        double rRand = rnd.nextDouble();
        int[] pRand = EncodingUtils.getPermutation(this.problem.newSolution().getVariable( 0 ));
        ArrayList<Integer[]> vRand = getAttractor(particles, pRand, bRand * rRand );

        // Xi(t+1) = dGlob + 1/2 * (dLoc - dGlob) + Vrand
        dGlob = Utils.applyEdgeExchanges(dGlob, Utils.EdgeExchangesAddition(getAttractor( dGlob, dloc, 0.5 ), vRand));

        EncodingUtils.setPermutation( offspring.getVariable( 0 ), dGlob );
        this.particles[i] = offspring;
    }

    private Solution selectLeader() {
        // ToDo := Neighbouring technique
        Random rnd = new Random();
        Solution leader1 = this.leaders.get(rnd.nextInt(this.leaders.size()));
        Solution leader2 = this.leaders.get(rnd.nextInt(this.leaders.size()));
        int flag = (int) Math.round( leader1.getObjective(0) - leader2.getObjective(0) );
        if (flag < 0) {
            return leader1;
        } else if (flag > 0) {
            return leader2;
        } else {
            return rnd.nextBoolean() ? leader1 : leader2;
        }
    }

    private void updateBests() {
        for(int i = 0; i < this.swarmSize; ++i) {
            int best = (int) Math.round( this.particles[i].getObjective(0) - this.localBestParticles[i].getObjective(0) );
            if( best < 0 ){
                this.localBestParticles[i] = this.particles[i];
            }
        }

    }

    public NondominatedPopulation getResult() {
        return new NondominatedPopulation( Arrays.asList( this.leaders.toArray() ) );
    }

    public Solution[] getBest() {
        Solution[] particles = this.localBestParticles.clone();
        Arrays.sort(particles, new Utils.ParticleComparator(true));
        return particles;
    }

    protected void initialize() {
        super.initialize();
        Solution[] initialParticles = (new RandomInitialization(this.problem, this.swarmSize)).initialize();
        this.evaluateAll(initialParticles);

        for(int i = 0; i < this.swarmSize; ++i) {
            this.particles[i] = initialParticles[i];
            this.localBestParticles[i] = initialParticles[i];
        }

        this.leaders.addAll(initialParticles);
        this.leaders.update();
    }

    protected void iterate() {
        this.updatePositions();
        this.evaluateAll(this.particles);
        this.updateBests();
        this.leaders.addAll(this.localBestParticles);
        this.leaders.update();
    }

    protected Solution[] getSwarm(){
        return this.localBestParticles;
    }

    @SuppressWarnings( "unchecked" )
    public Serializable getState() {
        if (!this.isInitialized()) {
            throw new AlgorithmInitializationException(this, "algorithm not initialized");
        } else {
            List<Solution> particlesList = Arrays.asList(this.particles);
            List<Solution> localBestParticlesList = Arrays.asList(this.localBestParticles);
            List<Solution> leadersList = new ArrayList();

            for(int i = 0; i < this.leaders.size(); i++){
                leadersList.add( this.leaders.get( i ) );
            }

            return new Utils.DPSOAlgorithmState(this.getNumberOfEvaluations(), particlesList, localBestParticlesList, leadersList);
        }
    }

    public void setState(Object objState) throws NotSerializableException {
        super.initialize();
        Utils.DPSOAlgorithmState state = (Utils.DPSOAlgorithmState)objState;
        this.numberOfEvaluations = state.getNumberOfEvaluations();
        if (state.getParticles().size() != this.swarmSize) {
            throw new NotSerializableException("swarmSize does not match serialized state");
        } else {
            int i;
            for(i = 0; i < this.swarmSize; ++i) {
                this.particles[i] = state.getParticles().get(i);
            }

            for(i = 0; i < this.swarmSize; ++i) {
                this.localBestParticles[i] = state.getLocalBestParticles().get(i);
            }

            this.leaders.addAll(state.getLeaders());
            this.leaders.update();
        }
    }
}
