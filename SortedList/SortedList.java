//  Base code taken from http://blog.scottlogic.com/2010/12/22/sorted_lists_in_java.html
//  Supporting two additional operations that may be common on Balanced BSTs,
//    1) lower - Finding the largest element strictly less than given element.
//    2) findInOrderPosition - Finding the position of the element if all elements 
//                             were to be arranged in increasing order in an array.

//  SortedList based on AVL Tree
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
 
public class SortedList<T> extends AbstractList<T> implements Serializable {
 
  private Node root;
  private final Comparator<? super T> comparator;
 
    /**
     * Constructs a new, empty SortedList which sorts the elements
     * according to the given {@code Comparator}.
     * 
     * @param comparator the {@code Comparator} to sort the elements by.
     */
    public SortedList(Comparator<? super T> comparator){
      this.comparator = comparator;
    }
    
    /**
     * Inserts the given object into this {code SortedList} at the appropriate
     * location, so as to ensure that the elements in the list are kept in
     * the order specified by the given {@code Comparator}.
     * <p>
     * This method only allows non-<code>null</code> values to be added, if the given
     * object is <code>null</code>, the list remains unaltered and false returned.
     * 
     * @param object the object to add.
     * @return false when the given object is null and true otherwise.
     */
    @Override
    public boolean add(T object){
        boolean treeAltered = false;
        if(object != null){
            //wrap the value in a node and add it..
            add(new Node(object)); //will ensure the modcount is increased..
            treeAltered = true;
        }
        return treeAltered;
    }
    
    /**
     * Add the given Node to this {@code SortedList}.
     * <p>
     * This method can be overridden by a subclass in order to change the definition of the {@code Node}s
     * that this List will store.
     * <p>
     * This implementation uses the {@code Node#compareTo(Node)} method in order to ascertain where the
     * given {@code Node} should be stored. It also increments the modCount for this list.
     *
     * @param toAdd the {@code Node} to add.
     */
    protected void add(Node toAdd){
      if(root == null){ //simple case first..
        root = toAdd;
 
      } else { //non-null root case..
          Node current = root;
          while(current != null) { //should always break!
              int comparison = toAdd.compareTo(current);
   
              if(comparison < 0){ //toAdd < node
                  if(current.leftChild == null){ 
                      current.setLeftChild(toAdd);
                      break;
                 } else {
                     current = current.leftChild;
                 }
              } else { //toAdd > node (equal should not be possible)
                  if(current.rightChild == null){
                      current.setRightChild(toAdd);
                      break;
                  } else {
                      current = current.rightChild;
                  }
              }
          }
      }
        modCount++; //see AbstractList#modCount, incrementing this allows for iterators to be fail-fast..
    }
    
    /**
     * Returns the number of elements in this {@code SortedList}.
     * 
     * @return the number of elements stored in this {@code SortedList}. 
     */
    @Override
    public int size(){
        return (root == null) ? 0 : 1 + root.numChildren;
    }
 
    /**
     * Returns the root node of this {@code SortedList}, which is
     * <code>null</code> in the case that this list is empty.
     *
     * @return the root node of this {@code SortedList}, which is
     *         <code>null</code> in the case that this list is empty.
     */
    protected Node getRoot(){
      return root;
    }
    
    /**
     * Returns whether or not the given object is present in this {@code SortedList}.
     * The comparison check uses the {@code Object#equals(Object)} method and work
     * under the assumption that the given <code>obj</code> must have type <code>T</code>
     * to be equal to the elements in this {@code SortedList}.  Works in time
     * <i>O(log(n))</i>, where <i>n</i> is the number of elements in the list.
     *
     * @param obj the object to check for.
     * @return true if the given object is present in this {code SortedList}.
     */
  @SuppressWarnings("unchecked")
  @Override
    public boolean contains(Object obj){
      return obj != null
          && !isEmpty()
          && findFirstNodeWithValue((T) obj) != null;
    }
 
