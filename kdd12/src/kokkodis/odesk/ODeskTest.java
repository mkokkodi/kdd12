package kokkodis.odesk;

import java.io.File;
import java.util.HashMap;

import kokkodis.db.Queries;
import kokkodis.db.MySQLoDeskQueries;
import kokkodis.factory.ErrorHolder;
import kokkodis.utils.PrintToFile;
import kokkodis.utils.odesk.RegressionUtils;

public class ODeskTest {

	public static ErrorHolder errorHolder;
	private static MySQLoDeskQueries q;
	public static String evalPath;
	public static int historyThreshold;
	public static HashMap<String, HashMap<String, Double[]>> allModelCoeffs;

	private static String resultPath = "C:\\Users\\mkokkodi\\"
			+ "Documents\\My Dropbox\\workspace\\java\\kdd12\\results\\odesk\\";
	// private static PrintToFile resultsTxt = new PrintToFile();
	private static PrintToFile allResultsCsv = new PrintToFile();
	private static HashMap<String, PrintToFile> coeffResults = new HashMap<String, PrintToFile>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ODeskTrain.print("Starting...");
		for (String model : ODeskTrain.models) {
			for (String approach : ODeskTrain.qApproach) {
				allResultsCsv.openFile(new File(resultPath + model + "\\"
						+ approach + ".csv"));
				allResultsCsv
						.writeToFile("Score-Threshold,History-Threshold,MAE-"
								+ model + ",MAE-Baseline" + ",MSE-" + model
								+ ",MSE-Baseline");
				if (model.equals("Binomial")) {
					for (float scoreTh : ODeskTrain.scoreThresholds) {
						ODeskTrain.scoreTh = scoreTh;

						// resultsTxt.openFile(new File(resultPath +
						// Train.scoreTh
						// + "\\" + model + "_" + approach + ".csv"));

						allModelCoeffs = getAllModelCoeffs(model, approach);

						ODeskTrain.print("Running evaluation for model:"
								+ model + ", approach:" + approach);

						runEvaluation(model, approach);
						// resultsTxt.closeFile();
					}
				} else {
					allModelCoeffs = getAllModelCoeffs(model, approach);

					ODeskTrain.print("Running evaluation for model:" + model
							+ ", approach:" + approach);

					runEvaluation(model, approach);

				}
				allResultsCsv.closeFile();
			}
		}

		for (PrintToFile pf : coeffResults.values())
			pf.closeFile();
		ODeskTrain.print("Completed");
	}

	private static HashMap<String, HashMap<String, Double[]>> getAllModelCoeffs(
			String model, String approach) {

		HashMap<String, HashMap<String, Double[]>> allcoeffs = new HashMap<String, HashMap<String, Double[]>>();
		RegressionUtils ru = new RegressionUtils();

		for (String level : ODeskTrain.hierarchyLevel) {
			/**
			 * Code for printing into file coeffs
			 */
			PrintToFile pf = coeffResults.get(model+level);
			if (pf == null) {
				pf = new PrintToFile();
				coeffResults.put(model+level, pf);
				pf.openFile(new File(resultPath + model+"//"+ level + "Coeff.csv"));
				if (level.equals("Technical") || level.equals("Non-technical"))
					pf.writeToFile("VoteThreshold,Approach,a11,a12,a13,b1,a21,a22,a23,b2,a31,a32,a33,b3");
				else
					pf.writeToFile("VoteThreshold,Approach,a11,a12,b1,a21,a22,b2");

			}
			/**
			 * End
			 */
			String inFile;
			if (model.equals("Binomial"))
				inFile = ODeskTrain.scoreTh + "\\" + model + "_" + approach
						+ "_" + level;
			else
				inFile = model + "_" + approach + "_" + level;

			if (level.equals("Technical") || level.equals("Non-technical")) {
				ODeskRegressions.basedOn = "_BasedOn_0_1_2_3";
				ODeskTrain.mPlus1 = 4;
			} else {
				ODeskRegressions.basedOn = "_BasedOn_0_1_2";
				ODeskTrain.mPlus1 = 3;
			}
			String regressionFileToUse = ODeskRegressions.regressionOuputPath
					+ inFile + "_";
			HashMap<String, Double[]> tmpCoeff = ru.getCoeffs(
					regressionFileToUse, true);

			// ru.printCoeffs(tmpCoeff, null);
			allcoeffs.put(level, tmpCoeff);

			/**
			 * Again for printing Coeff
			 */
			String str = ODeskTrain.scoreTh + "," + approach + ",";
			for (int l = 1; l < ODeskTrain.mPlus1; l++) {

				Double[] tmp = tmpCoeff.get(l + ODeskRegressions.basedOn);
				for (int i = 0; i < ODeskTrain.mPlus1; i++) {
					str += tmp[i] + ",";
				}

			}
			pf.writeToFile(str.substring(0, str.length() - 1));
			/**
			 * End
			 */
		}
		return allcoeffs;

	}

	private static void runEvaluation(String model, String approach) {

		q = new MySQLoDeskQueries();
		q.connect();

		// resultsTxt
		// .writeToFile("History-Threshold,MAE-Binomial,MAE-Baseline, MSE-Binomial, MSE-Baseline");
		System.out.println("History-Threshold,MAE-" + model
				+ ",MAE-Baseline, MSE-" + model + ", MSE-Baseline");

		for (int i = 5; i <= 15; i += 2) {

			errorHolder = new ErrorHolder();

			historyThreshold = i;
			q.rawDataToBinomialModel("hierarchicalModel", approach, model, true);

			double maeBaseline = errorHolder.getBaselineMAESum()
					/ errorHolder.getTotalEvaluations();
			double maeBinomialModel = errorHolder.getBinomialModelMAESum()
					/ errorHolder.getTotalEvaluations();

			double mseBinomialModel = errorHolder.getBinomialModelMSESum()
					/ errorHolder.getTotalEvaluations();

			double mseBaseline = errorHolder.getBaselineMSESum()
					/ errorHolder.getTotalEvaluations();

			String resStr = historyThreshold + "," + maeBinomialModel + ","
					+ maeBaseline + "," + mseBinomialModel + "," + mseBaseline;
			System.out.println(resStr);
			// resultsTxt.writeToFile(resStr);
			allResultsCsv.writeToFile(ODeskTrain.scoreTh + "," + resStr);

		}

	}

}
