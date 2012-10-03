package kokkodis.odesk.cv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import kokkodis.db.MySQLoDeskQueries;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.PrintToFile;

public class Train {

	/**
	 * @param args
	 */

	public static HashMap<Integer, Integer> developerToSet=null;
	/*
	 * Some tmp additions.
	 */
	public static String cvDataPath = "/home/mkokkodi/workspace/git/kdd12/cv_data/";
	public static HashMap<Integer, PrintToFile> outputFiles;

	public static void main(String[] args) {

		System.out.println("Starting...");

		for (String model : ODeskTrain.models) {
			for (String approach : ODeskTrain.qApproach) {
				if (model.equals("Binomial")) {
					for (float tmpTh : ODeskTrain.scoreThresholds) {
						ODeskTrain.scoreTh = tmpTh;

						for (String level : ODeskTrain.hierarchyLevel) {
							runLevel(level, approach, model);

						}
					}
				} else
					for (String level : ODeskTrain.hierarchyLevel) {
						runLevel(level, approach, model);

					}

			}
		}
		System.out.println("Completed.");

	}

	private static void runLevel(String level, String approach, String model) {

		ODeskTrain.initialize();
		loadSets();

		createOutputFiles(model, approach, level);
		if (level.equals("Technical"))
			ODeskTrain.mPlus1 = 4;
		else if (level.equals("Non-technical"))
			ODeskTrain.mPlus1 = 4;
		else
			ODeskTrain.mPlus1 = 3;
		ODeskTrain.q.rawDataToBinomialModelCV(level, approach, model, false);
		
		for (int i = 1; i <= 10; i++) {
			outputFiles.get(i).closeFile();
		}
		
	}

	private static void createOutputFiles(String model, String approach,
			String level) {
		ODeskTrain.print(cvDataPath + ODeskTrain.scoreTh + "/" + model + "_"
				+ approach + "_" + level + ".csv");

		outputFiles = new HashMap<Integer, PrintToFile>();
		for (int i = 1; i <= 10; i++) {
			PrintToFile pf = new PrintToFile();
			pf.openFile(new File(cvDataPath + ((ODeskTrain.scoreTh!=0.0)?ODeskTrain.scoreTh + "/" :"")+ model
					+ "_" + approach + "_" + level + i + ".csv"));
			if (level.equals("Technical")) {

				pf.writeToFile("id,logit(q), logit(web-development), logit(Software-development), "
						+ "logit(design and multimedia),cat, logit(q_cat(t+1))");

			} else if (level.equals("Non-technical")) {
				pf.writeToFile("id,logit(q),logit(writing), logit(administrative),"
						+ "logit(Sales and Marketing),cat, logit(q_cat(t+1))");

			} else {

				pf.writeToFile("id,logit(q),logit(tech), logit(nontech),"
						+ "cat, logit(q_cat(t+1))");
			}

			outputFiles.put(i, pf);
		}

	}

	public static void loadSets() {
		if (developerToSet == null) {

			developerToSet = new HashMap<Integer, Integer>();
			for (int i = 1; i < 11; i++) {
				mapDevelopers(i);
			}
		}

	}

	private static void mapDevelopers(int i) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(cvDataPath
					+ "developers/set" + i));
			String line;

			while ((line = input.readLine()) != null) {
				developerToSet.put(Integer.parseInt(line.trim()), i);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