    /**
     * Returns the node representing the given value in the tree, which can be null if
     * no such node exists.
     * <p>
     * This method performs a binary search using the given comparator, and hence works in time
     * O(log(n)). 
     *
     * @param value the value to search for.
     * @return the first node in this list with the given value.
     */
    protected Node findFirstNodeWithValue(T value){
        Node current = root;
        while(current != null){
          //use the comparator on the values, rather than nodes..
          int comparison = comparator.compare(current.value, value);
            if(comparison == 0){
               //find the first such node..
               while(current.leftChild != null
                   && comparator.compare(current.leftChild.value, value) == 0){
                 current = current.leftChild;
               }
               break;
            } else if(comparison < 0){ //need to go right..
                current = current.rightChild;
            } else {
                current = current.leftChild;
            }
        }
        return current;
    }
 
    /**
     * Removes the first element in the list with the given value, if such
     * a node exists, otherwise does nothing.  Comparisons
     * on elements are done using the given comparator.
     * <p>
     * Returns whether or not a matching element was found and removed or not.
     *
     * @param value the object to remove from this {@code SortedList}.
     * @return <code>true</code> if the given object was found in this
     *         {@code SortedList} and removed, <code>false</code> otherwise.
     */
    @Override
    public boolean remove(Object value){
        boolean treeAltered = false;
        try {
          if(value != null && root != null){
              @SuppressWarnings("unchecked")
        Node toRemove = findFirstNodeWithValue((T) value);
              if(toRemove != null){
                remove(toRemove);
                treeAltered = true;
              }
          }
        } catch(ClassCastException e){
          //comparator may throw this error, don't need to do anything.. 
        }
        return treeAltered;
    }
    
    /**
     * Removes the given {@code Node} from this {@code SortedList}, re-balancing if required,
     * adds to <code>modCount</code> too.
     * <p>
     * Operates in time <i>O(log(n))</i>, where <i>n</i> is the number of elements in the list.
     *
     * @param toRemove the {@code Node}, which must be a {@code Node} in this {@code SortedList}.
     */
    protected void remove(Node toRemove){
        if(toRemove.isLeaf()){
            Node parent = toRemove.parent;
            if(parent == null){ //case where there is only one element in the list..
                root = null;
            } else {
                toRemove.detachFromParentIfLeaf();
            }
        } else if(toRemove.hasTwoChildren()){ //interesting case..
            Node successor = toRemove.successor(); //will not be a non-null leaf or has one child!!
 
            //switch the values of the nodes over, then delete the switched node..
            toRemove.switchValuesForThoseIn(successor);
            remove(successor); //will be one of the simpler cases.
 
        } else if(toRemove.leftChild != null){
            toRemove.leftChild.contractParent();
        } else { //leftChild is null but right isn't..
            toRemove.rightChild.contractParent();
        }
        modCount++; //see AbstractList#modCount, incrementing this allows for iterators to be fail-fast..
    }
    
    /**
     * Returns the element at the given index in this {@code SortedList}.  Since the list is sorted,
     * this is the "index"th smallest element, counting from 0-<i>n</i>-1.
     * <p>
     * For example, calling {@code get(0)} will return the smallest element in the list.
     *
     * @param index the index of the element to get.
     * @return the element at the given index in this {@code SortedList}.
     * @throws IllegalArgumentException in the case that the index is not a valid index.
     */
    @Override
    public T get(int index){
      return findNodeAtIndex(index).value;
    }
 
    /**
     * Returns the {@code Node} at the given index.
     *
     * @param index the index to search for.
     * @return the {@code Node} object at the specified index.
     * 
     * @throws IllegalArgumentException in the case that the the index is not valid.
     */
    protected Node findNodeAtIndex(int index){
      if(index < 0 || index >= size()){ 
            throw new IllegalArgumentException(index + " is not valid index.");
        }
      Node current = root;
        //the the number of smaller elements of the current node as we traverse the tree..
        int totalSmallerElements = (current.leftChild == null) ? 0 : current.leftChild.sizeOfSubTree();
        while(current!= null){  //should always break, due to constraint above..
            if(totalSmallerElements == index){
                break;
            }
            if(totalSmallerElements > index){ //go left..
                current = current.leftChild;
                totalSmallerElements--;
                totalSmallerElements -= (current.rightChild == null) ? 0 : current.rightChild.sizeOfSubTree();
            } else { //go right.. 
                totalSmallerElements++;
                current = current.rightChild;
                totalSmallerElements += (current.leftChild == null) ? 0 : current.leftChild.sizeOfSubTree();
            }
        }
        return current;
    }
 
