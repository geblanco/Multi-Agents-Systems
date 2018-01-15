/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class GlobalMin
{       
  static final public long serialVersionUID = 15L;
  MutableDouble2D position = new MutableDouble2D();

  public GlobalMin(double x, double y, PSO p)
  {
    super();
    position.setTo(x, y);
    p.space.setObjectLocation(this,new Double2D(position));
  }
}
