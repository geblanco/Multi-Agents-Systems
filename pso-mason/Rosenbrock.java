/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

public class Rosenbrock implements Evaluatable 
{
  /* 
     http://en.wikipedia.org/wiki/Rosenbrock_function

     Number of variables: 2 variables.

     Definition: f(x,y) = (1-x)^2 + 100(y - x^2)^2

     Search domain: −5 ≤ x_i ≤ 10, i = 1, 2, ... , n

     Number of local minima: several local minima.

     The global minima: x,y =  (1, 1), f(x,y) = 0.
     */

  public double calcFunction(double x, double y) 
  {
    double expr1 = 1 - x;
    double expr2 = (y - x*x);
    return (PSO.FUNCTION_SCALE - (expr1*expr1 + (100 * expr2*expr2))); 
  }
}