    /**
     * Returns the largest element strictly less than given element.
     *
     * @param element to search for.
     * @return largest element strictly less than given element.
     * 
     * @throws NullPointerException in the case there is no element less than given element.
     */
    public T lower(T value) {
        Node p = root;
        while (p != null) {
            int cmp = comparator.compare(value, p.value);
            if (cmp > 0) {
                if (p.rightChild != null)
                    p = p.rightChild;
                else
                    return p.value;
            } else {
                if (p.leftChild != null) {
                    p = p.leftChild;
                } else {
                    Node parent = p.parent;
                    Node ch = p;
                    while (parent != null && ch == parent.leftChild) {
                        ch = parent;
                    parent = parent.parent;
                    }
                    return parent.value;
                }
            }
        }
        return null;
    }
 
    /**
     * Returns the position of the element in Inorder traversal(i.e. ascending order) of the Balanced BST.
     *
     * @param element to search for.
     * @return position of the element in Inorder traversal.
     */
    public int findInOrderPosition(T value){
        Node current = root;
        int pos = 0;
 
        while(current != null){
          //use the comparator on the values, rather than nodes..
          int comparison = comparator.compare(current.value, value);
            if(comparison == 0){
              pos += 1;
              if(current.leftChild != null)
                pos += current.leftChild.sizeOfSubTree();
               //find the first such node..
               while(current.leftChild != null
                   && comparator.compare(current.leftChild.value, value) == 0){
                 current = current.leftChild;
               }
               break;
            } else if(comparison < 0){ //need to go right..
                pos += 1;
                if(current.leftChild != null)
                  pos += current.leftChild.sizeOfSubTree();
                current = current.rightChild;
            } else {
                current = current.leftChild;
            }
        }
        return pos - 1;
    }
 
    /**
     * Returns whether or not the list contains any elements.
     * 
     * @return {@code true} if the list has no element in it
     *         and {@code false} otherwise.
     */
    @Override
    public boolean isEmpty(){
        return root == null;
    }
    
    /**
     * Removes all elements from the list, leaving it empty.
     */
    @Override
    public void clear(){
        root = null; //TF4GC.
    }
   
   /**
    * Returns the smallest balance factor across the entire list, this serves not other
    * purpose other than for testing.
    * 
    * @return the minimum of all balance factors for nodes in this tree, or 0 if this tree is empty.
    */
   int minBalanceFactor(){
       int minBalanceFactor = 0;
       Node current = root;
       while(current != null){
           minBalanceFactor = Math.min(current.getBalanceFactor(), minBalanceFactor);
           current = current.successor();
       }
       return minBalanceFactor; 
   }
   
   /**
    * Returns the largest balance factor across the entire list, this serves not other
    * purpose other than for testing.
    * 
    * @return the maximum of all balance factors for nodes in this tree, or 0 if this tree is empty.
    */
   int maxBalanceFactor(){
       int maxBalanceFactor = 0;
       Node current = root;
       while(current != null){
           maxBalanceFactor = Math.max(current.getBalanceFactor(), maxBalanceFactor);
           current = current.successor();
       }
       return maxBalanceFactor; 
   }
   
   //Implementation of the AVL tree rebalancing starting at the startNode and working up the tree...
   private void rebalanceTree(Node startNode){
       Node current = startNode;
       while(current!= null){
           //get the difference between the left and right subtrees at this point..
           int balanceFactor = current.getBalanceFactor();
           
           if(balanceFactor == -2){ //the right side is higher than the left.
               if(current.rightChild.getBalanceFactor() == 1){ //need to do a double rotation..
                   current.rightChild.leftChild.rightRotateAsPivot();
               }
               current.rightChild.leftRotateAsPivot();
    
           } else if(balanceFactor == 2){ //left side higher than the right.
               if(current.leftChild.getBalanceFactor() == -1){ //need to do a double rotation..
                   current.leftChild.rightChild.leftRotateAsPivot();
               }
               current.leftChild.rightRotateAsPivot();
           }
 
           if(current.parent == null){ //the root may have changed so this needs to be updated..
               root = current;
               break;
           } else {
               //make the request go up the tree..
               current = current.parent;
           }
       }
    }
   
