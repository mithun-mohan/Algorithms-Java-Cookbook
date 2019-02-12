/*
  Problem Statement: You are given a string of only binary characters(i.e. for example 1010) of length N. You have to perform following operations on the given string M times;
  i) Replace all 1's with 10, and
  ii) Replace all 0's with 01.

  Now you will be asked what is the character at any given position(say P) in the resultant string.

  Solution Complexity: max(log(P), M) where P = position to find and M = number or times to do the operation
*/

import java.util.*;
import java.lang.*;
import java.io.*;
import java.math.*;
 
import java.util.*;
import java.lang.*;
import java.io.*;
 
@SuppressWarnings("unchecked")
public class Problem1 implements Runnable {

  static BufferedReader in;
  static PrintWriter out;
 
  public static void main(String[] args) {
      new Thread(null, new Problem1(), "whatever", 1<<29).start();
  }
 
  public void run() {
    in = new BufferedReader(new InputStreamReader(System.in));
    out = new PrintWriter(System.out, false);
 
    try
    {
      // in = new BufferedReader(new FileReader("A-large (1).in"));
      // out = new PrintWriter("output.txt");

      System.out.print("Enter a binary string : ");
      String text = in.readLine().trim();
      int textLength = text.length();

      System.out.print("Enter the number of times to do the operation(i.e. replace 1 with 10 and 0 with 01) : ");
      int times = Integer.parseInt(in.readLine().trim());

      System.out.print("Enter the position in the resultant string you want to query : ");
      long positionInResultantString = Long.parseLong(in.readLine().trim()) - 1;

      List<Long> pathToResultantPosition = new ArrayList<>();
      long curPosition = positionInResultantString;

      for(int i = 0; i <= times; i++) {
        pathToResultantPosition.add(curPosition);
        curPosition /= 2;
      }

      long position = pathToResultantPosition.get(pathToResultantPosition.size() - 1);
      int bit = text.charAt((int)position) - 48;
      for(int i = pathToResultantPosition.size() - 2; i >= 0; i--) {
        position = pathToResultantPosition.get(i);
        bit = (position%2 == 0L) ? bit : (1-bit);
      }

      System.out.println(String.format("Character at position %s in resultant string is : %s", positionInResultantString + 1, bit));

      out.flush();
      out.close();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}