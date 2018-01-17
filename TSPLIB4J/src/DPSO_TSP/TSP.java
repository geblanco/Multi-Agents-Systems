package DPSO_TSP;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.tsplib.TSPInstance;
import org.moeaframework.problem.tsplib.TSPPanel;
import org.moeaframework.problem.tsplib.Tour;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static DPSO_TSP.Utils.toTour;

/**
 * Created by Guillermo Echegoyen Blanco on 2018.
 */
@SuppressWarnings( "unchecked" )
public class TSP {

    /**
     * The color for population members.
     */
    private static final Color lightGray = new Color(128, 128, 128, 64);
    private static boolean GUI = false;

    // Tunable parameters
    private static final String KEY_PROB_PATH = "problemPath";
    private static final String KEY_SWARM_SIZE = "swarmSize";
    private static final String KEY_LEADER_SIZE = "leaderSize";
    private static final String KEY_B_LOC = "bLoc";
    private static final String KEY_B_GLOB = "bGlob";

    // Fixed parameters
    private static final String KEY_N_ITER = "maxIter";
    private static final String KEY_N_RUNS = "maxRuns";

    // Local, per run metrics
    private static final String KEY_DIST_BEST = "currentBest";
    private static final String KEY_ITER_REACH = "iterReached";

    // Global, per problem metrics
    private static final String KEY_GLOB_DIST_BEST = "globDistBest";
    private static final String KEY_GLOB_ITER_REACH = "globIterReached";

    private static final String KEY_GLOB_DIST_WORST = "globDistWorst";

    private static final String KEY_GLOB_DIST = "globDist";
    private static final String KEY_GLOB_TIME = "globTime";

    private static class UI {

        private TSPInstance instance;
        private TSPPanel panel;
        private JFrame frame;
        private JTextArea progressText;
        private StringBuilder progress;

        public UI(TSPInstance instance) {
            this.instance = instance;
            this.constructUI( this.instance );
        }

