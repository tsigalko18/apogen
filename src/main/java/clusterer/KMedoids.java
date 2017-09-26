/*
 * File: MyPAM.java
 * Auth: Jian Lu (jlu1@cs.uml.edu)
 * Date: 2013-11-01
 * Desc: This program is used in the project of Data Mining Course (91.544) in UMass Lowell.
 * 		The project is supposed to implement K-medoids clustering algorithms in WEKA system.
 * 		This java code implements the Partitioning Around Medoids (PAM) algorithm, which is
 * 		one of the most common used K-medoids clustering algorithm.
 * 		
 * Copyright (c) 2013 University of Massachusetts, Lowell.
 * 
 * ------------------------------------------------------------------------
 * PAM Algorithm Description
 * ------------------------------------------------------------------------
 * The PAM algorithm works as following steps:
 * 1. Initialization Step: randomly select k data points as the medoids.
 * 2. Assignment Step: associate each data point to closest medoid.
 * 3. Updating Step: for each cluster (with medoid m) and for every data point o in this cluster
 * 						(a) Swap m and o; (b) Compute the total swapping cost from m to o.
 * 					 If there exists o with negative cost, then replace m by o with the smallest cost.
 * 4. Repeat steps 2 and 3 until there is no change in medoids.
 * 5. Repeat steps 1 to 4 for several times and output the solution with the least squared error.
 * ------------------------------------------------------------------------
 * 
 */
package clusterer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.clusterers.NumberOfClustersRequestable;
import weka.clusterers.RandomizableClusterer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

/**
 * Class MyPAM, implementation of PAM algorithm This class if modified from
 * SimpleKMeans.java, which is the implementation of K-Means clustering
 * algorithm in the WEKA system.
 * 
 * @author Andrea Stocco
 */
@SuppressWarnings("unused")
public class KMedoids extends RandomizableClusterer implements NumberOfClustersRequestable, WeightedInstancesHandler {
	/***********************************************************************
	 *** Class Variables
	 ***********************************************************************/

	/*************************************************************************
	 * READ THIS FOR UNDERSTANDING CLASS VARIABLES
	 * ----------------------------------------------------------------------- 1.
	 * Before each class variable declaration, there is a comment to describe the
	 * meaning of the variable. Please read them carefully. 2. The following
	 * variables are designed to describe the clusterer: - m_NumClusters: the number
	 * of clusters - m_BestMedoids[]: the array of instance index, where each
	 * element represents the medoid of a cluster - m_BestAssignments[]: the array
	 * of length m_InstanceNum, where each element is an integer in the range of
	 * [0..K-1]. m_BestAssignments[i] = j means the i-th instance is clustred to the
	 * j-th cluster. - m_BestDistanceErrorSum: the squared errors for evaluating the
	 * clustering result. In this implementation, it is just the sum of
	 * DistanceErrors of all clusters. The DistanceError for a cluster is the sum of
	 * the distances between each instance in the cluster and the medoid instance.
	 * 3. As we mentioned in the PAM algorithm, we try to create multiple
	 * (m_RepeatTimes) clusters and choose the one with the minimum DistanceErrorSum
	 * (squared errors). We called it the LOOP (or outer loop). Inside each loop, we
	 * repeat assign-and-update steps until medoids are not changed. We call it the
	 * iteration (or inner loop). You may need to update following variables during
	 * the loops/iterations: - m_Medoids[]: the array of current medoids we selected
	 * - m_DistanceErrors[]: the DistanceErrors of all clusters -
	 * m_DistanceErrorSum: the sum of m_DistanceErrors[] 4. I use m_RuntimeInfo
	 * string to record the runing time information. You can note everything you
	 * want to display in the final output by using it.
	 ************************************************************************/

	/**
	 * for serialization, copied from SimpleKMeans.java
	 */
	static final long serialVersionUID = -3235809600124455376L;

	private String[] options = new String[6];

	/**
	 * Only used for debug, you may not need it
	 */
	private boolean m_Debug = true;

	/**
	 * true if the dataset represents pre-calculated distances
	 */
	private boolean m_distance = false;

	/**
	 * String for recording the algorithm running information
	 */
	private String m_RuntimeInfo = "";

