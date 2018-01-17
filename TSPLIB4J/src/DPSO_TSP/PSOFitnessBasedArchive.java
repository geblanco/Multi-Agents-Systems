package DPSO_TSP;

import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Guillermo Echegoyen Blanco on 2018.
 */
public class PSOFitnessBasedArchive {

    private ArrayList<Solution> leaders;
    private PSOFitnessEvaluator evaluator;
    private int leaderSize;

    public PSOFitnessBasedArchive(int leaderSize){
        this.leaders = new ArrayList<Solution>( leaderSize );
        this.leaderSize = leaderSize;
        this.evaluator = new PSOFitnessEvaluator();
    }

    public void addAll( Solution[] particles){
        this.leaders.clear();
        for( Solution particle : particles) {
            this.add( particle );
        }
    }

    public void addAll( List<Solution> leaders ){
        this.leaders.clear();
        for( Object leader : leaders ){
            Solution solution = (Solution) leader;
            this.add( solution );
        }
    }

    public void add( Solution particle){
        this.leaders.add( particle.copy() );
    }

    public void update(){
        this.evaluator.evaluate( this.leaders );
        Collections.sort( this.leaders, new Utils.ParticleComparator(false) );
        this.truncate();
    }

    private void truncate(){
        if( this.leaders.size() > this.leaderSize ){
            while( this.leaders.size() > this.leaderSize ){
                this.leaders.remove( this.leaders.size() -1 );
            }
        }
    }

    public int size(){ return this.leaders.size(); }
    public Solution get( int idx){ return this.leaders.get( idx % this.leaders.size() ); }

    public Solution[] toArray(){
        Solution[] ret = new Solution[this.leaders.size()];
        for(int i = 0; i < this.leaders.size(); i++){
            ret[i] = this.leaders.get( i );
        }
        return ret;
    }

    private class PSOFitnessEvaluator implements FitnessEvaluator {

        public PSOFitnessEvaluator() {
        }

        public void evaluate( ArrayList<Solution> solutions ){
            for(Solution solution: solutions){
                solution.setAttribute("fitness", solution.getObjective( 0 ));
            }
        }

        public void evaluate(Population population) {
            for(Solution solution : population) {
                solution.setAttribute("fitness", solution.getObjective( 0 ));
            }
        }

        public boolean areLargerValuesPreferred() {
            return false;
        }
    }
}
