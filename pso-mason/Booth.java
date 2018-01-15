/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

  Modified 2012 by Nik Swoboda, Universidad Politecnica de Madrid
*/

public class Booth implements Evaluatable 
{
  /* 
     http://www-optima.amp.i.kyoto-u.ac.jp/member/student/hedar/Hedar_files/TestGO_files/Page816.htm

      Number of variables: n = 2

      Definition: f(x^*) = (x_1 + 2*x_2 - 7)^2 + (2*x_1 + x_2 - 5)^2

      Search domain: −10 ≤ x_i ≤ 10, i = 1, 2.

      Number of local minima: several local minima.

      The global minimum: x^* = (1, 3), f(x^*) = 0.
   */

  public double calcFunction(double x, double y) 
  {
    return (PSO.FUNCTION_SCALE - ((x + 2*y - 7) * (x + 2*y - 7) + 
		    (2*x + y - 5) * (2*x + y - 5)));
  }
}
