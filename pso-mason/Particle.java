/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class Particle
{       
  static final public long serialVersionUID = 15L;
  double personalBestVal = 0;     
  MutableDouble2D personalBestPosition = new MutableDouble2D();

  MutableDouble2D position = new MutableDouble2D();
  MutableDouble2D velocity = new MutableDouble2D();       

  PSO pso;
  Evaluatable function;
  int index;  

  public Particle() 
  {
    super();
  }

  public Particle(double x, double y, double vx, double vy, PSO p, 
		  Evaluatable f, int i)
  {
    super();

    position.setTo(x, y);
    velocity.setTo(vx, vy);

    pso = p;
    function = f;
    pso.space.setObjectLocation(this,new Double2D(position));
    index = i;
  }

  public void updateGlobalBest(double currVal, double currX, double currY)
  {
    if (currVal > personalBestVal)
    {
      personalBestVal = currVal;
      personalBestPosition.setTo(currX, currY);
      pso.updateGlobalBest(currVal, currX, currY);
    }
  }

  public double getFitness()
  {
    return function.calcFunction(position.x,position.y);
  }

  public void stepUpdateFitness()
  {
    updateGlobalBest(getFitness(), position.x, position.y);
  }

  public void stepUpdateVelocity()
  {
    double x = position.x;
    double y = position.y;

    // updates the location of neighborsBestPos
    MutableDouble2D neighborsBestPos = new MutableDouble2D(); 
    pso.getNeighborhoodBest(index, neighborsBestPos);       

    // calc x component
    double inertia = pso.getInertiaConstant() * velocity.x;
    double cognitiveComp = pso.getCognitiveConstant()*(personalBestPosition.x - x);
    double socialComp;
    if (pso.getIsGlobalBest())
      socialComp = pso.getSocialConstant()*(pso.globalBestPosition.x - x);
    else
      socialComp = pso.getSocialConstant()*(neighborsBestPos.x - x);
    double vx = (inertia + pso.random.nextDouble()*cognitiveComp + 
		 pso.random.nextDouble()*socialComp);
    vx = Math.max(vx,pso.getMaxVelocity()*-1);
    vx = Math.min(vx,pso.getMaxVelocity()); 

    // calc y component
    inertia = pso.getInertiaConstant() * velocity.y;
    cognitiveComp = pso.getCognitiveConstant()*(personalBestPosition.y - y);
    if (pso.getIsGlobalBest())
      socialComp = pso.getSocialConstant()*(pso.globalBestPosition.y - y);
    else
      socialComp = pso.getSocialConstant()*(neighborsBestPos.y - y);
    double vy = (inertia + pso.random.nextDouble()*cognitiveComp + 
		 pso.random.nextDouble()*socialComp);
    vy = Math.max(vy,pso.getMaxVelocity()*-1);
    vy = Math.min(vy,pso.getMaxVelocity()); 

    // update velocity
    velocity.setTo(vx, vy);         
  }

  public void stepUpdatePosition()
  {
    position.addIn(velocity);
    pso.space.setObjectLocation(this, new Double2D(position));
  }
}