	/**
	 * number of clusters to generate
	 */
	private int m_NumClusters = 11;

	/**
	 * The Instances object for storing the training dataset
	 */
	private Instances m_data = null;

	/**
	 * Holds the number of the instances
	 */
	private int m_InstanceNum;

	/**
	 * The indices of the medoids
	 */
	private int[] m_Medoids = null;
	private int[] m_BestMedoids = null;

	/**
	 * The sum of distance error (squared errors) for all clusters
	 */
	private double m_DistanceErrorSum;
	private double m_BestDistanceErrorSum;

	/**
	 * The number of instances in each cluster
	 */
	private int[] m_ClusterSizes;
	private int[] m_BestClusterSizes;

	/**
	 * Holds the squared errors for all clusters
	 */
	private double[] m_DistanceErrors;

	/**
	 * Assignments of cluster for each instance
	 */
	private int[] m_Assignments;
	private int[] m_BestAssignments;

	/**
	 * Maximum number of iterations to be executed
	 */
	private int m_MaxIterations = 500;

	/**
	 * Keep track of the number of iterations completed before convergence
	 */
	private int m_Iterations = 0;

	/**
	 * Holds the times that need to find best medoids
	 */
	private int m_RepeatTimes = 4;

	/**
	 * The distance function used.
	 */
	protected DistanceFunction m_DistanceFunction = new EuclideanDistance();

	/**
	 * Holds whether output the cluster result
	 */
	protected boolean m_SaveClusterResult = true;

	/***********************************************************************
	 *** Class Methods
	 ***********************************************************************/

	/*************************************************************************
	 * READ THIS BEFORE CODING
	 * ----------------------------------------------------------------------- 1.
	 * You may only need to implement following methods - buildClusterer, which
	 * implements the PAM algorithm. - toString, which returns a string describing
	 * the cluster. You can add everything you want to be displayed in the output in
	 * this method. 2. I leave a method clusterProcessedInstance, which cluster an
	 * instance according to the current medoids. You may need it in your program.
	 * 3. I leave another method saveClusters, which will save the data with
	 * clusters. You need call it at the end of the buildClusterer for saving the
	 * result in the text file. 4. Other Tips: - You can add other private methods
	 * to obtain better structure. - For full understanding, please read my comments
	 * before each method.
	 ************************************************************************/

	/**
	 * the default constructor
	 * 
	 * @param distance
	 */
	public KMedoids(boolean distance) {
		super();
		m_distance = distance;
		// m_distance = false;
	}

	/**
	 * Function: globalInfo Returns a string describing the clusterer.
	 * 
	 * @return a description of the evaluator suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String globalInfo() {
		return "Cluster data using the PAM algorithm. Can use either "
				+ "the Euclidean distance (default) or the Manhattan distance.";
	}

	/**
	 * Returns the default capabilities of the clusterer. - Only numerical
	 * attributes accepted - Missing values is not allowed
	 *
	 * @return the capabilities of this clusterer
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();
		result.enable(Capability.NO_CLASS);
		// attributes
		// result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		// result.enable(Capability.MISSING_VALUES);
		return result;
	}

	/**
	 * Updates the medoid of one cluster
	 * 
	 * @param clusterIndex
	 *            the index of the cluster
	 * @param clusterMember
	 *            the indices of the instance members in the cluster
	 * @return true if the medoid of this cluster is changed and update the
	 *         m_Medoids; otherwise return false
	 */
	private boolean updateMedoid(int clusterIndex, ArrayList<Integer> clusterMember) {
		double BestCost = m_DistanceErrors[clusterIndex];
		int NewMedoid = m_Medoids[clusterIndex];
		int ClusterSize = clusterMember.size();
		for (int i = 0; i < ClusterSize; i++) {
			double CurrentCost = 0;
			int CurrentMedoid = clusterMember.get(i);
			for (Integer x : clusterMember) {

				if (m_distance) {
					CurrentCost += m_data.get(x).attribute(CurrentMedoid).getLowerNumericBound();
				} else {
					CurrentCost += m_DistanceFunction.distance(m_data.instance(CurrentMedoid), m_data.instance(x));
				}

			}
			if (CurrentCost < BestCost) {
				NewMedoid = CurrentMedoid;
				BestCost = CurrentCost;
			}
		}
		if (NewMedoid == m_Medoids[clusterIndex]) {
			return false; // Not changed
		} else {
			m_Medoids[clusterIndex] = NewMedoid;
			m_DistanceErrors[clusterIndex] = BestCost;
			return true;
		}
	}