   /**
    * Inner class used to represent positions in the tree. Each node stores a list of equal values,
    * is aware of their children and parent nodes, the height of the subtree rooted at that point and
    * the total number of children elements they have.
    *
    * @param T the value the node will store.
    */
   protected class Node implements Comparable<Node> {
    
     private T value; //the data value being stored at this node 
      
     private Node leftChild;
       private Node rightChild;
       private Node parent;
 
       //The "cached" values used to speed up methods..
       private int height;
       private int numChildren; 
       
       /**
        * Constructs a new Node which initially just stores the given value.
        *
        * @param t the value which this node will store.
        */
       protected Node(T t){
           this.value = t;
       }
 
       /**
        * Returns whether or not this {@code Node} has two children.
        *
        * @return <code>true</code> if this node has two children and <code>false</code> otherwise.
        */
       protected boolean hasTwoChildren(){
           return leftChild != null && rightChild != null;
       }
       
       //Removes this node if it's a leaf node and updates the number of children and heights in the tree.. 
       private void detachFromParentIfLeaf(){
           if(!isLeaf() || parent == null){
               throw new RuntimeException("Call made to detachFromParentIfLeaf, but this is not a leaf node with a parent!");
           }
           if(isLeftChildOfParent()){
               parent.setLeftChild(null);
           } else {
               parent.setRightChild(null);
           }
       }
 
       /**
        * Returns the grand parent {@code Node} of this {@code Node}, which may be <code>null</code>.
        * 
        * @return the grand parent of this node if there is one and <code>null</code> otherwise.
        */
       protected Node getGrandParent(){
           return (parent != null && parent.parent != null) ? parent.parent : null;
       }
 
       //Moves this node up the tree one notch, updates values and rebalancing the tree..
       private void contractParent(){
           if(parent == null || parent.hasTwoChildren()){
               throw new RuntimeException("Can not call contractParent on root node or when the parent has two children!");
           }
           Node grandParent = getGrandParent();
           if(grandParent != null){
               if(isLeftChildOfParent()){
                   if(parent.isLeftChildOfParent()){
                       grandParent.leftChild = this;
                   } else {
                       grandParent.rightChild = this;
                   }
                   parent = grandParent;
               } else {
                   if(parent.isLeftChildOfParent()){
                       grandParent.leftChild = this;
                   } else {
                       grandParent.rightChild = this;
                   }
                   parent = grandParent;
               }
           } else { //no grandparent..
               parent = null;
               root = this; //update root in case it's not done elsewhere..
           }
           
           //finally clean up by updating values and rebalancing..
           updateCachedValues();
           rebalanceTree(this);
       }
       
       /**
        * Returns whether or not this not is the left child of its parent node; if this is the
        * root node, then <code>false</code> is returned.
        * 
        * @return <code>true</code> if this is the left child of its parent node
        *         and <code>false</code> otherwise.
        */
       public boolean isLeftChildOfParent(){
           return parent != null && parent.leftChild == this;
       }
 
       /**
        * Returns whether or not this not is the right child of its parent node; if this is the
        * root node, then <code>false</code> is returned.
        * 
        * @return <code>true</code> if this is the right child of its parent node
        *         and <code>false</code> otherwise.
        */
       public boolean isRightChildOfParent(){
           return parent != null && parent.rightChild == this;
       }
 
       /**
        * Returns the left child of this {@code Node}, which may be <code>null</code>.
        * 
        * @return the left child of this {@code Node}, which may be <code>null</code>.
        */
       protected Node getLeftChild(){
         return leftChild;
       }
       
       /**
        * Returns the right child of this {@code Node}, which may be be <code>null</code>.
        * 
        * @return the right child of this node, which may be <code>null</code>.
        */
       protected Node getRightChild(){
         return rightChild;
       }
       
