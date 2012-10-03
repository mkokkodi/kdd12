package kokkodis.odesk.cv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.odesk.ODeskTest;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.PrintToFile;

public class AverageResults {

	/**
	 * @param args
	 */
	private static String resultPath = "/home/mkokkodi/workspace/git/kdd12/cv_data/results/";

	public static void main(String[] args) {

		PrintToFile resultsFile = new PrintToFile();
		resultsFile.openFile(new File(resultPath+"cv_results.csv"));
		resultsFile.writeToFile("Model,Approach,Score,History,MAEModel, MAEBasline");
		for (String model : ODeskTrain.models) {
			for (String approach : ODeskTrain.qApproach) {

				/**
				 * score -> History -> Metric
				 */
				System.out.println("---------------------------------------------------------");
				System.out.println("| Model:"+model+" - Approach:"+approach);
				
				System.out.println("---------------------------------------------------------");
				
				HashMap<Double, HashMap<Integer, HashMap<String, ArrayList<Double>>>> data = new HashMap<Double, HashMap<Integer, HashMap<String, ArrayList<Double>>>>();
				for (int fold = 1; fold < 11; fold++) {
					String inFile = resultPath + model + "/" + approach + fold
							+ ".csv";

					try {
						BufferedReader input = new BufferedReader(
								new FileReader(inFile));
						String line;
						line = input.readLine();

						// Score-Threshold,History-Threshold,MAE-Binomial,
						// MAE-Baseline,MSE-Binomial,MSE-Baseline
						while ((line = input.readLine()) != null) {

							String[] tmpAr = line.split(",");

							double score = Double.parseDouble(tmpAr[0].trim());
							int history = Integer.parseInt(tmpAr[1].trim());
							double maeModel = Double.parseDouble(tmpAr[2]
									.trim());
							double maeBaseline = Double.parseDouble(tmpAr[3]
									.trim());

							HashMap<Integer, HashMap<String, ArrayList<Double>>> curScoreMap = data
									.get(score);
							if (curScoreMap == null) {
								curScoreMap = new HashMap<Integer, HashMap<String, ArrayList<Double>>>();
								data.put(score, curScoreMap);
							}
							HashMap<String, ArrayList<Double>> curHistoryMap = curScoreMap
									.get(history);

							if (curHistoryMap == null) {
								curHistoryMap = new HashMap<String, ArrayList<Double>>();
								curScoreMap.put(history, curHistoryMap);
							}

							ArrayList<Double> modelMAE = curHistoryMap
									.get("maeModel");
							if (modelMAE == null) {
								modelMAE = new ArrayList<Double>();
								curHistoryMap.put("maeModel", modelMAE);
							}
							modelMAE.add(maeModel);

							ArrayList<Double> baselineMAE = curHistoryMap
									.get("baselineModel");
							if (baselineMAE == null) {
								baselineMAE = new ArrayList<Double>();
								curHistoryMap.put("baselineModel", baselineMAE);
							}
							baselineMAE.add(maeBaseline);

						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				System.out.println("Score,History,AvgMAEModel, AvgMAEBasline");
				for(Entry<Double,HashMap<Integer,HashMap<String,ArrayList<Double>>>> e1: data.entrySet()){
					
					for(Entry<Integer,HashMap<String,ArrayList<Double>>> e2: e1.getValue().entrySet()){
						String line=model+","+approach+","+e1.getKey()+","+e2.getKey()+",";
						System.out.print(e1.getKey()+","+e2.getKey()+",");
						for(Entry<String,ArrayList<Double>> e3: e2.getValue().entrySet()){
							double sum = 0;
							for(double d: e3.getValue())
								sum+=d;
							
							line+=sum/e3.getValue().size()+",";
							System.out.print(sum/e3.getValue().size()+",");
						}
						resultsFile.writeToFile(line.substring(0, line.length()-1));
						System.out.println();
					}
				}
			}
		}
		resultsFile.closeFile();
	}
}