	/**
	 * Returns the index of the cluster for an instance
	 * 
	 * @param instance
	 *            a data instance for clustering
	 * @param updateErrors
	 *            update the within clusters sum of errors if true
	 * @return the cluster index the instance is clustered to
	 */
	private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
		double minDist = Double.MAX_VALUE;
		int bestCluster = 0;
		for (int i = 0; i < m_NumClusters; i++) {

			double dist = Double.MIN_VALUE;

			if (m_distance) {
				dist = m_data.get(m_data.indexOf(instance)).attribute(m_Medoids[i]).getLowerNumericBound();
				// dist = m_data.get(m_Medoids[i]);
			} else {
				dist = m_DistanceFunction.distance(instance, m_data.instance(m_Medoids[i]));
			}

			if (dist < minDist) {
				minDist = dist;
				bestCluster = i;
			}
		}
		if (updateErrors) {
			m_DistanceErrors[bestCluster] += minDist;
		}
		return bestCluster;
	}

	/**
	 * Saves the cluster result to the text file: PAM_InstancX_ClusterY, where X is
	 * the number of the instances and Y is the number of clusters. We add a new
	 * attribute (cluster index) to each instance.
	 * 
	 * @throws IOException
	 */
	public void saveClusters() throws IOException {
		String filename = "PAM_Instance" + Integer.toString(m_InstanceNum);
		filename += "_Cluster" + Integer.toString(m_NumClusters);
		filename += ".txt";
		File file = new File(filename);
		Writer writer = new BufferedWriter(new FileWriter(file));

		for (int i = 0; i < m_InstanceNum; i++) {
			writer.write(m_data.instance(i).toString());
			writer.write(",");
			writer.write(Integer.toString(m_BestAssignments[i]));
			writer.write("\n");
		}
		writer.close();
	}

	private ArrayList<Integer> getClusterMembers(int clusterIndex) {
		ArrayList<Integer> out = new ArrayList<Integer>();

		for (int i = 0; i < m_InstanceNum; i++) {
			if (m_Assignments[i] == clusterIndex) {
				out.add(i);
			}
		}

		return out;
	}

	/**
	 * Generates the clusterer, including: - Update m_BestMedoids[] - Update
	 * m_BestAssignment[] - Update m_BestClusterSizes[] - Update
	 * m_BestDistanceErrorSum
	 * 
	 * @param data
	 *            the set of instances serving as training data
	 * @throws Exception
	 *             if the clusterer has not been generated successfully
	 */
	public void buildClusterer(Instances data) throws Exception {

		// Check the capabilities of the clusterer
		getCapabilities().testWithFail(data);
		// Copy Data to m_data
		m_data = new Instances(data);
		// Get the number of instances
		m_InstanceNum = m_data.numInstances();
		/*
		 * Initialize all fields that are not being set via options
		 */
		// Initialize the variables for the final result
		m_BestDistanceErrorSum = 0.0; // Double.MAX_VALUE;
		m_BestClusterSizes = null;
		m_BestAssignments = null;
		m_BestMedoids = null;
		// Initialize the variables used in iterations
		m_DistanceErrorSum = 0.0; // Double.MAX_VALUE;
		m_ClusterSizes = new int[m_NumClusters];
		m_Assignments = new int[m_InstanceNum];
		m_Medoids = new int[m_NumClusters];
		m_DistanceErrors = new double[m_NumClusters];
		m_Iterations = 0;
		// Set m_data for m_DistanceFunction
		// This is necessary if you wanna use DistanceFunction implementation in WEKA
		m_DistanceFunction.setInstances(m_data);

		/***************************************
		 **** START TO IMPLEMENT YOUR PAM ****
		 ***************************************/

		/*
		 * The following code is nothing but example replace them with your code.
		 */
		// for(int i=0; i<m_RepeatTimes; i++) {
		// System.out.println("Create cluster No." + i);
		// boolean medoid_changed = false;
		// while (medoid_changed) {
		// System.out.println("Update medoids and compute DistanceErrors");
		// }
		// System.out.println("Keep the best cluster with the minimum
		// DistanceErrorSum");
		// }
		/*
		 * ------------------------------------------------------------------------ PAM
		 * Algorithm Description
		 * ------------------------------------------------------------------------ The
		 * PAM algorithm works as following steps: 1. Initialization Step: randomly
		 * select k data points as the medoids. 2. Assignment Step: associate each data
		 * point to closest medoid. 3. Updating Step: for each cluster (with medoid m)
		 * and for every data point o in this cluster (a) Swap m and o; (b) Compute the
		 * total swapping cost from m to o. If there exists o with negative cost, then
		 * replace m by o with the smallest cost. 4. Repeat steps 2 and 3 until there is
		 * no change in medoids. 5. Repeat steps 1 to 4 for several times and output the
		 * solution with the least squared error.
		 * ------------------------------------------------------------------------
		 */

		// 1. Initialization Step: randomly select k data points as the medoids.
		for (int i = 0; i < m_NumClusters; i++) {
			int r = new Random().nextInt(m_InstanceNum);
			m_Medoids[i] = r;
			// m_BestMedoids[i] = r;
			// System.err.print(m_Medoids[i]+"\t");
		}

		boolean noChange = false;

		while (!noChange) { // || m_Iterations < m_MaxIterations){

			m_Iterations++;

			// 2. Assignment Step: associate each data point to closest medoid.
			for (int i = 0; i < m_InstanceNum; i++) {

				Instance inst = m_data.instance(i);
				int c = clusterProcessedInstance(inst, true);
				m_Assignments[i] = c;

			}

			/*
			 * 3. Updating Step: for each cluster (with medoid m) and for every data point o
			 * in this cluster (a) Swap m and o; (b) Compute the total swapping cost from m
			 * to o. If there exists o with negative cost, then replace m by o with the
			 * smallest cost.
			 */
			boolean check = false;

			for (int i = 0; i < m_NumClusters; i++) {
				if (updateMedoid(i, getClusterMembers(i))) {
					check = true;
				}
			}

			if (check == false) {
				noChange = true;
			}
		}

		// System.err.println("m_Iterations: " + m_Iterations);
		m_BestMedoids = m_Medoids;

		/*
		 * END OF PAM
		 */

		// Save the clustered instances into text file
		// Set the SaveClusterResult to True in the WEKA GUI
		if (m_SaveClusterResult) {
			m_RuntimeInfo += "Save instances with clusters to the file: ";
			m_RuntimeInfo += "PAM_Instance" + Integer.toString(m_InstanceNum) + "_Cluster"
					+ Integer.toString(m_NumClusters) + ".txt\n";
			saveClusters();
		}
	} // End of buildClusterer

	/**
	 * Returns a string describing this clusterer
	 * 
	 * @return a description of the clusterer as a string
	 */
	public String toString() {
		if (m_BestMedoids == null) {
			return "No clusterer built yet!\n";
		}
		StringBuffer buff = new StringBuffer();
		buff.append("\n=== MyPAM Algorithm Output ===\n\n");
		buff.append("Add any string you like!");
		buff.append("But the buff content will not dispaly if m_BestMedoid is null");
		return buff.toString();
	} // End of toString
	/*----------------------------------------------------------------------------
	 * Override Functions
	 *--------------------------------------------------------------------------*/

	@Override
	public int numberOfClusters() throws Exception {
		return m_NumClusters;
	}

	@Override
	public int clusterInstance(Instance instance) throws Exception {
		double minDist = Double.MAX_VALUE;
		int bestCluster = -1;
		for (int i = 0; i < m_NumClusters; i++) {

			double dist = 0.0;

			if (m_distance) {
				dist = m_data.get(m_data.indexOf(instance)).attribute(m_BestMedoids[i]).getLowerNumericBound();
			} else {
				dist = m_DistanceFunction.distance(instance, m_data.instance(m_BestMedoids[i]));
			}

			if (dist < minDist) {
				minDist = dist;
				bestCluster = i;
			}
		}
		return bestCluster;
	}
	/*----------------------------------------------------------------------------
	 * END of the Functions for <Options Setup>
	 *----------------------------------------------------------------------------*/

	/*----------------------------------------------------------------------------
	 * Functions for <Options Setup>
	 * - listOptions: adds option elements
	 * - numClustersTipText, getNumClusters, setNumClusters: for m_NumClusters
	 * - repeatTimesTipText, getRepeatTimes, setRepeatTimes: for m_RepeatTimes
	 * - maxIterationsTipText, getMaxIterations, setMaxIterations: for m_MaxIterations
	 * - showClusterResultTipText, getShowClusterResult, setShowClusterResult: for m_ShowClusterResult
	 * - distanceFunctionTipText, getDistanceFunction, setDistanceFunction: for m_DistanceFunction
	 * - setOptions: Set the values of the options from a list of strings
	 * - getOptions: Get the currently used options as a list of strings
	 *--------------------------------------------------------------------------*/
	/**
	 * Returns an enumeration describing the available options
	 * 
	 * @return an enumeration of all the available options
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enumeration listOptions() {
		Vector result = new Vector();

		result.addElement(new Option("\tnumber of clusters.\n\t(default 3).", "N", 1, "-N <num>"));

		result.add(new Option("\tDistance function to use.\n\t(default: EuclideanDistance)\n", "A", 1,
				"-A <classname and options>"));

		result.add(new Option("\tMaximum number of iterations.\n", "I", 1, "-I <num>"));

		result.add(new Option("\tTotal times of repeating the Initial-Assign-Update steps.\n\t(default 5)", "J", 1,
				"-J <num>"));

		result.addElement(new Option("\tSave the cluster result.\n", "s", 0, "-s"));

		Enumeration en = super.listOptions();
		while (en.hasMoreElements())
			result.addElement(en.nextElement());

		return result.elements();
	}

	/**
	 * Function: numClustersTipText Returns the tip text for m_NumClusters
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String numClustersTipText() {
		return "set number of clusters";
	}

	/**
	 * Returns the number of clusters to generate
	 * 
	 * @return the number of clusters
	 */
	public int getNumClusters() {
		return m_NumClusters;
	}

	/**
	 * Sets the number of clusters to generate
	 * 
	 * @param n
	 *            the number of clusters to generate
	 * @throws Exception
	 *             if the number of clusters is negative or zero
	 */
	public void setNumClusters(int n) throws Exception {
		if (n <= 0) {
			throw new Exception("Number of clusters should be greater than 0");
		}
		m_NumClusters = n;
	}

	/**
	 * Returns the tip text for m_RepeatTimes
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String repeatTimesTipText() {
		return "The total times of repeating the Initial-Assign-Update steps.";
	}

	/**
	 * Returns the total times of repeat the Initial-Assign-Update steps.
	 * 
	 * @return the total times of repeat the Initial-Assign-Update steps.
	 */
	public int getRepeatTimes() {
		return m_RepeatTimes;
	}

	/**
	 * Sets the total times of repeat the Initial-Assign-Update steps.
	 * 
	 * @param r
	 *            the number of repeat times
	 * @throws Exception
	 *             if the number is negative or zero
	 */
	public void setRepeatTimes(int r) throws Exception {
		if (r <= 0) {
			throw new Exception("The number of repeat times should be greater than 0");
		}
		m_RepeatTimes = r;
	}

	/**
	 * Returns the tip text for m_MaxIterations
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String maxIterationsTipText() {
		return "set maximum number of iterations";
	}

	/**
	 * Sets the maximum number of iterations
	 * 
	 * @param n
	 *            the maximum number of iterations
	 * @throws Exception
	 *             if the maximum number is negative or zero
	 */
	public void setMaxIterations(int n) throws Exception {
		if (n <= 0) {
			throw new Exception("The maximum number of iterations must be > 0");
		}
		m_MaxIterations = n;
	}

	/**
	 * Returns the maximum number of iterations
	 * 
	 * @return the maximum number of iterations
	 */
	public int getMaxIterations() {
		return m_MaxIterations;
	}

	/**
	 * Returns the tip text for m_ShowClusterResult
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String showClusterResultTipText() {
		return "Whether save the cluster result to the text file.";
	}

	/**
	 * Sets whether output the cluster result.
	 * 
	 * @param r
	 *            whether output the cluster result
	 */
	public void setSaveClusterResult(boolean r) {
		m_SaveClusterResult = r;
	}

	/**
	 * Gets whether output the cluster result.
	 * 
	 * @return whether output the cluster result
	 */
	public boolean getSaveClusterResult() {
		return m_SaveClusterResult;
	}

	/**
	 * Returns the tip text for m_DistanceFunction.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter GUI
	 */
	public String distanceFunctionTipText() {
		return "The distance function to use for instances comparison " + "(default: weka.core.EuclideanDistance). ";
	}

	/**
	 * Sets the distance function to use for instance comparison.
	 * 
	 * @param df
	 *            the new distance function to use
	 * @throws Exception
	 *             if df is not EuclideanDistance or ManhattanDistance
	 */
	public void setDistanceFunction(DistanceFunction df) throws Exception {
		if ((df instanceof EuclideanDistance) || (df instanceof ManhattanDistance)) {
			m_DistanceFunction = df;
		} else {
			throw new Exception("MyPAM only support the Euclidean or Manhattan distance.");
		}
	}

	/**
	 * Returns the distance function currently in use.
	 * 
	 * @return the distance function currently in use
	 */
	public DistanceFunction getDistanceFunction() {
		return m_DistanceFunction;
	}

	/**
	 * Sets the options
	 * 
	 * @param options
	 *            a list of options as an array of strings
	 * @throws Exception
	 *             if an option is not support
	 */
	public void setOptions(String[] options) throws Exception {

		// Set the number of the cluster
		String optionString = Utils.getOption('N', options);
		if (optionString.length() != 0) {
			setNumClusters(Integer.parseInt(optionString));
		}

		// Set the number of the maximum iterations
		optionString = Utils.getOption("I", options);
		if (optionString.length() != 0) {
			setMaxIterations(Integer.parseInt(optionString));
		}

		// Set the repeat times
		optionString = Utils.getOption("J", options);
		if (optionString.length() != 0) {
			setRepeatTimes(Integer.parseInt(optionString));
		}

		// Set the distance function
		String distFunctionClass = Utils.getOption('A', options);
		if (distFunctionClass.length() != 0) {
			String distFunctionClassSpec[] = Utils.splitOptions(distFunctionClass);
			if (distFunctionClassSpec.length == 0) {
				throw new Exception("Invalid DistanceFunction specification string.");
			}
			String className = distFunctionClassSpec[0];
			distFunctionClassSpec[0] = "";

			setDistanceFunction(
					(DistanceFunction) Utils.forName(DistanceFunction.class, className, distFunctionClassSpec));
		} else {
			setDistanceFunction(new EuclideanDistance());
		}

		// Set whether to output the cluster result
		m_SaveClusterResult = Utils.getFlag("s", options);

		// Other options
		super.setOptions(options);
	}

	/**
	 * Returns the current options as strings
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String[] getOptions() {
		int i;
		Vector result;
		String[] options;

		result = new Vector();

		result.add("-N");
		result.add("" + getNumClusters());

		result.add("-A");
		result.add("" + m_DistanceFunction);

		result.add("-I");
		result.add("" + getMaxIterations());

		result.add("-J");
		result.add("" + getRepeatTimes());

		options = super.getOptions();
		for (i = 0; i < options.length; i++)
			result.add(options[i]);

		return (String[]) result.toArray(new String[result.size()]);
	}

	/*----------------------------------------------------------------------------
	 * END of the Functions for <Options Setup>
	 *----------------------------------------------------------------------------*/

	/**
	 * Returns the number of data points in each cluster
	 * 
	 * @return the number of data points in each cluster as a int array
	 */
	public int[] getClusterSizes() {
		return m_BestClusterSizes;
	}

	public String getRevision() {
		return RevisionUtils.extract("$Revision: 5538 $");
	}

}
