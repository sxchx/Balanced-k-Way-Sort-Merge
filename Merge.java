import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;


/**
 *  Balanced k-way Sort Merge
 *  Merge.java
 *  Purpose: Takes an integer and a filename as command-line arguments. 
 *	The file contains initial runs created by CreateRuns program, and the integer is the k of the k-way merge. 
 *	Initial runs are read from the input file and distributed over k many temporary files for subsequent
 *	merging using a heap. Output is a file containing all the data in sorrted order.  
 *
 *  Authors: Elizabeth Macken and Sacha Raman
 *  
 */
public class Merge {
    public static void main(String[] args) {
        // Checking the correct number of arguments were passed and that the file passed is a .runs file
        if (args.length != 2 || !(args[1].endsWith(".runs"))) {
            System.out.println("ERROR - Correct usage: java Merge [integer] [filename.runs]");
            return;
        }
        else {
            try {
                // Getting the k-value passed and checking that it is at least 2
                int k = Integer.parseInt(args[0]);
                if (k < 2) {
                    System.out.println("ERROR - k-value must be greater than 1");
					return;
                }
                // Creating a reader to read from the input file and an array of writers to print to files for sorting
                BufferedReader br = new BufferedReader(new FileReader(args[1]));
                PrintWriter[] writers = new PrintWriter[k];
                // Creating a file with the same name as the input file but replacing .runs with .sorted
                String filename = args[1].substring(0, args[1].length() - 5);
                File sortedFile = new File(filename + ".sorted");
                // Creating an array to store twice as many files as the k-value we were given
                File[] files = new File[k*2];
                
                // Populating the file array with temporary files that will be deleted on exit
                for (int j = 0; j < (k*2); j+=2) {
                    files[j] = File.createTempFile("Merge-", ".txt");
                    files[j].deleteOnExit();
                    files[j+1] = File.createTempFile("Merge-", ".txt");
                    files[j+1].deleteOnExit();
                }
                
                // Populating the writers array with elements linked to the first k many temporary files in our array
                for (int i = 0; i < k; i++) {
                    // The append flag is set to true so no data is overwritten
                    writers[i] = new PrintWriter(new BufferedWriter(new FileWriter(files[i], true)));
                }
                
                // IF NOT USING END-OF-RUN MARKERS:                
                // Adding each line from the input file to the current temporary file until we reach the end of that run
                int n = 0;
                String inputLine;
                String previousLine = null;
                while ((inputLine = br.readLine()) != null) {
                    // Checking whether we have reached the end of a run
                    if (previousLine != null && inputLine.compareTo(previousLine) <= 0) {
                        // If so increment n (the index of the temporary file)
                        n++;
                        // If n is now outside of the first k many files in the file array, reset n to 0
                        if (n == k) {
                            n = 0;
                        }
                    }
                    // Then adding the line as normal to the temporary file at array index n
                    writers[n].println(inputLine);
                    // Storing the current line so we can compare against it the next time
                    previousLine = inputLine;
                }
                
                // IF USING END-OF-RUN MARKERS:
                /*// Adding each line from the input file to the current temporary file until we reach the end of that run
                int n = 0;
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    // Checking whether we have reached the end-of-line marker
                    if (inputLine.equals("][][][][")) {
                        // If so increment n (the index of the temporary file)
                        n++;
                        // If n is now outside of the first k many files in the file array, reset n to 0
                        if (n == k) {
                            n = 0;
                        }
                    }
                    // Otherwise adding the line as normal to the temporary file at array index n
                    else {
                        writers[n].println(inputLine);
                    }
                }*/
                
                // Closing the inputfile reader and all writers in the writer array
                br.close();
                for (int i = 0; i < k; i++) {
                    writers[i].close();
                }
                
                // We don't know whether the sorted file will be file 0 or file k in the files array, so we can check
                // whether both of the preceding files are empty, which if true means all the data is sorted in either
                // file 0 or file k. Until then, keep looping.
                boolean oddNumPasses = true;
                int totalPasses = 0;
                while (!(files[1].length() == 0 && files[k+1].length() == 0)) {
                    // If we are in an odd number of passes, we will have files 0 - k as our input files and k - k*2
                    // as our output files, otherwise we will have files k - k*2  as our input files and 0 - k*2 as
                    // our output files, so we need this flag to check
                    if (oddNumPasses) {
                        // Creating a new minheap with a max capacity of the number of files we have (k)
                        MinHeap minHeap = new MinHeap(k);
                        // Adding the files if they are not empty
                        for (int i = 0; i < k; i++) {
                            if (files[i].length() != 0) {
                                minHeap.addFile(files[i]);
                            }
                        }
                        
                        // Checking that the input files are not ALL empty
                        boolean inputFilesNotEmpty = true;
                        int passes = k;
                        while (inputFilesNotEmpty) {
                            // The output file starts at k and increments after each pass, if it ever reaches k*2 it
                            // resets to k
                            if (passes == k*2) {
                                passes = k;
                            }
                            // Creating a new pass and incrementing the pass counter by 1
                            minHeap.createPass(files[passes]);
                            totalPasses++;
                            
                            // Assuming the input files are empty, check the length of each. If any are not empty, set
                            // the flag to true again
                            inputFilesNotEmpty = false;
                            for (int i = 0; i < k; i++) {
                                if (files[i].length() != 0) {
                                    inputFilesNotEmpty = true;
                                }
                            }
                            // Incrementing the pass file counter so we will write to the next file in the array
                            passes++;
                        }
                        oddNumPasses = false;
                    }
                    else {
                        // Creating a new minheap with a max capacity of the number of files we have (k)
                        MinHeap minHeap = new MinHeap(k);
                        // Adding the files if they are not empty
                        for (int i = k; i < k*2; i++) {
                            if (files[i].length() != 0) {
                                minHeap.addFile(files[i]);
                            }
                        }
                        
                        // Checking that the input files are not ALL empty
                        boolean inputFilesNotEmpty = true;
                        int passes = 0;
                        while (inputFilesNotEmpty) {
                            // The output file starts at 0 and increments after each pass, if it ever reaches k it
                            // resets to 0
                            if (passes == k) {
                                passes = 0;
                            }
                            // Creating a new pass and incrementing the pass counter by 1
                            minHeap.createPass(files[passes]);
                            totalPasses++;
                            
                            // Assuming the input files are empty, check the length of each. If any are not empty, set
                            // the flag to true again
                            inputFilesNotEmpty = false;
                            for (int i = k; i < k*2; i++) {
                                if (files[i].length() != 0) {
                                    inputFilesNotEmpty = true;
                                }
                            }
                            // Incrementing the pass file counter so we will write to the next file in the array
                            passes++;
                        }
                        oddNumPasses = true;
                    }
                }
                
                // Checking whether the sorted data is stored in the file at array index 0, and if so renaming it to the
                // sorted filename we created
                if (files[0].length() != 0) {
                    files[0].renameTo(sortedFile);
                }
                // Checking whether the sorted data is stored in the file at array index k, and if so renaming it to the
                // sorted filename we created
                else if (files[k].length() != 0) {
                    files[k].renameTo(sortedFile);
                }
                // The program should never go into here if the logic above is sound, but catching the error just in case
                else {
                    System.out.println("ERROR: sorted file not found");
                }
                
                // Printing to standard error the total number of passes required to sort the data
                System.err.println("Total Passes: " + Integer.toString(totalPasses));
            }
            catch (Exception ex) {
                // Catching the exception of being unable to parse the argument at index 0 to an int (and any other
                // exceptions)
                System.out.println("Exception Thrown! " + ex.toString());
                return;
            }
        }
    }
}

