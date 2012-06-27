/**
 * Author Marios Kokkodis
 * Last update 01/17/2012
 * -------------------------------------------------------
 * Categories ategories:
 * 
 * Technical Level: (m=3+1), '1 - web-development', '2 - Software-development', '3 - design and multimedia'
 * Non - Technical Level: (m=3+1), '1 - writing', '2 - administrative', '3 - Sales & marketing'
 * Generic: (m=2+1), '1 - technical' , '2 - non tech'
 * 
 * --------------------------------------------------------
 *  

 * 
 * Binomial model with history and score (helpfulness) thresholds. 
 * Point Estimate (PE) and  Random Sampling  (RS) aproaches.
 * 
 * Training stream. Outputs tuples ready for regressions
 * Output dir: ~/outFiles/training/$scoreTh/$model_$approach_$level 
 * 
 */

package kokkodis.odesk;

import java.io.File;

import kokkodis.db.Queries;
import kokkodis.db.MySQLoDeskQueries;
import kokkodis.utils.PrintToFile;

public class ODeskTrain {

	/*
	 * Global Variables for Train, Regressions and Test
	 */
	public static double K = 5; // number of buckets!

	public static int mPlus1; // m+1
	public static float scoreTh;
	public static int historyThr = 5;
	public static String trainingOutPath = "C:\\Users\\mkokkodi\\Desktop\\bigFiles\\kdd\\odesk\\training\\";
	public static String[] qApproach = { 
		"PE",
		"RS" };
	public static String[] hierarchyLevel = { "Technical"// };
			, "Non-technical", "Generic" };
	public static String[] models = {
	// "Binomial",
	"Multinomial"
	 };
	public static float[] scoreThresholds = { 0.6f, 0.7f, 0.8f, 0.9f };

	// public static String dataPath =
	// "C:\\Users\\mkokkodi\\workspace\\odeskExperiment\\data\\";

	private static MySQLoDeskQueries q;

	/*
	 * Some tmp additions.
	 */
	public static PrintToFile outputFile;

	public static void main(String[] args) {

		
		System.out.println("Starting...");
		for (String model : models) {
			for (String approach : qApproach) {
				if(model.equals("Binomial")){
				for (float tmpTh : scoreThresholds) {
					scoreTh = tmpTh;

					for (String level : hierarchyLevel) {
						runLevel(level, approach, model);

					}
				}
				}else
					for (String level : hierarchyLevel) {
					runLevel(level, approach, model);

				}
					
			}
		}
		System.out.println("Completed.");

	}

	private static void runLevel(String level, String approach, String model) {

		initialize();

		
		if(model.equals("Binomial"))
		{print(trainingOutPath + scoreTh + "\\" + model + "_" + approach + "_"
				+ level + ".csv");
		outputFile.openFile(new File(trainingOutPath + scoreTh + "\\" + model
				+ "_" + approach + "_" + level + ".csv"));
		}else{
			print(trainingOutPath +  model + "_" + approach + "_"
					+ level + ".csv");
			outputFile.openFile(new File(trainingOutPath +  model
					+ "_" + approach + "_" + level + ".csv"));
		}

		if (level.equals("Technical")) {

			outputFile
					.writeToFile("id,logit(q), logit(web-development), logit(Software-development), "
							+ "logit(design and multimedia),cat, logit(q_cat(t+1))");
			mPlus1 = 4;

		} else if (level.equals("Non-technical")) {
			outputFile
					.writeToFile("id,logit(q),logit(writing), logit(administrative),"
							+ "logit(Sales and Marketing),cat, logit(q_cat(t+1))");
			mPlus1 = 4;

		} else {
			mPlus1 = 3;
			outputFile.writeToFile("id,logit(q),logit(tech), logit(nontech),"
					+ "cat, logit(q_cat(t+1))");
		}
		q.rawDataToBinomialModel(level, approach, model, false);
		outputFile.closeFile();
	}

	private static void initialize() {
		System.out.println("Initializing...");
		q = new MySQLoDeskQueries();
		q.connect();
		outputFile = new PrintToFile();
	}

	public static void print(String str) {
		System.out.println(str);
	}
}
