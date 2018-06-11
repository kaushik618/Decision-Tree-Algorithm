// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Courtney Wood

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.*;

class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1] CLOUDY
	 *      children[2] is the branch for attribute 3 == strings[3][2] Rainy
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class TreeNode {

		TreeNode[] children;
		int value;

		public TreeNode(TreeNode[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()

		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class TreeNode


  private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private TreeNode decisionTree;	// Tree learnt in training, used for classifying (this is the root node)
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor

	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);

	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		for (int i = 1; i < testData.length; i++) {
			String answer = classifyHelper(decisionTree, testData[i]); //returns what the correct answer is
			System.out.println(answer);
		}
	} // classify()

	public String classifyHelper(TreeNode node, String[] ex) {
		if (node.children == null) {
			return strings[attributes - 1][node.value]; //returns leaf node
		}
		else {
			String category = ex[node.value];
			int locInStrings = -1;
			String[] stringsForAttribute = strings[node.value];
			for (int i = 0; i < stringsForAttribute.length; i++) {
				if (category.equals(stringsForAttribute[i])) {
					locInStrings = i;
				}
			}
			return classifyHelper(node.children[locInStrings], ex);
		}
	}

	public void train(String[][] trainingData) {
		indexStrings(trainingData);

		//remove null values from strings
		String[][] stringsNoNull = new String[strings.length][];
		for (int i = 0; i < strings.length; i++) {
			ArrayList<String> realArray = new ArrayList<>();
			for (int j = 0; j < strings[i].length; j++) {
				if (strings[i][j] != null) {
					realArray.add(strings[i][j]);
				}
			}
			String[] arrReal = new String[realArray.size()];
			for (int j = 0; j < arrReal.length; j++) {
				arrReal[j] = realArray.get(j);
			}
			stringsNoNull[i] = arrReal;
		}
    strings = stringsNoNull;

		decisionTree = new TreeNode(null, -1);
		ArrayList<Integer> alreadyUsedColumns = new ArrayList<>();
		trainRecursive(decisionTree, trainingData, alreadyUsedColumns);

	} // train()

	public void trainRecursive(TreeNode node, String[][] subset, ArrayList<Integer> alreadyUsedColumns) {
    if (subsetHasSameClass(subset)) { //if subset all belongs to the same class, create a leaf node for this class
			int leafVal = findLeafValue(subset);
			if (node == null) node = new TreeNode(null, leafVal);
			else {
				node.value = leafVal;
				node.children = null;
			}
		}
		else {
			int category = findNextQuestion(subset,alreadyUsedColumns);
			if (category == -1) { //if there are no more questions left to ask
        int leafVal = findMajority(subset);
  			if (node == null) node = new TreeNode(null, leafVal);
  			else {
  				node.value = leafVal;
  				node.children = null;
  			}
			}
			else {
				alreadyUsedColumns.add(category);
        TreeNode[] children = new TreeNode[strings[category].length];
        //initialize children
				for (int i = 0; i < children.length; i++) {
					children[i] = new TreeNode(null, -1);
				}

				if (node == null) node = new TreeNode(children, category);
				else {
					node.value = category;
					node.children = children;
				}

				//determine if any subsets created from dividing based on the question are empty
        ArrayList<String[][]> subsets = new ArrayList<>();
        boolean needFindMajority = false;
				for (int i = 0; i < strings[category].length; i++) {
					String[][] newSubset = createSubset(subset, strings[category][i], category);
          subsets.add(newSubset);
          if (newSubset.length == 0) needFindMajority = true;
				}

				//if any subsets are empty, find the majority class and create a leaf node
        if (needFindMajority) {
          int leafVal = findMajority(subset);
    			if (node == null) node = new TreeNode(null, leafVal);
    			else {
    				node.value = leafVal;
    				node.children = null;
    			}
        }
				//if not, call trainRecursive() on all subsets
        else {
          for (int i = 0; i < node.children.length; i++) {
            ArrayList<Integer> alreadyUsedCopy = copy(alreadyUsedColumns);
  					trainRecursive(node.children[i], subsets.get(i), alreadyUsedCopy);
          }
        }
			}
    }
	}

	public int findMajority(String[][] subset) { //returns the majority class of the subset
		//create a map of classes to occurences
		HashMap<String, Integer> occurences = new HashMap<>();
		for (int i = 0; i < subset.length; i++) {
			String classifier = subset[i][attributes - 1];
			if (occurences.containsKey(classifier)) {
				int num = occurences.get(classifier);
				num++;
				occurences.put(classifier, num);
			}
			else {
				occurences.put(classifier, 1);
			}
		}

		//find the biggest occurence
		String classification = "";
		int largestValue = Integer.MIN_VALUE;
		for (Map.Entry<String, Integer> entry : occurences.entrySet()) {
			int entryVal = entry.getValue();
			if (entryVal > largestValue) {
				largestValue = entryVal;
				classification = entry.getKey();
			}
		}

		String[] classes = strings[attributes - 1];
		for (int i = 0; i < classes.length; i++) {
			if (classification.equals(classes[i])) {
        return i;
      }
		}
		return -1;
	}

	public int findNextQuestion(String[][] subset, ArrayList<Integer> alreadyUsedColumns) { //returns column of category to ask
		double highest = Integer.MIN_VALUE;
		int col = -1;
		double HofS = calculateHS(subset);
		for (int i = 0; i < attributes - 1; i++) {
			if (!alreadyUsedColumns.contains(i)) {
				HashMap<String, HashMap<String, Integer> > map = split(subset, i);
				double gain = calculateGain(map, HofS);
				if (gain > highest) {
					highest = gain;
					col = i;
				}
			}
		}
    return col;
	}

	public int findLeafValue(String[][] subset) { //returns int value based on location of classifier in strings array
		String classification = subset[0][subset[0].length - 1];
		//find class in strings
		String[] classes = strings[attributes - 1];
		for (int i = 0; i < classes.length; i++) {
			if (classification.equals(classes[i])) return i;
		}
		return -1;
	}

	public boolean subsetHasSameClass(String[][] data) { //returns true if all examples in data have the same class
		String classification = data[0][data[0].length - 1];
		for (int i = 0; i < data.length; i++) {
			if (!classification.equals(data[i][data[i].length - 1])) {
				return false;
			}
		}
		return true;
	}

	public String[][] createSubset(String[][] data, String attribute, int colOfAttribute) { //creates a subset for the given attribute from data
		ArrayList<String[]> examplesToKeep = new ArrayList<>();
		int col = colOfAttribute;
		for (int i = 0; i < data.length; i++) {
			if (data[i][colOfAttribute].equals(attribute)) {
				examplesToKeep.add(data[i]);
			}
		}
		String[][] newArray = new String[examplesToKeep.size()][attributes];
		for (int i = 0; i < examplesToKeep.size(); i++) {
			newArray[i] = examplesToKeep.get(i);
		}
		return newArray;
	}

	public double calculateGain(HashMap<String, HashMap<String, Integer> > map, double HofS) {
		HashMap<String, Double> HVals = new HashMap<>();
		ArrayList<Double> vals = new ArrayList<>();
		for (Map.Entry<String, HashMap<String, Integer> > entry : map.entrySet()) {
			String category = entry.getKey();
			HashMap<String, Integer> hashmap = entry.getValue();
			double total = 0;
			for (Map.Entry<String, Integer> entry2 : hashmap.entrySet()) {
				total += entry2.getValue();
			}
			double runningHVal = 0;
			for (Map.Entry<String, Integer> entry2 : hashmap.entrySet()) {
				double x = entry2.getValue() / total;
				double xlogxVal = xlogx(x);
				runningHVal -= xlogxVal;
			}
			runningHVal = runningHVal * (total / (double) examples);
			vals.add(runningHVal);
		}
		double G = HofS;
		for (int i = 0; i < vals.size(); i++) {
			G -= vals.get(i);
		}
		return G;
	}

	public HashMap<String, HashMap<String, Integer> > split(String[][] d, int col) { //returns Hashmap of hashmaps. hashmap for each possible choice
		HashMap<String, HashMap<String, Integer> > splitMap = new HashMap<>();
		int rows = d.length;
		int cols = d[0].length;
		//iterate through all examples
		int start = 0;
		if (d.length == examples) start = 1;
		for (int j = start; j < rows; j++) {
			String name = d[j][col];
			if (!splitMap.containsKey(name)) {
				//create a hashmap for the values
				HashMap<String, Integer> valMap = new HashMap<>();
				if (valMap.containsKey(d[j][cols - 1])) {
					//increase number
					int num = valMap.get(d[j][cols - 1]);
					num++;
					valMap.put(d[j][cols - 1], num);
				}
				else {
					valMap.put(d[j][cols - 1], 1);
				}
				splitMap.put(name, valMap);
			}
			else {
				HashMap<String, Integer> valMap = splitMap.get(name);
				if (valMap.containsKey(d[j][cols - 1])) { //already exists in valMap
					//increase number
					int num = valMap.get(d[j][cols - 1]);
					num++;
					valMap.put(d[j][cols - 1], num);
				}
				else {
					//add a new one to the hashmap
					valMap.put(d[j][cols - 1], 1);
				}
			}
		}
		return splitMap;
	}

	public double calculateHS(String[][] data) {
		HashMap<String, Double> map = new HashMap<>();
		//iterate through last column
		int start = 0;
		if (data.length == examples) start = 1;
		for (int i = start; i < data.length; i++) {
			String result = data[i][data[i].length - 1];
			if (!map.containsKey(result)) {
				map.put(result, 1.0);
			}
			else {
				//result is already in map
				double val = map.get(result);
				val++;
				map.put(result, val);
			}
		}

		HashMap<String, Double> logVals = new HashMap<>();
		for ( Map.Entry<String, Double> entry : map.entrySet()) {
			String key = entry.getKey();
			double val = entry.getValue();
			double x = val / (examples - 1);
			double result = xlogx(x);
			logVals.put(key, result);
		}

		double HofS = 0;
		for (double log : logVals.values()) {
			HofS = HofS - log;
		}
		return HofS;
	}

	public ArrayList<Integer> copy(ArrayList<Integer> columns) { //makes a hard copy of an arraylist
		ArrayList<Integer> newArr = new ArrayList<>();
		for (int i = 0; i < columns.size(); i++) {
			newArr.add(columns.get(i));
		}
		return newArr;
	}

	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()

	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null) {
			lines++;
		}
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
/*
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				System.out.println(data[i][j]);
			}
		}
*/
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {

		long startTime = System.nanoTime();

		long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();

		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);

		long endTime   = System.nanoTime();
		long totalTime = (endTime - startTime)/1000000;
		System.out.println("Total Execution Time = " + totalTime);

		long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		long actualMemUsed=(afterUsedMem-beforeUsedMem);
		System.out.println("Memory used = " + actualMemUsed);

	} // main()

} // class ID3