class MinHeap {
    // Creating variables to store the capacity of the MinHeap once initialised
    private int capacity_, usableCapacity_;
    private int currentCapacity_ = 0;
    // Creating arrays to hold the String data, the reader objects and the file objects
    private String[] data_;
    private BufferedReader[] readers_;
    private File[] files_;
    
    // MinHeap constructor, passed an integer as an argument for the size of the heap
    public MinHeap(int capacity) {
        // Setting the capacity passed in as the max size for this MinHeap
        capacity_ = capacity;
        usableCapacity_ = capacity;
        // Creating the arrays now that we know the max size they will have to be
        data_ = new String[capacity];
        readers_ = new BufferedReader[capacity];
        files_ = new File[capacity];
        
    }
    
    // Public method to add a file to the heap, passed a File object as an argument
    public void addFile(File nodeFile) {
        try {
            // Creating readers, String objects and linking the file all with the same index in their respective arrays
            readers_[currentCapacity_] = new BufferedReader(new FileReader(nodeFile));
            data_[currentCapacity_] = readers_[currentCapacity_].readLine();
            files_[currentCapacity_] = nodeFile;
            // Incrementing the count of how many objects we have filled in our tree
            currentCapacity_++;
        }
        catch (Exception e) {
            System.out.println("Exception Thrown! " + e.toString() + " - unable to read from the given node file");
            System.exit(1);
        }
    }
    
