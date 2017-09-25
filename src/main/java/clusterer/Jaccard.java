package clusterer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Jaccard {
	
	/**
	 * Calculate the Jaccard similarity between the two clusters
	 * @param m1
	 * @param m2
	 * @param b 
	 * @return
	 * @throws Exception 
	 */
	public double[] calculateJaccard(LinkedHashMap<Integer, LinkedList<String>> m1, LinkedHashMap<Integer, LinkedList<String>>m2, boolean b) throws Exception{
		
		double[][] distanceMatrix;
		
		int rows = m1.keySet().size();
		int columns = m2.keySet().size();
		
		distanceMatrix = new double[rows][columns];
		double[] maxRows = new double[rows];
		double[] maxColumns = new double[columns];
		
		Integer[] rowsArray = m1.keySet().toArray(new Integer[m1.size()]);
		Integer[] columnsArray = m2.keySet().toArray(new Integer[m2.size()]);
		
		for (int r = 0; r < rowsArray.length; r++) {
			
			for (int c = 0; c < columnsArray.length; c++) {
				
				LinkedList<String> l1 = m1.get(r);
				LinkedList<String> l2 = m2.get(c);
				
				if(l1 == null || l2 == null){
					System.err.print("");
				}
				
				distanceMatrix[r][c] = jaccard(l1, l2);
				
				if(distanceMatrix[r][c] > maxRows[r]){
					maxRows[r] = distanceMatrix[r][c];
				}
			
				if(distanceMatrix[r][c] > maxColumns[c]){
					maxColumns[c] = distanceMatrix[r][c];
				}
				
			}
			
		}
		
		if(b){
			printDistanceMatrix(distanceMatrix, rows, columns, maxRows, maxColumns);
		}
		
		double total = 0.0;
		
		for (double i : maxRows)
		    total += i;
	
		double[] avg = new double[3];
		
		avg[0] = total / rows;
		
		total = 0.0;
		
		for (double i : maxColumns)
		    total += i;
		
		avg[1] = total / columns;
		
		avg[2] = (avg[0] + avg[1]) / 2;
	
		return avg;
		
	}

	/**
	 * Print the distance matrix
	 * @param dm
	 * @param columns 
	 * @param rows 
	 * @param maxRows 
	 * @param maxColumns 
	 */
	private void printDistanceMatrix(double[][] dm, int rows, int columns, double[] maxRows, double[] maxColumns) {
		
		System.out.println("\tJACCARD SIMILARITY MATRIX");
		
		for (int c = 0; c < columns; c++) {
			System.out.print("\t|" + c + "|");
		}
		
		System.out.print("\n");
		
		for (int c = 0; c < columns; c++) {
			System.out.print("-----------");
		}
		
		System.out.print("\n");
		
		for (int i = 0; i < rows; i++) {
			
			System.out.print("|" + i + "|\t");
			
			for (int j = 0; j < columns; j++) {
				System.out.print(String.format("%03.2f\t", dm[i][j]));
			}
			System.out.print(String.format("MAX: %03.2f", maxRows[i]));
			//System.out.println("MAX: " + Arrays.toString(sumVector));
			System.out.print("\n");
		}
		
		for (int c = 0; c < columns; c++) {
			System.out.print("-----------");
		}
		
		System.out.print("\n");	
		
		System.out.print("MAX:\t");
		
		for (int i = 0; i < columns; i++) {	
			System.out.print(String.format("%03.2f\t", maxColumns[i]));
		}
		
		System.out.print("\n");
		
		for (int c = 0; c < columns; c++) {
			System.out.print("-----------");
		}
		
		System.out.print("\n");		
	}

	/**
	 * Calculate Jaccard similarity between two instances
	 * @param l1
	 * @param l2
	 * @return
	 */
	private double jaccard(LinkedList<String> l1,
			LinkedList<String> l2) {
		
		if(l1 == null || l2 == null){
			try {
				throw new Exception("Lists cannot be empty");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		double intersection = intersectionCardinality(l1, l2);
		double union = unionCardinality(l1, l2);
		
		double result = intersection / union;
		
		return result;
	}
	
	/**
	 * Calculate union between two instances
	 * @param l1
	 * @param l2
	 * @return
	 */
	private double unionCardinality(LinkedList<String> l1,
			LinkedList<String> l2){
		
		double union = Math.max(l1.size(), l2.size());
		
		return union;
		
	}
	
	/**
	 * Calculate intersection between two instances
	 * @param l1
	 * @param l2
	 * @return
	 */
	private double intersectionCardinality(LinkedList<String> l1,
			LinkedList<String> l2){
			
		List<String> l3 = new ArrayList<String>(l2);
		l3.retainAll(l1);
		
		double commons = l3.size();
		
		return commons;
		
	}
	
	/*
	private static Map<Integer, LinkedList<String>> printMap(Map<Integer, LinkedList<String>> map) throws InterruptedException {
		System.out.println("CLUSTERS");
		for (Integer cluster : map.keySet()) {
			System.out.println("\t"+ cluster + "\t" + map.get(cluster));			
		}
		System.out.println();
		return map;
	}
	*/
	
	public static LinkedHashMap<Integer, LinkedList<String>> cleanMap(LinkedHashMap<Integer, LinkedList<String>> map){
		
		LinkedHashMap<Integer, LinkedList<String>> output = new LinkedHashMap<Integer, LinkedList<String>>();
		
		int i = 0;
		for (Integer c : map.keySet()) {
			if(!map.get(c).isEmpty()){
				output.put(i, map.get(c));
				i++;
			}
		}
		
		return output;
	}

}
