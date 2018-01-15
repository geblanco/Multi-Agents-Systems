/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

public class Rastrigin implements Evaluatable 
{
  /*
    http://en.wikipedia.org/wiki/Rastrigin_function

    Number of variables: n variables.

    Definition: f(x) = A*n + Sum_i=0^n(x_i^2 - A*cos(2*\pi*x_i))

    Search domain: −5.12 ≤ x_i ≤ 5.12, i = 1, 2, . . . , n.

    Number of local minima: several local minima.

    The global minima: x* =  (0, ... , 0), f(x*) = 0.
    */

  public double calcFunction(double x, double y) 
  {
    return (PSO.FUNCTION_SCALE - (20 + (x*x - 10*Math.cos(2*Math.PI*x)) + 
		         (y*y - 10*Math.cos(2*Math.PI*y))));
  }
}
