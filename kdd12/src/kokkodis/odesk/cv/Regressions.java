package kokkodis.odesk.cv;

import kokkodis.odesk.ODeskRegressions;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.odesk.RegressionUtils;

public class Regressions {

	/**
	 * @param args
	 */
	private static RegressionUtils ru;
	public static String regressionsCVOutPath = "/Users/mkokkodi/git/kdd12/cv_data/regressions/";

	public static void main(String[] args) {
		ODeskTrain.print("Starting...");
		ODeskRegressions.regressionOuputPath = regressionsCVOutPath;
		ru = new RegressionUtils();
		ODeskTrain.trainingOutPath = Train.cvDataPath;

		System.out.println("Starting...");

		for (int i = 1; i <= 10; i++)
			runRegressions(i);

		ODeskTrain.print("Completed");

	}

	private static void runRegressions(int i) {
		for (String model : ODeskTrain.models) {

			for (String approach : ODeskTrain.qApproach) {

				for (String level : ODeskTrain.hierarchyLevel) {
					if (model.equals("Binomial")) {
						for (float scoreTh : ODeskTrain.scoreThresholds) {
							ODeskTrain.scoreTh = scoreTh;
							String inFile = ODeskTrain.scoreTh + "/" + model
									+ "_" + approach + "_" + level+i;
							ru.createRegressionFiles(inFile, level);
							String tmpPath = ODeskRegressions.regressionOuputPath
									+ inFile + "_";
							ru.getCoeffs(tmpPath, true);
						}
					} else {
						String inFile = model + "_" + approach + "_" + level+i;
						ru.createRegressionFiles(inFile, level);
						String tmpPath = ODeskRegressions.regressionOuputPath
								+ inFile + "_";
						ru.getCoeffs(tmpPath, true);
					}
				}

			}

		}
	}

}