       /**
        * Returns the parent {@code Node} of this node, which will be <code>null</code>
        * in the case that this is the root {@code Node}.
        * 
        * @return the parent node of this one.
        */
       protected Node getParent(){
         return parent;
       }
       
       /**
        * Compares the value stored at this node with the value at the given node using
        * the comparator, if these values are equal it compares the nodes on their IDs;
        * older nodes considered to be smaller.
        * 
        * @return if the comparator returns a non-zero number when comparing the values stored at
        *         this node and the given node, this number is returned, otherwise this node's id minus
        *         the given node's id is returned.
        */
       public int compareTo(Node other){
           int comparison = comparator.compare(value, other.value);
           return comparison;
       }
 
       /**
        * Finds and returns the smallest node in the tree rooted at this node.
        *
        * @return the smallest valued node in the tree rooted at this node, which maybe this node. 
        */
       protected final Node smallestNodeInSubTree(){
           Node current = this;
           while(current != null){
               if(current.leftChild == null){
                   break;
               } else {
                   current = current.leftChild;
               }
           }
           return current;
       }
       
       /**
        * Finds the largest node in the tree rooted at this node.
        *
        * @return the largest valued node in the tree rooted at this node which may be this node.
        */
       protected final Node largestNodeInSubTree(){
           Node current = this;
           while(current != null){
               if(current.rightChild == null){
                   break;
               } else {
                   current = current.rightChild;
               }
           }
           return current;
       }
       
       /**
        * Gets the next biggest node in the tree, which is <code>null</code> if this is
        * the largest valued node.
        *
        * @return the next biggest node in the tree, which is <code>null</code> if this
        *         is the largest valued node. 
        */
       protected final Node successor(){
           Node successor = null;
           if(rightChild != null){
               successor = rightChild.smallestNodeInSubTree();
           } else if(parent != null){
               Node current = this;
               while(current != null && current.isRightChildOfParent()){
                   current = current.parent;
               }
               successor = current.parent;
           }
           return successor;
       }
       
       //Sets the child node to the left/right, should only be used if the given node
       //is null or a leaf, and the current child is the same..
       private void setChild(boolean isLeft, Node leaf){
           //perform the update..
           if(leaf != null){
               leaf.parent = this;
           }
           if(isLeft){
               leftChild = leaf;
           } else {
               rightChild = leaf;
           }
           
           //make sure any change to the height of the tree is dealt with..
           updateCachedValues();
           rebalanceTree(this);
       }
 
       /**
        * Returns whether or not this {@code Node} is a leaf; this is true in the case that
        * both its left and right children are set to <code>null</code>.
        * 
        * @return <code>true</code> if this node is leaf and <code>false</code> otherwise.
        */
       public boolean isLeaf(){
           return (leftChild == null && rightChild == null);
       }
 
       //performs a left rotation using this node as a pivot..
       private void leftRotateAsPivot(){
           if(parent == null || parent.rightChild != this){
               throw new RuntimeException("Can't left rotate as pivot has no valid parent node.");
           }
 
           //first move this node up the tree, detaching parent...
           Node oldParent = parent;
           Node grandParent = getGrandParent();
           if(grandParent != null){
               if(parent.isLeftChildOfParent()){
                   grandParent.leftChild = this;
               } else {
                   grandParent.rightChild = this;
               }
           }
           this.parent = grandParent; //could be null.
    
           //now make old parent left child and put old left child as right child of parent..
           Node oldLeftChild = leftChild;
           oldParent.parent = this;
           leftChild = oldParent;
           if(oldLeftChild != null){
                   oldLeftChild.parent = oldParent;
           }
           oldParent.rightChild = oldLeftChild;
    
           //now we need to update the values for height and number of children..
           oldParent.updateCachedValues();
       }
 
       /**
        * Returns, the number of children of this {@code Node} plus one.  This method uses
        * a cached variable ensuring it runs in constant time. 
        * 
        * @return the number of children of this {@code Node} plus one.
        */
       public int sizeOfSubTree(){
           return 1 + numChildren;
       }
         
