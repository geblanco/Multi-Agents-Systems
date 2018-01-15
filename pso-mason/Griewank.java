/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

public class Griewank implements Evaluatable 
{
  /* 
    http://mathworld.wolfram.com/GriewankFunction.html

     Definition: f(x_1,...,x_n) = 1 + (1/4000) * Sum_i=1^n (x_i^2) -
                                      Product_i=1^n cos(x_i/sqrt(i))

     Search domain: −600 ≤ x_i ≤ 600, i = 1, 2, . . . , n.

     Number of local minima: several local minima.

     The global minima: x^* =  (0, ... , 0), f(x^*) = 0.
   */

  public double calcFunction(double x, double y) 
  {
    return (PSO.FUNCTION_SCALE - (1 + ((1/4000)*((x*x) + (y*y))) - 
		     (Math.cos(x) * 
		      Math.cos(y/Math.sqrt(2)))));
  }
}
