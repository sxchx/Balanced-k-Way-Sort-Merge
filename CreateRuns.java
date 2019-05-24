import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;


/**
 *  Balanced k-way Sort Merge
 *  CreateRuns.java
 *  Purpose: takes an integer and the name of a file to sort as command-line arguments.
 *	Creates initial runs with the replacement selection strategy using a Heap with a 
 *	maximum size specified by the command-line integer.
 *  
 *  Authors: Sacha Raman and Elizabeth Macken
 *  
 */
class CreateRuns
{
    //Calculate the parent position based off the current position
    public static int parent(int pos) {
        return (((pos + 1) / 2) - 1);
    }
    
    //Calculate the leftChild position based off the current position
    public static int leftChild(int pos) {
        return (((pos + 1) * 2) - 1);
    }
    
    //Calculate the rightChild position based off the current position
    public static int rightChild(int pos) {
        return ((pos + 1) * 2);
    }
    
    //Insert the input string into the min heap in the correct position (Upheap)
    public static String[] insert(String[] heap, int pos, String input) {
        //insert the input into the next available position
        heap[pos] = input;
        //Upheap - While input is not root pos and its contents is smaller than the parent
        while (pos != 0 && heap[pos].compareTo(heap[parent(pos)]) <= 0) {
            //swap parent with current pos and update pos index
            heap = swap(heap, parent(pos), pos);
            pos = parent(pos);
        }
        return heap;
    }
    
    //Downheap - Order heap from current position downheap
    public static String[] downheap(String[] heap, int pos, int notionalCapacity) {
        int smaller;
        //while left child is within notionalCapacity and current pos is not smaller than both its children
        while ((leftChild(pos) < notionalCapacity) && ((heap[pos].compareTo(heap[leftChild(pos)]) > 0) || (rightChild(pos) < notionalCapacity && (heap[pos].compareTo(heap[rightChild(pos)])) > 0))) {
            //if there is a rightChild and the rightChild is smaller, get its index, else get the index of leftChild
            if((rightChild(pos) < notionalCapacity) && heap[leftChild(pos)].compareTo(heap[rightChild(pos)]) > 0) {
                smaller = rightChild(pos);
            }
            else {
                smaller = leftChild(pos);
            }
            //swap the pos and child values and update current pos index
            heap = swap(heap, smaller, pos);
            pos = smaller;
        }
        return heap;
    }
    
    //Reheap - When the notionalCapacity is 0, re-sort the heap bottom up - Downheap
    public static String[] reheap(String[] heap) {
        //calculate the index of the last node that is a parent
        int mid = ((heap.length / 2) - 1);
        //downheap until everything is re-sorted
        for(int i = mid; i >= 0; i--) {
            heap = downheap(heap, i, heap.length);
        }
        return heap;
    }

    //Swap two values in the heap
    public static String[] swap(String[] heap, int index1, int index2) {
		//store index1 in temp variable
        String temp = heap[index1];
		//swap value at index2 to index1
        heap[index1] = heap[index2];
		//put value of index1 from temp to index2
        heap[index2] = temp;
        return heap;
    }
    
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Usage: java CreateRuns <max heap int> <filename>");
            return;
        }
        try {
			//declare variables
			//get integer which determines size of heap, if less than 1 then return.
            int maxHeapSize = Integer.parseInt(args[0]);
			if(maxHeapSize < 1) {
				System.err.println("Error: Integer provided must be greater than 0");
				return;
			}
            int notionalCapacity = maxHeapSize;
            String filename = args[1];
            String[] heap = new String[maxHeapSize];            
            int currHeapSize = 0;
            int runCount = 0;
            boolean sInserted = false;
            String currentRun = null;
			//create reader to read in input and writer to print output
            BufferedReader br = new BufferedReader(new FileReader(filename));
            FileWriter fw = new FileWriter(filename + ".runs");
            PrintWriter pw = new PrintWriter(fw);
            String s = br.readLine();
			//while there are still lines to be read
            while (s != null) {
                //if the number of items in the heap is less that total capacity
                if(currHeapSize < maxHeapSize) {
                    //call insert method and inc current heap size
                    heap = insert(heap, currHeapSize, s);
                    currHeapSize++;
                }
                else {
					//while we have not inserted our read in string
                    while(sInserted == false) {
                        //if there are no items in the run or the heap root is >= to the last outputted item
                        if(currentRun == null || heap[0].compareTo(currentRun) >= 0 ) {
                            //Replace - print root value to output file, update currentRun, replace root with input
                            pw.println(heap[0]);
                            currentRun = heap[0];
                            heap[0] = s;
                            sInserted = true;
                            // downheap
                            heap = downheap(heap, 0, notionalCapacity);
                        }
                        else {
                            //call remove method
                            if(notionalCapacity > 1) {
                                //swap root with the last value in our heap
                                heap = swap(heap, notionalCapacity-1, 0);
                                //reduce our notionalCapacity and downheap
                                notionalCapacity--;
                                heap = downheap(heap, 0, notionalCapacity);
                            }
                            //if there is 1 item left and we cannot replace
                            else {
                                //reset the notionalCapacity and reheap
                                notionalCapacity = maxHeapSize;
                                heap = reheap(heap);
                                //signify end of tape, reset currentRun
                                //pw.println("][][][][");        
                                currentRun = null;    
                                runCount++;    
                            }
                            sInserted = false;
                        }
                    }
                    sInserted = false;
                }
                s = br.readLine();
            }
            //end current run, reset notionalCapacity, downheap, output remaining heap
            //pw.println("][][][][");
            notionalCapacity = maxHeapSize;
            heap = reheap(heap);
            for(int i = heap.length -1; i >= 0; i--){
                pw.println(heap[0]);
                heap[0] = heap[i];
                notionalCapacity--;
                heap = downheap(heap, 0, notionalCapacity);
            }
            //pw.println("][][][][");
            runCount++;

            br.close();
            pw.close();
            fw.close();
            System.err.println("Total runs: " + runCount);
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e);
        }
    }
}
