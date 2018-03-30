import java.util.*;
import java.lang.*;
import java.io.*;
import java.math.*;
 
class SortedListNatural<T extends Comparable<? super T>> extends SortedList<T> {
  public SortedListNatural(){
    super(new Comparator<T>(){
      public int compare(T one, T two){
        return one.compareTo(two);
      }
    });
  }
}

public class SortedListTest {
  public static void main(String[] args) {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      SortedListNatural<Integer> list = new SortedListNatural<Integer>();

      //  Add some elements
      list.add(9);
      list.add(5);
      list.add(1);
      list.add(7);
      list.add(3);

      //  Find positions of elements if the elements were to placed in ascending order in some array
      System.out.println("Position of 1 is :" + list.findInOrderPosition(1));  //  Prints 0
      System.out.println("Position of 3 is :" + list.findInOrderPosition(3));  //  Prints 1
      System.out.println("Position of 5 is :" + list.findInOrderPosition(5));  //  Prints 2
      System.out.println("Position of 7 is :" + list.findInOrderPosition(7));  //  Prints 3
      System.out.println("Position of 9 is :" + list.findInOrderPosition(9));  //  Prints 4

      //  Find largest element strictly less than each of these inserted elements
      System.out.println("Largest element strictly less than 1 is :" + list.lower(1));  //  Throws NullPointerException as 1 itself is the minimum value
      System.out.println("Largest element strictly less than 3 is :" + list.lower(3));  //  Prints 1
      System.out.println("Largest element strictly less than 5 is :" + list.lower(5));  //  Prints 3
      System.out.println("Largest element strictly less than 7 is :" + list.lower(7));  //  Prints 5
      System.out.println("Largest element strictly less than 9 is :" + list.lower(9));  //  Prints 7
 
  }
}  