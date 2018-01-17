package DPSO_TSP;

/**
 * Created by Guillermo Echegoyen Blanco on 2016.
 */
public class Statistics {

    private double[] store;
    private double acc;
    private int pointer;

    public Statistics(int capacity){
        this.store = new double[capacity];
        this.acc = 0;
        this.pointer = 0;
    }

    public double mean(){
        return this.acc / this.pointer;
    }

    public double stddev(){
        double currMean = this.mean();
        double currStd = 0.0;

        for(double value : this.store){
            currStd += (value - currMean) * (value - currMean);
        }

        return Math.sqrt( currStd / (this.pointer - 1) );
    }

    public void add(double value) throws Exception{
        if( this.store.length == this.pointer ){
            throw new Exception( "Statistics capacity exceeded" );
        }
        this.store[pointer++] = value;
        this.acc += value;
    }
}