       /**
        * Returns the value stored at this {@code Node}.
        * 
        * @return the value that this {@code Node} stores.
        */
       public T getValue(){
         return value;
       }
       
       //performs a left rotation using this node as a pivot..
       private void rightRotateAsPivot(){
           if(parent == null || parent.leftChild != this){
                   throw new RuntimeException("Can't right rotate as pivot has no valid parent node.");
           }
           //first move this node up the tree, detaching parent...
           Node oldParent = parent;
           Node grandParent = getGrandParent();
           if(grandParent != null){
               if(parent.isLeftChildOfParent()){
                   grandParent.leftChild = this;
               } else {
                   grandParent.rightChild = this;
               }
           }
           this.parent = grandParent; //could be null.
 
           //now switch right child to left child of old parent..
           oldParent.parent = this;
           Node oldRightChild = rightChild;
           rightChild = oldParent;
           if(oldRightChild != null){
               oldRightChild.parent = oldParent;
           }
           oldParent.leftChild = oldRightChild;
 
           //now we need to update the values for height and number of children..
           oldParent.updateCachedValues();
        }
 
        /**
         * Updates the height and the number of children for nodes on the path to this.
         * Also calls {@code #updateAdditionalCachedValues()}, for every node on the path to
         * this, including this one.
         */
        protected final void updateCachedValues(){
            Node current = this;
            while(current != null){
                if(current.isLeaf()){
                    current.height = 0;
                    current.numChildren = 0;
                    
                } else {
                    //deal with the height..
                    int leftTreeHeight = (current.leftChild == null) ? 0 : current.leftChild.height;
                    int rightTreeHeight = (current.rightChild == null) ? 0 : current.rightChild.height;
                    current.height = 1 + Math.max(leftTreeHeight, rightTreeHeight);
                    
                    //deal with the number of children..
                    int leftTreeSize = (current.leftChild == null) ? 0 : current.leftChild.sizeOfSubTree();
                    int rightTreeSize = (current.rightChild == null) ? 0 : current.rightChild.sizeOfSubTree();                   
                    current.numChildren = leftTreeSize + rightTreeSize;
                }
                
                //update any additional cached values set if required..
                current.updateAdditionalCachedValues();
                
               //propagate up the tree.. 
               current = current.parent;
            }
        }
        
        /**
         * Called when a node is inserted or removed from the tree and provides a hook for
         * sub-classes to get their cached values updated. 
         * <p>
         * This method is called every time the list is altered.  It is first called on the deepest node
         * which is affected by a given change and is then subsequently called on ancestors of this node until
         * it is called on the root node.
         * <p>
         * It is therefore only suitable for updating cached values in the case that they are non-global
         * and do not rely on the parent node having the correct value when being calculated.
         * <p>
         * This implementation is empty, and hence does nothing.
         */
        protected void updateAdditionalCachedValues(){
          //do nothing - this is a hook to allow subclasses to provide additional behaviour..
        }
 
        //Just replaces the values this this node with those in other..
        //should only be called when this is doing to be removed and has just one value..
        private void switchValuesForThoseIn(Node other){
            this.value = other.value;  //switch the values over, nothing else need change..
        }
        
        //returns (height of the left subtree - the right of the right subtree)..
        private int getBalanceFactor(){
            return ((leftChild == null) ? 0 : leftChild.height + 1) -
                         ((rightChild == null) ? 0 : rightChild.height + 1);
        }
 
        //Sets the left child node.
        private void setLeftChild(Node leaf){
            if((leaf != null && !leaf.isLeaf()) || (leftChild != null && !leftChild.isLeaf())){
                throw new RuntimeException("setLeftChild should only be called with null or a leaf node, to replace a likewise child node.");
            }
            setChild(true, leaf);
        }
 
        //Sets the right child node.
        private void setRightChild(Node leaf){
            if((leaf != null && !leaf.isLeaf()) || (rightChild != null && !rightChild.isLeaf())){
                throw new RuntimeException("setRightChild should only be called with null or a leaf node, to replace a likewise child node.");
            }
            setChild(false, leaf);
        }
   } //End of inner class: Node.
 
}