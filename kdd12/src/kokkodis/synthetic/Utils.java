package kokkodis.synthetic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import kokkodis.factory.EvalWorker;
import kokkodis.factory.ModelCategory;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.odesk.TestUtils;
import kokkodis.utils.odesk.TrainUtils;

public class Utils {

	public Utils() {
		// TODO Auto-generated constructor stub
	}

	public void rawDataToBinomialModel(String approach, String model,
			int categories, boolean test) {

		if (!test) {
			System.out.println("Reading file.");
			System.out.println("Approch:" + approach);
		}
		HashMap<Integer, HashMap<Integer, ModelCategory>> dataMapHolder = null;

		HashMap<Integer, EvalWorker> dataMapHolderEval = null;

		if (!test) {

			dataMapHolder = new HashMap<Integer, HashMap<Integer, ModelCategory>>();

			manipulateDataTrain(approach, model, dataMapHolder);

		} else {
			dataMapHolderEval = new HashMap<Integer, EvalWorker>();
			manipulateData(approach, model, dataMapHolderEval);
		}

	}

	/**
	 * This is for evaluating
	 * 
	 * @param approach
	 * @param model
	 * @param dataMapHolderEval
	 */

	protected void manipulateData(String approach, String model,
			HashMap<Integer, EvalWorker> dataMapHolderEval) {

		// TestUtils tu = new TestUtils();
		// if (model.equals("Binomial")) {
		// while (rs.next()) {
		// Integer catId = cats.get(rs.getInt("jobCategory"));
		//
		// if (catId != null) {
		// int developerId = rs.getInt("developer");
		//
		// double score = rs.getDouble("score");
		// double actualTaskScore = (score / 5.0);
		//
		// boolean succesfullOutcome = ((actualTaskScore > ODeskTrain.scoreTh))
		// ? true
		// : false;
		//
		// EvalWorker tmp = dataMapHolderEval.get(developerId);
		// String currentTask = (tu.getGenericCat(catId) == 1) ? "Technical"
		// : "Non-technical";
		// String workerType = null;
		// if (tmp == null) {
		// tmp = new EvalWorker();
		// workerType = currentTask;
		// } else {
		//
		// workerType = tmp.getWorkerType();
		// }
		// catId = tu.adjustODeskCategory(workerType, catId);
		// tu.updateEvalWorker(dataMapHolderEval, developerId, catId,
		// succesfullOutcome, actualTaskScore, approach,
		// workerType, currentTask, model);
		// }
		// }
		// } else if (model.equals("Multinomial")) {
		// while (rs.next()) {
		// Integer catId = cats.get(rs.getInt("jobCategory"));
		//
		// if (catId != null) {
		//
		// int developerId = rs.getInt("developer");
		//
		// double score = rs.getDouble("score");
		// double actualTaskScore = (score / 5.0);
		// int bucket = tu.getBucket(actualTaskScore);
		//
		// EvalWorker tmp = dataMapHolderEval.get(developerId);
		// String currentTask = (tu.getGenericCat(catId) == 1) ? "Technical"
		// : "Non-technical";
		// String workerType = null;
		// if (tmp == null) {
		// tmp = new EvalWorker();
		// workerType = currentTask;
		// } else {
		//
		// workerType = tmp.getWorkerType();
		// }
		// catId = tu.adjustODeskCategory(workerType, catId);
		//
		// tu.updateEvalWorker(dataMapHolderEval, developerId, catId,
		// bucket, actualTaskScore, approach, workerType,
		// currentTask, model);
		// }
		//
		// }
		//
		// }
	}

	/**
	 * This is for training!
	 * 
	 * @param approach
	 * @param model
	 * @param dataMapHolder
	 * @throws SQLException
	 */

	protected void manipulateDataTrain(String approach, String model,
			HashMap<Integer, HashMap<Integer, ModelCategory>> dataMapHolder) {

		TrainUtils tu = new TrainUtils();
		try {

			BufferedReader input = new BufferedReader(new FileReader(new File(
					"/Users/mkokkodi/Desktop/bigFiles/synthetic_data/train"+SyntheticTrain.categories+".csv")));
			String line;

			while ((line = input.readLine()).contains("#"))
				;
			if (model.equals("Binomial")) {

				while ((line = input.readLine()) != null) {
					String[] tmpAr = line.split(",");

					int catId = Integer.parseInt(tmpAr[2].trim());

					int developerId = Integer.parseInt(tmpAr[0].trim());

					double actualTaskScore = Double
							.parseDouble(tmpAr[3].trim());

					boolean succesfullOutcome = ((actualTaskScore > ODeskTrain.scoreTh)) ? true
							: false;

					tu.updateWorkerHistoryAndPrintTuple(dataMapHolder,
							developerId, catId, succesfullOutcome,
							actualTaskScore, approach, model);
				}
			} else {
				System.out.println("Multinomial!");

				while ((line = input.readLine()) != null) {

					String[] tmpAr = line.split(",");

					int catId = Integer.parseInt(tmpAr[2].trim());

					int developerId = Integer.parseInt(tmpAr[0].trim());

					double actualTaskScore = Double
							.parseDouble(tmpAr[3].trim());

					int bucket = tu.getBucket(actualTaskScore);

					tu.updateWorkerHistoryAndPrintTuple(dataMapHolder,
							developerId, catId, bucket, actualTaskScore,
							approach, model);
				}
			}

		} catch (IOException e) {
		}
	}

}
