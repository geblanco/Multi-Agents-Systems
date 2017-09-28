import sim.engine.*;
import sim.field.grid.*;

public class GameOfLife extends SimState implements Steppable
{
  public int gridWidth = 400;
  public int gridHeight = 200;
  public IntGrid2D grid = null;

  public GameOfLife(long seed)
  {
    super(seed);
  }
  private void seedGrid()
  {
    for(int i = 0; i < gridWidth; i++){
      for(int j = 0; j < gridHeight; j++){
        grid.set(i, j, random.nextInt(2));
      }
    }
  }
  private int getAliveNeighborsCount(int x, int y, IntGrid2D currGrid){
    int lowXIndex = x <= 0 ? 0 : x -1;
    int lowYIndex = y <= 0 ? 0 : y -1;
    int hiXIndex = x == gridWidth -1 ? x : x +1;
    int hiYIndex = y == gridHeight -1 ? y : y +1;
    int aliveCount = 0;

    for(int i = lowXIndex; i <= hiXIndex; i++){
      for(int j = lowYIndex; j <= hiYIndex; j++){
        aliveCount += currGrid.get(i, j);
      }
    }
    return Math.max(aliveCount - currGrid.get(x, y), 0);
  } 
  private int getNextCellState(int x, int y, IntGrid2D currGrid)
  {
    int ret = 0;
    int aliveNeigbors = getAliveNeighborsCount(x, y, currGrid);
    if( currGrid.get(x, y) == 0 ){
      // If dead and alive neighbors >=3 
      // : cell becomes alive
      if( aliveNeigbors == 3 ){
        ret = 1;
      }
    }else{
      // If alive neighbors < 2 or > 3
      // : cell dies
      if( 2 == aliveNeigbors || aliveNeigbors == 3 ){
        ret = 1;
      }
    }
    return ret;
  }
  public void start()
  {
    super.start();
    grid = new IntGrid2D(gridWidth, gridHeight);

    seedGrid();
    schedule.scheduleRepeating(this);
  }
  @Override
  public void step(SimState state)
  {
    IntGrid2D tmp = new IntGrid2D(grid);
    for(int i = 0; i < gridWidth; i++){
      for(int j = 0; j < gridHeight; j++){
        grid.set(i, j, getNextCellState(i, j, tmp));
      }
    }
  }
  public void printGrid(){
    for(int i = 0; i < gridWidth; i++){
      for(int j = 0; j < gridHeight; j++){
        System.out.print(grid.get(i, j) + " ");
      }
      System.out.println();
    }
    System.out.println();
  }
  public static void main(String[] args)
  {
    GameOfLife gol = new GameOfLife(System.currentTimeMillis());
    gol.start();
    long steps = 0;
    gol.printGrid();
    for(long steps = 0; steps < 1000 && gol.schedule.step(gol);){
      steps = gol.schedule.getSteps();
    }
    gol.printGrid();
    gol.finish();
    System.exit(0);
  }
}