        private void constructUI(TSPInstance instance){

            this.progress = new StringBuilder();
            this.progressText = new JTextArea();
            this.panel = new TSPPanel(instance);

            panel.setAutoRepaint(false);

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(panel);
            splitPane.setBottomComponent(new JScrollPane(progressText));
            splitPane.setDividerLocation(300);
            splitPane.setResizeWeight(1.0);

            // display the panel on a window
            this.frame = new JFrame(instance.getName());
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(splitPane, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        public TSPPanel getPanel(){ return panel; }

        public boolean stillVisible(){ return this.frame.isVisible(); }

        public void addText(String text){
            this.progress.insert( 0, text );
            this.progressText.setText( this.progress.toString() );
        }
    }

    private static void flushLocalValues(HashMap params){
        // Local keys to flush: KEY_DIST_BEST
        params.put( KEY_DIST_BEST, Double.MAX_VALUE );
    }

    private static HashMap setupHashMapInstance(HashMap params){
        // Setup on main
        // KEY_PROB_PATH
        // KEY_SWARM_SIZE
        // KEY_LEADER_SIZE
        // KEY_B_LOC
        // KEY_B_GLOB
        // Setup on testSuite
        // params.put(KEY_GLOB_DIST, new Statistics(nIter * nRuns));
        // params.put(KEY_GLOB_TIME, new Statistics(nIter * nRuns));

        params.put(KEY_DIST_BEST, Double.MAX_VALUE);
        params.put(KEY_ITER_REACH, 0.0);
        params.put(KEY_GLOB_DIST_BEST, Double.MAX_VALUE);
        params.put(KEY_GLOB_ITER_REACH, 0.0);
        params.put(KEY_GLOB_DIST_WORST, 0.0);

        return params;
    }

    private static void reportStats(TSPInstance instance, HashMap params, Solution solution, Statistics stats, int iteration) throws Exception{
        Tour best = toTour(solution);
        double dist = best.distance(instance);

        stats.add( dist );

        double currentBest = (Double) params.get(KEY_DIST_BEST);
        if( dist < currentBest ){
            params.put( KEY_DIST_BEST, dist );
            params.put( KEY_ITER_REACH, iteration );
            // System.out.println("Reached new LOCAL best distance\n  Prev: " + currentBest + " New: " + dist);

            double globalBest = (Double) params.get( KEY_GLOB_DIST_BEST );
            if( dist < globalBest ){
                params.put( KEY_GLOB_DIST_BEST, dist );
                params.put( KEY_GLOB_ITER_REACH, iteration );
                // System.out.println("Reached new GLOBAL best distance\n  Prev: " + globalBest + " New: " + dist);
            }
        }

        // If it is the last iteration, store the worst
        if( iteration == (Integer) params.get( KEY_N_ITER ) ){
            double worstDist = (Double) params.get( KEY_GLOB_DIST_WORST );

            if( worstDist < dist ){
                params.put( KEY_GLOB_DIST_WORST, dist );
                // System.out.println("Reached new GLOBAL worst distance\n  Prev: " + worstDist + " New: " + dist);
            }
        }
    }

    private static void testSuiteNoGUI( HashMap params ) throws Exception{

        TSPInstance instance = new TSPInstance( new File( (String) params.get(KEY_PROB_PATH) ) );

        int nRuns = (Integer) params.get( KEY_N_RUNS );
        int nIter = (Integer) params.get( KEY_N_ITER );

        Statistics timeStats = new Statistics( nRuns * nIter );
        Statistics distStats = new Statistics( nRuns * nIter );

        int swarmSize = (Integer) params.get(KEY_SWARM_SIZE);
        int leaderSize = (Integer) params.get(KEY_LEADER_SIZE);
        double bLoc = (Double) params.get(KEY_B_LOC);
        double bGlob = (Double) params.get(KEY_B_GLOB);

        for(int run = 0; run < nRuns; run++) {

            Problem problem = new TSPProblem( instance );
            DPSO algorithm = new DPSO( problem, swarmSize, leaderSize, bLoc, bGlob );


            double elapsed = System.currentTimeMillis();
            // now run the evolutionary algorithm
            for(int iteration = 0; iteration < nIter; iteration++){
                algorithm.step();
                iteration++;
                reportStats( instance, params, algorithm.getResult().get( 0 ), distStats, iteration + 1 );
            }

            timeStats.add( System.currentTimeMillis() - elapsed );
            flushLocalValues(params);
        }

        params.put( KEY_GLOB_DIST, distStats );
        params.put( KEY_GLOB_TIME, timeStats );
    }

    private static void runTestSuite(String[] args) throws Exception{
        if( args.length < 1 ){
            System.out.println("Usage test <tsp_file>");
            System.exit( 1 );
        }

        int[] swarmSize = new int[]{ 10, 20, 50, 100};
        int[] leaderSize = new int[]{ 2, 5, 10, 10};

        double[] bLoc = new double[]{ 0.3, 0.4, 0.5, 0.6, 0.7 };
        double[] bGlob = new double[]{ 0.7, 0.6, 0.5, 0.4, 0.3};

        int nIter = 50;
        int nRun = 10;

        HashMap params = new HashMap();
        params.put( KEY_N_ITER, nIter );
        params.put( KEY_N_RUNS, nRun );
        params.put(KEY_PROB_PATH, args[0]);

        for(int i = 0; i < swarmSize.length; i ++){
            for(int j = 0; j < bLoc.length; j ++ ) {
                params = setupHashMapInstance(params);
                params.put( KEY_SWARM_SIZE, swarmSize[ i ] );
                params.put( KEY_LEADER_SIZE, leaderSize[ i ] );
                params.put( KEY_B_LOC, bLoc[ j ] );
                params.put( KEY_B_GLOB, bGlob[ j ] );
                testSuiteNoGUI( params );
                print( params );
            }
        }
    }

    public static void print(HashMap params){
        Statistics distStats = (Statistics) params.get(KEY_GLOB_DIST);
        Statistics timeStats = (Statistics) params.get(KEY_GLOB_TIME);
        System.out.println("=======================\n" +
                "Problem: " + (String) params.get(KEY_PROB_PATH) + "\n" +
                "Swarm: " + (Integer) params.get( KEY_SWARM_SIZE ) + " Leader: " + (Integer) params.get(KEY_LEADER_SIZE) + "\n" +
                "bLoc: " + (Double) params.get(KEY_B_LOC) + " bGlob: " + (Double) params.get(KEY_B_GLOB) + "\n" +
                "Global best: " + (Double) params.get(KEY_GLOB_DIST_BEST) + " reached at iteration: " + (Integer) params.get(KEY_GLOB_ITER_REACH) + "\n" +
                "Global worst: " + (Double) params.get(KEY_GLOB_DIST_WORST) + "\n" +
                "Global mean distance: " + distStats.mean() + " stddev distance: " + distStats.stddev() + "\n" +
                "Global mean time (per iteration in ms): " + timeStats.mean() + " stddev time: " + timeStats.stddev()
        );
    }

    public static void testWithUI(String path) throws IOException{
        TSPInstance instance = new TSPInstance( new File(path) );

        Problem problem = new TSPProblem( instance );
        DPSO algorithm = new DPSO( problem, 100, 10, 0.4, 0.6 );

        UI ui = new UI( instance );

        for(int iteration = 0; iteration < 1000; iteration++) {

            algorithm.step();
            iteration++;

            ui.getPanel().clearTours();

            // display population with light gray lines
            for( Solution solution : algorithm.getBest() ) {
                ui.getPanel().displayTour( toTour( solution ), lightGray );
            }

            Tour best = toTour( algorithm.getResult().get( 0 ) );
            ui.getPanel().displayTour( best, Color.RED, new BasicStroke( 2.0f ) );
            ui.addText( "Iteration " + iteration + ": " + best.distance( instance ) + "\n" );
            ui.getPanel().repaint();
        }
    }

    public static void main(String[] args) throws Exception{
        // testWithUI( args[0] );
        runTestSuite( args );
    }

}

