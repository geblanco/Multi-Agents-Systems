/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.MutableDouble2D;

public class PSO extends SimState 
{       
  public static final int FUNCTION_SCALE = 1000;
  public static final int FUNCTION_LOWERBOUND = 0;
  public static final int FUNCTION_XMIN = 1;
  public static final int FUNCTION_YMIN = 2;
  public static final int FUNCTION_GLOBALMIN = 3;

  public Continuous2D space;

  public double width = 10.24;
  public double height = 10.24;
  public Particle[] particles;
  public double globalBestVal = 0;
  MutableDouble2D globalBestPosition = new MutableDouble2D();

  // Console variables
  private int _fNumParticles = 10;
  public int getNumParticles() 
  { 
    return _fNumParticles; 
  }
  public void setNumParticles(int val) 
  { 
    if (val >= 0) 
      _fNumParticles = val; 
  }

  private boolean _fIsGlobalBest = false;
  public boolean getIsGlobalBest()
  {
    return _fIsGlobalBest;
  }
  public void setIsGlobalBest(boolean val)
  {
    _fIsGlobalBest = val;
  }

  private int _fNeighborhoodSize = 2;
  public int getNeighborhoodSize() 
  { 
    return _fNeighborhoodSize; 
  }
  public void setNeighborhoodSize(int val) 
  { 
    if ((val >= 0) && (val <= _fNumParticles)) 
      _fNeighborhoodSize = val; 
  }

  private double _fMaxVelocity = 1.0;
  public double getMaxVelocity() 
  { 
    return _fMaxVelocity; 
  }
  public void setMaxVelocity(double val) 
  { 
    if (val >= 0) 
      _fMaxVelocity = val; 
  }

  private double _fInertiaConstant = 1.0;
  public double getInertiaConstant()
  {
    return _fInertiaConstant;
  }
  public void setInertiaConstant(double val)
  {
    if (val >= 0) 
      _fInertiaConstant = val;
  }

  private double _fCognitiveConstant = 2.0;
  public double getCognitiveConstant()
  {
      return _fCognitiveConstant;
  }
  public void setCognitiveConstant(double val)
  {
    if (val >= 0) 
      _fCognitiveConstant = val;
  }

  private double _fSocialConstant = 2.0;
  public double getSocialConstant()
  {
    return _fSocialConstant;
  }
  public void setSocialConstant(double val)
  {
    if (val >= 0) 
      _fSocialConstant = val;
  }

  private int _fFunction = 0;
  public int getFunction() 
  { 
    return _fFunction; 
  }
  public void setFunction(int val) 
  { 
    _fFunction = val; 
  }
  public Object domFunction() 
  { 
    return new String[] { "Booth", "Rastrigin", "Griewank", "Rosenbrock" };
  }

  private Evaluatable mapFunction(int val)
  {
    switch (val)
    {
      case 0: return new Booth();
      case 1: return new Rastrigin();
      case 2: return new Griewank();
      case 3: return new Rosenbrock();
    }
    return new Booth();
  }

  public double[][] functionProperties = 
  {
    {920,1,3,0}, // lowerBound, x_globalMin, y_globalMin, f(x,y)_globalMin
    {950,0,0,0},
    {998,0,0,0},
    {200,1,1,0}
  };

  private double _fSuccessThreshold = 1.0e-6;
  public double getSuccessThreshold() 
  { 
    return _fSuccessThreshold; 
  }
  public void setSuccessThreshold(double val) 
  { 
    if (val >= 0) 
      _fSuccessThreshold = val; 
  }

  public PSO(long seed)
  {
    super(seed);
  }

  public void updateGlobalBest(double currVal, double currX, double currY)
  {
    if (currVal > globalBestVal)
    {
      globalBestVal = currVal;
      globalBestPosition.setTo(currX, currY);
    }               
  }

  public double getNeighborhoodBest(int index, MutableDouble2D pos)
  {
    double bv = Double.NEGATIVE_INFINITY;
    Particle p;     

    for (int i = 0; i < _fNeighborhoodSize; i++)
    {
      p = particles[(index + i) % _fNumParticles];
      if (p.personalBestVal > bv)
      {
	bv = p.personalBestVal;
	pos.setTo(p.personalBestPosition);
      }
    }
    return bv;             
  }

  public void start()
  {
    // reset the global best
    globalBestVal = 0;

    super.start();
    particles = new Particle[_fNumParticles];
    space = new Continuous2D(height, width, height);
    Evaluatable f = mapFunction(_fFunction);            

    GlobalMin gm = new GlobalMin(functionProperties[_fFunction]
				                   [FUNCTION_XMIN],
				 functionProperties[_fFunction]
				                   [FUNCTION_YMIN],this);

    for (int i = 0; i < _fNumParticles; i++)
    {
      double x = (random.nextDouble() * width) - (width * 0.5);
      double y = (random.nextDouble() * height) - (height * 0.5);
      double vx = (random.nextDouble() * _fMaxVelocity) - 
	          (_fMaxVelocity * 0.5);
      double vy = (random.nextDouble() * _fMaxVelocity) - 
	          (_fMaxVelocity * 0.5);

      final Particle p = new Particle(x, y, vx, vy, this, f, i);
      particles[i] = p;

      schedule.scheduleRepeating(Schedule.EPOCH,1,new Steppable()
        {
          public void step(SimState state) { p.stepUpdateFitness(); }
	});

      schedule.scheduleRepeating(Schedule.EPOCH,2,new Steppable()
	{
	  public void step(SimState state) { p.stepUpdateVelocity(); }
	});

      schedule.scheduleRepeating(Schedule.EPOCH,3,new Steppable()
        {
	  public void step(SimState state) { p.stepUpdatePosition(); }
        });
    }

    schedule.scheduleRepeating(Schedule.EPOCH, 4, new Steppable()
     {
       public void step(SimState state)
       {
         int count = 0;
         for (int i = 0; i < space.allObjects.numObjs; i++)
         {
           Object o = space.allObjects.get(i);

	   if (o instanceof Particle)
	   {
	     Particle p = (Particle)o;
	     if (Math.abs(p.getFitness() - FUNCTION_SCALE - 
			  functionProperties[_fFunction]
			                    [FUNCTION_GLOBALMIN]) 
		 <= _fSuccessThreshold)
	       count++;
	   }
         }
	 if (count == _fNumParticles)
	   state.kill();
       }
     });             
  }

  public static void main(String[] args) 
  {
    doLoop(PSO.class, args);
    System.exit(0);
  }
}
