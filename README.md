# Balanced-k-Way-Sort-Merge

## Creating Runs
Creates initial runs with the replacement selection strategy using a Heap with a maximum size specified by the command-line integer.

### Input
Takes an integer and the name of a file to sort as command-line arguments:
java CreateRuns <max heap int> <filename>
```bash
$ javac CreateRuns.java
$ java CreateRuns 500 BrownCorpus.txt
Total runs: 54
```

## Merging Runs
The file contains initial runs created by CreateRuns program, and the integer is the k of the k-way merge.
Initial runs are read from the input file and distributed over k many temporary files for subsequent merging using a heap. Output is a file containing all the data in sorrted order.

### Input
Takes an integer and a filename as command-line arguments.
java Merge <integer> <filename.runs>
```bash
$ javac Merge.java
$ java Merge 5 BrownCorpus.txt.runs
Total Passes: 42 
```
