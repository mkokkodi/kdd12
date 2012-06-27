/**
 * Author Marios Kokkodis
 * Last update 01/17/2012
 * 
 * Rutines for creating clean regression files.
 * Output files at bigFiles/odesk/regressions...
 * 
 */

package kokkodis.odesk;

import kokkodis.utils.odesk.RegressionUtils;

public class ODeskRegressions {

	public static String basedOn;

	public static String regressionOuputPath = "C:\\Users\\mkokkodi\\Desktop\\bigFiles\\kdd\\odesk\\regressions\\";
	private static RegressionUtils ru;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ODeskTrain.print("Starting...");
		ru = new RegressionUtils();

		System.out.println("Starting...");
		for (String model : ODeskTrain.models) {

			for (String approach : ODeskTrain.qApproach) {

				for (String level : ODeskTrain.hierarchyLevel) {
					if (model.equals("Binomial")) {
						for (float scoreTh : ODeskTrain.scoreThresholds) {
							ODeskTrain.scoreTh = scoreTh;
							String inFile = ODeskTrain.scoreTh + "\\" + model
									+ "_" + approach + "_" + level;
							ru.createRegressionFiles(inFile, level);
							String tmpPath = regressionOuputPath + inFile + "_";
							ru.getCoeffs(tmpPath, true);
						}
					} else {
						String inFile =  model + "_"
								+ approach + "_" + level;
						ru.createRegressionFiles(inFile, level);
						String tmpPath = regressionOuputPath + inFile + "_";
						ru.getCoeffs(tmpPath, true);
					}
				}

			}
		}

		ODeskTrain.print("Completed");

	}

}
