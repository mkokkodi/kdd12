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

package kokkodis.synthetic;

import java.io.File;

import kokkodis.db.Queries;
import kokkodis.db.MySQLoDeskQueries;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.PrintToFile;

public class SyntheticTrain {

	/*
	 * Global Variables for Train, Regressions and Test
	 */
	public static double K = 5; // number of buckets for multinomial!

	public static int mPlus1; // m+1
	public static float scoreTh;
	public static int historyThr = 5;
	public static String trainingOutPath = "/Users/mkokkodi/Desktop/bigFiles/synthetic_logits/";
	public static String[] qApproach = { "PE", "RS" };
	public static String[] models = { "Binomial",
	// "Multinomial"
	};

	public static int categories;
	public static float[] scoreThresholds = { 0.6f, 0.7f, 0.8f, 0.9f };

	/*
	 * Some tmp additions.
	 */
	public static PrintToFile outputFile;

	public static void main(String[] args) {

		System.out.println("Starting...");
		categories = 5;
		for (String model : models) {
			for (String approach : qApproach) {
				if (model.equals("Binomial")) {
					for (float tmpTh : scoreThresholds) {
						scoreTh = tmpTh;

						runLevel(approach, model, categories);

					}
				} else
					runLevel(approach, model, categories);

			}
		}
		print("Completed.");

	}

	private static void runLevel(String approach, String model, int categories) {

		initialize();

		if (model.equals("Binomial")) {
			print(trainingOutPath + model + "_" + approach + "_" + scoreTh
					+ ".csv");
			outputFile.openFile(new File(trainingOutPath + model + "_"
					+ approach + "_" + scoreTh + ".csv"));
		} else {
			print(trainingOutPath + model + "_" + approach + ".csv");
			outputFile.openFile(new File(trainingOutPath + model + "_"
					+ approach + ".csv"));
		}

		String str = "id,logit(q)";
		for (int j = 0; j < categories; j++)
			str += ",logit(cat" + j + ")";

		str += ",cat, logit(q_cat(t+1))";
		outputFile.writeToFile(str);
		mPlus1 = categories + 1;

		ODeskTrain.mPlus1 = mPlus1;
		ODeskTrain.historyThr = historyThr;
		ODeskTrain.K = K;
		ODeskTrain.models = models;
		ODeskTrain.outputFile = outputFile;
		ODeskTrain.scoreTh = scoreTh;
		ODeskTrain.scoreThresholds = scoreThresholds;
		ODeskTrain.trainingOutPath = trainingOutPath;

		Utils u = new Utils();
		u.rawDataToBinomialModel(approach, model, categories, false);
		outputFile.closeFile();
	}

	private static void initialize() {
		System.out.println("Initializing...");
		outputFile = new PrintToFile();
	}

	public static void print(String str) {
		System.out.println(str);
	}
}