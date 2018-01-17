package DPSO_TSP;

import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.tsplib.Tour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Guillermo Echegoyen Blanco on 2016.
 */
public class Utils {
    // ======================== Edge Operations (From the paper) ========================
    public static ArrayList<Integer[]> getListOfEdgeExchanges(int[] tour1, int[] tour2){
        ArrayList<Integer[]> list = new ArrayList<Integer[]>();
        if( tour1 == tour2 ){
            return list;
        }else if( tour1.length != tour2.length ){
            return list;
        }else if( isEquivalent( tour1, tour2 ) ){
            return list;
        }

        while( !isEquivalent( tour1, tour2 ) ){
            for( int i = 0; i < tour1.length; i++ ) {
                if( tour1[i] != tour2[i] ){
                    int idx = indexOf(tour1, tour2[i]);
                    tour1 = reverse(tour1, i, idx );
                    list.add( new Integer[]{i, idx} );
                }
            }
        }

        return list;
    }

    private static int indexOf(int[] self, int value){
        int i = 0;
        for(; i < self.length && self[i] != value; i++);
        return i;
    }

    private static boolean isEquivalent(int[] self, int[] other) {
        // two equivalent tours must have the same length
        if( self.length != other.length ){
            return false;
        }

        // find index of matching node
        int startingIndex = -1;

        for( int i = 0; i < self.length; i++ ){
            if( self[0] == other[i] ){
                startingIndex = i;
                break;
            }
        }

        // if for some reason no matching id was found
        if( startingIndex == -1 ){
            return false;
        }

        // scan one direction to see if tours are equal
        boolean isEqual = true;

        for( int i = 0; i < self.length; i++ ){
            if( self[i] != other[(startingIndex+i) % other.length] ){
                isEqual = false;
                break;
            }
        }

        // if necessary, scan the other direction to see if tours are equal
        if( !isEqual ){
            isEqual = true;

            for( int i = 0; i < self.length; i++ ){
                if( self[i] != other[(startingIndex-i + other.length) % other.length] ){
                    isEqual = false;
                    break;
                }
            }
        }

        return isEqual;
    }

    private static int[] reverse(int[] self, int i, int j) {
        while( j < i ){
            j += self.length;
        }

        for( int k = 0; k < (j - i + 1) / 2; k++ ){
            int temp = self[i+k];
            self[(i+k) % self.length] = self[(j-k) % self.length];
            self[(j-k) % self.length] = temp;
        }
        return self;
    }

    public static int[] applyEdgeExchanges(int[] tour, ArrayList<Integer[]> exchanges){
        for(Integer[] exchange: exchanges){
            tour = reverse( tour, exchange[0], exchange[1] );
        }
        return tour;
    }

    // addition T1 + T2 = t1.1, ..., t1.k, t2.1, ..., t2.l
    protected static ArrayList<Integer[]> EdgeExchangesByConstant(ArrayList<Integer[]> exchanges, double constant){
        int idx = (int) Math.round(exchanges.size() * constant);
        ArrayList<Integer[]> tmp = new ArrayList<Integer[]>();
        for(int i = 0; i < idx; i++){
            tmp.add( exchanges.get( i ) );
        }
        return tmp;
    }

    // multiplication s * T => t1, ... t[s*k]
    protected static ArrayList<Integer[]> EdgeExchangesAddition(ArrayList<Integer[]> exchanges1, ArrayList<Integer[]> exchanges2){
        ArrayList<Integer[]> list = new ArrayList<Integer[]>( exchanges1.size() + exchanges2.size() );
        list.addAll( exchanges1 );
        list.addAll( exchanges2 );
        return list;
    }

    // ======================== DPSO Utils ========================
    // Serialization state for moea framework
    public static class DPSOAlgorithmState implements Serializable {
        private static final long serialVersionUID = -1895823731827106938L;
        private final int numberOfEvaluations;
        private final List<Solution> particles;
        private final List<Solution> localBestParticles;
        private final List<Solution> leaders;

        protected DPSOAlgorithmState( int numberOfEvaluations, List<Solution> particles, List<Solution> localBestParticles, List<Solution> leaders ) {
            this.numberOfEvaluations = numberOfEvaluations;
            this.particles = particles;
            this.localBestParticles = localBestParticles;
            this.leaders = leaders;
        }

        protected int getNumberOfEvaluations() {
            return this.numberOfEvaluations;
        }

        protected List<Solution> getParticles() {
            return this.particles;
        }

        protected List<Solution> getLocalBestParticles() {
            return this.localBestParticles;
        }

        protected List<Solution> getLeaders() {
            return this.leaders;
        }

    }

    // ======================== TSP Utils ========================
    /**
     * Converts a MOEA Framework solution to a {@link Tour}.
     *
     * @param solution the MOEA Framework solution
     * @return the tour defined by the solution
     */
    public static Tour toTour(Solution solution) {
        int[] permutation = EncodingUtils.getPermutation(
                solution.getVariable(0));

        // increment values since TSP nodes start at 1
        for (int i = 0; i < permutation.length; i++) {
            permutation[i]++;
        }

        return Tour.createTour(permutation);
    }

    /**
     * Saves a {@link Tour} into a MOEA Framework solution.
     *
     * @param solution the MOEA Framework solution
     * @param tour the tour
     */
    public static void fromTour(Solution solution, Tour tour) {
        int[] permutation = tour.toArray();

        // decrement values to get permutation
        for (int i = 0; i < permutation.length; i++) {
            permutation[i]--;
        }

        EncodingUtils.setPermutation(solution.getVariable(0), permutation);
    }

    public static class ParticleComparator implements DominanceComparator, Comparator<Solution>, Serializable {

        private boolean compareByObjective;

        public ParticleComparator(){
            this(true);
        }

        public ParticleComparator(boolean compareByObjective){
            this.compareByObjective = compareByObjective;
        }

        public int compare(Solution solution1, Solution solution2) {
            Double sol1Value;
            Double sol2Value;
            if( this.compareByObjective ){
                sol1Value = solution1.getObjective( 0 );
                sol2Value = solution2.getObjective( 0 );
            }else{
                sol1Value = (Double) solution1.getAttribute( "fitness" );
                sol2Value = (Double) solution2.getAttribute( "fitness" );
            }
            return (int)(sol1Value - sol2Value);
        }
    }
}