    // Public method to create a pass through the data, given a File to print the pass to
    public void createPass(File outputFile) {
        try {
            // Setting the usable capacity to the number of elements we have in our MinHeap
            usableCapacity_ = currentCapacity_;
            // If any of these elements are empty, move them to the end of the heap and reduce the size of our heap
            for (int i = 0; i < usableCapacity_; i++) {
                if (data_[i] == null) {
                    swap(usableCapacity_-1, i, null);
                    usableCapacity_--;
                }
            }
            // Putting the MinHeap in heap order
            reheap();
            
            // Creating a writer object to the given output file, setting the append flag to true
            PrintWriter writer_ = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
            
            // Looping until we have no more data in our heap we can print to the current pass
            boolean passComplete = false;
            String lastPrinted = null;
            while (passComplete != true) {
                // If the current data is not empty AND either we haven't printed anything yet or the last thing we
                // printed was less than the current data (still in order)
                if ((data_[0] != null) && (lastPrinted == null || data_[0].compareTo(lastPrinted) >= 0)) {
                    // Printing current data to the output file and storing it as the last thing we printed
                    writer_.println(data_[0]);
                    lastPrinted = data_[0];
                    
                    // Checking whether there is more data in the current file we have just printed from
                    String next = readers_[0].readLine();
                    // If yes, setting it as the current root and then downheaping to get the smallest at the root
                    if (next != null) {
                        data_[0] = next;
                        downheap(0);
                    }
                    else {
                        // Otherwise, if we have more than 1 node in our heap free...
                        if (usableCapacity_ > 1) {
                            // Empty the current file we are reading from
                            PrintWriter pw = new PrintWriter(files_[0]);
                            pw.print("");
                            pw.close();
                            // Move the current node to the max of our usable heap, reduce the size by 1, and downheap
                            swap(usableCapacity_-1, 0, next);
                            usableCapacity_--;
                            downheap(0);
                        }
                        // If we are on the last node of our heap, and there is no more data in this file...
                        else {
                            // Empty the current file we are reading from
                            PrintWriter pw = new PrintWriter(files_[0]);
                            pw.print("");
                            pw.close();
                            // Set the flag to true that we have completed this pass, and close the output file writer
                            passComplete = true;
                            writer_.close();
                        }
                    }
                }
                // Otherwise either our data item is null, or is larger than the last thing we printed
                else {
                    // If we have more than 1 node free in our heap...
                    if (usableCapacity_ > 1) {
                        // Move the current node to the max of our usable heap, reduce the size by 1, and downheap
                        swap(usableCapacity_-1, 0, data_[0]);
                        usableCapacity_--;
                        downheap(0);
                    }
                    // Otherwise this is the last node of our heap, so we set the flag to true that we have completed
                    // this pass, and close the output file writer
                    else {
                        passComplete = true;
                        writer_.close();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception Thrown! " + e.toString() + " - unable to write to the output file or read from the given node files");
            System.exit(1);
        }
    }
    
    // Private method to reorder the entire heap
    private void reheap() {
        // Calculate the middle of the heap (i.e. the first node that can be a parent) and downheap and then move up
        // the heap until at the root
        int mid = (usableCapacity_/2)-1;
        for (int i = mid; i >= 0; i--) {
            downheap(i);
        }
    }
    
    // Private method to order a node in the heap with regards to its children
    private void downheap(int pos) {
        int smaller;
        // If the data at the current position is null, swap with the largest node in the usable heap
        if (data_[pos] == null) {
            swap(usableCapacity_-1, pos, data_[pos]);
        }
        else {
            // Checking if the left child node or right child node has data that is less than the current
            // position, and are viable (that is, not null or outside the bounds of the usable heap)
            while ((leftChild(pos) < usableCapacity_) && data_[leftChild(pos)] != null && ((data_[pos].compareTo(data_[leftChild(pos)]) > 0) || (rightChild(pos) < usableCapacity_ && (data_[rightChild(pos)] != null) && (data_[pos].compareTo(data_[rightChild(pos)])) > 0))) {
                // If the right child node is smaller than its parent, store that as the smallest
                if ((rightChild(pos) < usableCapacity_) && data_[rightChild(pos)] != null && data_[leftChild(pos)].compareTo(data_[rightChild(pos)]) > 0) {
                    smaller = rightChild(pos);
                }
                // Otherwise store the left child as the smallest
                else {
                    smaller = leftChild(pos);
                }
                // Swap the node at the current position with the position of its smallest child
                swap(smaller, pos, data_[pos]);
                // Store the position just moved to its child node as the current posiion again, and loop
                // through, this time with its new children, until it reaches a spot where it is larger than
                // both of its children
                pos = smaller;
            }
        }
    }
    
    // Private method to swap the positions of two nodes, passed in the indexes of those nodes and the data value of
    // the larger element, so as to not call readLine() twice
    private void swap(int smaller, int current, String largerElement) {
        try {
            //Storing the current data, file, and reader objects of the smaller position in temporary variables
            String tempString = data_[smaller];
            File tempFile = files_[smaller];
            BufferedReader tempReader = readers_[smaller];
            
            // Setting the data, file, and reader pointers of the arrays equal to the data, file, and reader objects of
            // the larger node
            data_[smaller] = largerElement;
            files_[smaller] = files_[current];
            readers_[smaller] = readers_[current];
            
            // Setting the data, file, and reader pointers of the larger position in the arrays to the saved values
            // that were for the smaller position in the arrays
            data_[current] = tempString;
            files_[current] = tempFile;
            readers_[current] = tempReader;
        }
        catch (Exception e) {
            System.out.println("Exception Thrown! " + e.toString() + " - unable to read from the given root node file");
            System.exit(1);
        }
    }
    
    // Private method to calculate the position of the left child of a node, given the current position. Returns an int
    private int leftChild(int pos) {
        return (((pos + 1) * 2) - 1);
    }
    
    // Private method to calculate the position of the right child of a node, given the current position. Returns an int
    private int rightChild(int pos) {
        return ((pos + 1) * 2);
    }
}
