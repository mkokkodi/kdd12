package kokkodis.utils.odesk;

import java.util.HashMap;

import kokkodis.factory.BinCategory;
import kokkodis.factory.EvalWorker;
import kokkodis.factory.ModelCategory;
import kokkodis.factory.MultCategory;
import kokkodis.odesk.ODeskRegressions;
import kokkodis.odesk.ODeskTest;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.Utils;

public class TestUtils extends Utils {

	public TestUtils() {
	}

	public void updateEvalWorker(
			HashMap<Integer, EvalWorker> dataMapHolderEval, int developerId,
			Integer catId, boolean successfulOutcome, double score,
			String approach, String workerType, String currentTask, String model) {

		EvalWorker evalWorker = dataMapHolderEval.get(developerId);
		HashMap<Integer, ModelCategory> genericHistoryMapHolder;
		HashMap<Integer, ModelCategory> techHistoryMapHolder;
		HashMap<Integer, ModelCategory> nonTechHistoryMapHolder;

		double currentReviews = 0;
		BinCategory specializedOveralCategory = null;
		BinCategory genericOveralCategory = null;

		BinCategory specializedCurTaskCat = null;
		BinCategory genericCurTaskCat = null;

		/*
		 * Creates the necessary objects in order to add history in the end of
		 * the procedure The update follows the conditions!!
		 */
		if (evalWorker == null) {

			evalWorker = new EvalWorker();
			genericHistoryMapHolder = evalWorker.getGenericHistoryMap();
			techHistoryMapHolder = evalWorker.getTechnicalHistoryMap();
			nonTechHistoryMapHolder = evalWorker.getNonTechHistoryMap();

			specializedCurTaskCat = new BinCategory();
			genericCurTaskCat = new BinCategory();
			specializedOveralCategory = new BinCategory();
			genericOveralCategory = new BinCategory();

			genericHistoryMapHolder.put(0, genericOveralCategory);

			if (currentTask.equals("Technical")) {
				techHistoryMapHolder.put(catId, specializedCurTaskCat);
				techHistoryMapHolder.put(0, specializedOveralCategory);
				genericHistoryMapHolder.put(1, genericCurTaskCat);
			} else {
				nonTechHistoryMapHolder.put(0, specializedOveralCategory);
				nonTechHistoryMapHolder.put(catId, specializedCurTaskCat);
				genericHistoryMapHolder.put(2, genericCurTaskCat);
			}

			dataMapHolderEval.put(developerId, evalWorker);

		} else {

			genericHistoryMapHolder = evalWorker.getGenericHistoryMap();
			techHistoryMapHolder = evalWorker.getTechnicalHistoryMap();
			nonTechHistoryMapHolder = evalWorker.getNonTechHistoryMap();

			if (currentTask.equals("Technical")) {
				specializedCurTaskCat = (BinCategory) techHistoryMapHolder
						.get(catId);
				genericCurTaskCat = (BinCategory) genericHistoryMapHolder
						.get(1);
				specializedOveralCategory = (BinCategory) techHistoryMapHolder
						.get(0);
			} else {
				specializedCurTaskCat = (BinCategory) nonTechHistoryMapHolder
						.get(catId);
				genericCurTaskCat = (BinCategory) genericHistoryMapHolder
						.get(2);
				specializedOveralCategory = (BinCategory) nonTechHistoryMapHolder
						.get(0);
			}
			genericOveralCategory = (BinCategory) genericHistoryMapHolder
					.get(0);

			currentReviews = genericOveralCategory.getN();
			if (currentReviews > ODeskTest.historyThreshold) {

				if (approach.equals("RS")) {
					for (int j = 0; j < 30; j++) {
						updateErrors(score, evalWorker, approach, catId,
								workerType, currentTask, model);

					}
				} else
					updateErrors(score, evalWorker, approach, catId,
							workerType, currentTask, model);
			}
		}

		if (specializedCurTaskCat == null) {
			specializedCurTaskCat = new BinCategory();
			if (currentTask.equals("Technical")) {

				techHistoryMapHolder.put(catId, specializedCurTaskCat);
			} else {
				nonTechHistoryMapHolder.put(catId, specializedCurTaskCat);
			}

		}

		if (specializedOveralCategory == null) {
			specializedOveralCategory = new BinCategory();
			if (currentTask.equals("Technical")) {

				techHistoryMapHolder.put(0, specializedOveralCategory);
			} else {
				nonTechHistoryMapHolder.put(0, specializedOveralCategory);
			}

		}

		if (genericCurTaskCat == null) {
			genericCurTaskCat = new BinCategory();
			if (currentTask.equals("Technical")) {

				genericHistoryMapHolder.put(1, genericCurTaskCat);
			} else {
				genericHistoryMapHolder.put(2, genericCurTaskCat);
			}
		}

		addTaskOutcomeToCategory(specializedCurTaskCat, successfulOutcome);
		addTaskOutcomeToCategory(specializedOveralCategory, successfulOutcome);
		addTaskOutcomeToCategory(genericCurTaskCat, successfulOutcome);
		addTaskOutcomeToCategory(genericOveralCategory, successfulOutcome);

		if (currentTask.equals("Technical"))
			evalWorker.increaseTech();
		else
			evalWorker.increaseNonTech();

	}

	private void updateErrors(double actualInstanceQuality,
			EvalWorker evalWorker, String approach, Integer catId,
			String workerType, String currentTask, String model) {

		ODeskTest.errorHolder.setTotalEvaluations(ODeskTest.errorHolder
				.getTotalEvaluations() + 1);

		double modelQuality = 0;
		double modelAbsoluteError = 0;
		double baselineEstimatedQuality = 0;
		double baselineAbsoluteError = 0;

		if (model.equals("Binomial")) {
			modelQuality = predictBinomialModelQuality(catId, evalWorker,
					approach, workerType, currentTask);
			modelAbsoluteError = (Math
					.abs(modelQuality - actualInstanceQuality));

			baselineEstimatedQuality = estimateBinomialBaselineQuality(
					evalWorker, workerType, currentTask);
			baselineAbsoluteError = (Math.abs(baselineEstimatedQuality
					- actualInstanceQuality));
		} else if (model.equals("Multinomial")) {
			modelQuality = predictMultinomialModelQuality(catId, evalWorker,
					approach, workerType, currentTask);
			modelAbsoluteError = (Math
					.abs(modelQuality - actualInstanceQuality));

			baselineEstimatedQuality = estimateMultinomialBaselineQuality(
					evalWorker, workerType, currentTask);
			baselineAbsoluteError = (Math.abs(baselineEstimatedQuality
					- actualInstanceQuality));
		} else {
			System.out.println("Error in models!");
		}

		ODeskTest.errorHolder.setBinomialModelMAESum(ODeskTest.errorHolder
				.getBinomialModelMAESum() + modelAbsoluteError);

		ODeskTest.errorHolder.setBinomialModelMSESum(ODeskTest.errorHolder
				.getBinomialModelMSESum() + (Math.pow(modelAbsoluteError, 2)));

		ODeskTest.errorHolder.setBaselineMAESum(ODeskTest.errorHolder
				.getBaselineMAESum() + baselineAbsoluteError);

		ODeskTest.errorHolder.setBaselineMSESum(ODeskTest.errorHolder
				.getBaselineMSESum() + Math.pow(baselineAbsoluteError, 2));

	}

	private double estimateMultinomialBaselineQuality(EvalWorker evalWorker,
			String workerType, String currentTask) {

		if (workerType.equals("Technical") && currentTask.equals("Technical")) {
			return getAverageHistory(((MultCategory) evalWorker
					.getTechnicalHistoryMap().get(0)).getBucketSuccesses(),
					((MultCategory) evalWorker.getTechnicalHistoryMap().get(0))
							.getN());
		} else if ((workerType.equals("Technical") && currentTask
				.equals("Non-technical"))
				|| (workerType.equals("Non-technical") && currentTask
						.equals("Technical"))) {
			return getAverageHistory(((MultCategory) evalWorker
					.getGenericHistoryMap().get(0)).getBucketSuccesses(),
					((MultCategory) evalWorker.getGenericHistoryMap().get(0))
							.getN());
		} else
			return getAverageHistory(((MultCategory) evalWorker
					.getNonTechHistoryMap().get(0)).getBucketSuccesses(),
					((MultCategory) evalWorker.getNonTechHistoryMap().get(0))
							.getN());
	}

	private double getAverageHistory(double[] bucketSuccesses, double n) {
		double sum = 0;
		for (int i = 0; i < bucketSuccesses.length; i++) {
			sum += bucketSuccesses[i] * qualities[i];
		}
		return sum / n;
	}

	private double predictMultinomialModelQuality(Integer catId,
			EvalWorker evalWorker, String approach, String workerType,
			String currentTask) {

		Double[] coeffs;

		HashMap<String, Double[]> tmpCoeff = new HashMap<String, Double[]>();
		HashMap<Integer, ModelCategory> hm = new HashMap<Integer, ModelCategory>();

		if (workerType.equals("Technical") && currentTask.equals("Technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Technical");
			ODeskTrain.mPlus1 = 4;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2_3";
			hm = evalWorker.getTechnicalHistoryMap();
		} else if (workerType.equals("Technical")
				&& currentTask.equals("Non-technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Generic");
			ODeskTrain.mPlus1 = 3;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2";
			// System.out
			// .println(workerType + "," + currentTask + tmpCoeff.size());
			hm = evalWorker.getGenericHistoryMap();
			catId = 2;

		} else if (workerType.equals("Non-technical")
				&& currentTask.equals("Non-technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Non-technical");
			ODeskTrain.mPlus1 = 4;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2_3";
			hm = evalWorker.getNonTechHistoryMap();

		} else if (workerType.equals("Non-technical")
				&& currentTask.equals("Technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Generic");
			ODeskTrain.mPlus1 = 3;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2";
			hm = evalWorker.getGenericHistoryMap();
			catId = 1;

		}

		double modelQuality = 0;

		coeffs = tmpCoeff.get((catId) + ODeskRegressions.basedOn);
		/*
		 * if(catId == 1 && currentTask.equals("Technical") && ODeskTest.flag){
		 * for(double d1:coeffs) System.out.println(d1); ODeskTest.flag=false; }
		 */
		if (approach.equals("PE")) {
			for (int i = 0; i < ODeskTrain.mPlus1; i++) {
				MultCategory bc = (MultCategory) hm.get(i);
				if (bc == null) {
					bc = new MultCategory();
					modelQuality += coeffs[i]
							* getLogit(fix(getDirichletPointEstimate(bc
									.getQ_ijk())));
				}// getLogit(getCatMeans(i));
				else
					modelQuality += coeffs[i]
							* getLogit(fix(getDirichletPointEstimate(bc
									.getQ_ijk())));

			}
		} else {
			for (int i = 0; i < ODeskTrain.mPlus1; i++) {
				MultCategory bc = (MultCategory) hm.get(i);
				if (bc == null) {
					bc = new MultCategory();
					modelQuality += coeffs[i]
							* getLogit(fix(getDirichletDistroEstimate(bc
									.getBucketSuccesses())));//
					/*
					 * Parameters estimated by fitting beta matlab (betafit)
					 * getLogit(getCatMeans(i));
					 */
				} else
					modelQuality += coeffs[i]
							* getLogit(fix(getDirichletDistroEstimate(bc
									.getBucketSuccesses())));
			}
		}
		return inverseLogit(modelQuality);
	}

	private double estimateBinomialBaselineQuality(EvalWorker evalWorker,
			String workerType, String currentTask) {
		if (workerType.equals("Technical") && currentTask.equals("Technical")) {
			return (((BinCategory) evalWorker.getTechnicalHistoryMap().get(0))
					.getX() / ((BinCategory) evalWorker
					.getTechnicalHistoryMap().get(0)).getN());
		} else if ((workerType.equals("Technical") && currentTask
				.equals("Non-technical"))
				|| (workerType.equals("Non-technical") && currentTask
						.equals("Technical"))) {
			return (((BinCategory) evalWorker.getGenericHistoryMap().get(0))
					.getX() / ((BinCategory) evalWorker.getGenericHistoryMap()
					.get(0)).getN());

		} else
			return (((BinCategory) evalWorker.getNonTechHistoryMap().get(0))
					.getX() / ((BinCategory) evalWorker.getNonTechHistoryMap()
					.get(0)).getN());

	}

	private double predictBinomialModelQuality(int catId,
			EvalWorker evalWorker, String approach, String workerType,
			String currentTask) {
		Double[] coeffs;

		HashMap<String, Double[]> tmpCoeff = new HashMap<String, Double[]>();
		HashMap<Integer, ModelCategory> hm = new HashMap<Integer, ModelCategory>();

		if (workerType.equals("Technical") && currentTask.equals("Technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Technical");
			ODeskTrain.mPlus1 = 4;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2_3";
			hm = evalWorker.getTechnicalHistoryMap();
		} else if (workerType.equals("Technical")
				&& currentTask.equals("Non-technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Generic");
			ODeskTrain.mPlus1 = 3;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2";
			// System.out
			// .println(workerType + "," + currentTask + tmpCoeff.size());
			hm = evalWorker.getGenericHistoryMap();
			catId = 2;

		} else if (workerType.equals("Non-technical")
				&& currentTask.equals("Non-technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Non-technical");
			ODeskTrain.mPlus1 = 4;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2_3";
			hm = evalWorker.getNonTechHistoryMap();

		} else if (workerType.equals("Non-technical")
				&& currentTask.equals("Technical")) {
			tmpCoeff = ODeskTest.allModelCoeffs.get("Generic");
			ODeskTrain.mPlus1 = 3;
			ODeskRegressions.basedOn = "_BasedOn_0_1_2";
			hm = evalWorker.getGenericHistoryMap();
			catId = 1;

		}

		double modelQuality = 0;

		coeffs = tmpCoeff.get((catId) + ODeskRegressions.basedOn);

		if (approach.equals("PE")) {
			for (int i = 0; i < ODeskTrain.mPlus1; i++) {
				BinCategory bc = (BinCategory) hm.get(i);
				if (bc == null)
					modelQuality += coeffs[i]
							* getLogit(getBinomialPointEstimate(0, 0));// getLogit(getCatMeans(i));
				else
					modelQuality += coeffs[i]
							* getLogit(fix(getBinomialPointEstimate(bc.getX(),
									bc.getN())));

			}
		} else {
			for (int i = 0; i < ODeskTrain.mPlus1; i++) {
				BinCategory bc = (BinCategory) hm.get(i);
				if (bc == null)
					modelQuality += coeffs[i]
							* getLogit(fix(getDistroEstimate(0, 0)));//
				/*
				 * Parameters estimated by fitting beta matlab (betafit)
				 * getLogit(getCatMeans(i));
				 */
				else
					modelQuality += coeffs[i]
							* getLogit(fix(getDistroEstimate(bc.getX(),
									bc.getN())));
			}
		}
		return inverseLogit(modelQuality);
	}

	public void updateEvalWorker(
			HashMap<Integer, EvalWorker> dataMapHolderEval, int developerId,
			Integer catId, int bucket, double actualTaskScore, String approach,
			String workerType, String currentTask, String model) {

		EvalWorker evalWorker = dataMapHolderEval.get(developerId);
		HashMap<Integer, ModelCategory> genericHistoryMapHolder;
		HashMap<Integer, ModelCategory> techHistoryMapHolder;
		HashMap<Integer, ModelCategory> nonTechHistoryMapHolder;

		double currentReviews = 0;
		MultCategory specializedOveralCategory = null;
		MultCategory genericOveralCategory = null;

		MultCategory specializedCurTaskCat = null;
		MultCategory genericCurTaskCat = null;

		/*
		 * Creates the necessary objects in order to add history in the end of
		 * the procedure The update follows the conditions!!
		 */
		if (evalWorker == null) {

			evalWorker = new EvalWorker();
			genericHistoryMapHolder = evalWorker.getGenericHistoryMap();
			techHistoryMapHolder = evalWorker.getTechnicalHistoryMap();
			nonTechHistoryMapHolder = evalWorker.getNonTechHistoryMap();

			specializedCurTaskCat = new MultCategory();
			genericCurTaskCat = new MultCategory();
			specializedOveralCategory = new MultCategory();
			genericOveralCategory = new MultCategory();

			genericHistoryMapHolder.put(0, genericOveralCategory);

			if (currentTask.equals("Technical")) {
				techHistoryMapHolder.put(catId, specializedCurTaskCat);
				techHistoryMapHolder.put(0, specializedOveralCategory);
				genericHistoryMapHolder.put(1, genericCurTaskCat);
			} else {
				nonTechHistoryMapHolder.put(0, specializedOveralCategory);
				nonTechHistoryMapHolder.put(catId, specializedCurTaskCat);
				genericHistoryMapHolder.put(2, genericCurTaskCat);
			}

			dataMapHolderEval.put(developerId, evalWorker);

		} else {

			genericHistoryMapHolder = evalWorker.getGenericHistoryMap();
			techHistoryMapHolder = evalWorker.getTechnicalHistoryMap();
			nonTechHistoryMapHolder = evalWorker.getNonTechHistoryMap();

			if (currentTask.equals("Technical")) {
				specializedCurTaskCat = (MultCategory) techHistoryMapHolder
						.get(catId);
				genericCurTaskCat = (MultCategory) genericHistoryMapHolder
						.get(1);
				specializedOveralCategory = (MultCategory) techHistoryMapHolder
						.get(0);
			} else {
				specializedCurTaskCat = (MultCategory) nonTechHistoryMapHolder
						.get(catId);
				genericCurTaskCat = (MultCategory) genericHistoryMapHolder
						.get(2);
				specializedOveralCategory = (MultCategory) nonTechHistoryMapHolder
						.get(0);
			}
			genericOveralCategory = (MultCategory) genericHistoryMapHolder
					.get(0);

			currentReviews = genericHistoryMapHolder.get(0).getN();
			if (currentReviews > ODeskTest.historyThreshold) {

				if (approach.equals("RS")) {
					for (int j = 0; j < 30; j++) {
						updateErrors(actualTaskScore, evalWorker, approach,
								catId, workerType, currentTask, model);

					}
				} else
					updateErrors(actualTaskScore, evalWorker, approach, catId,
							workerType, currentTask, model);
			}
		}

		if (specializedCurTaskCat == null) {
			specializedCurTaskCat = new MultCategory();
			if (currentTask.equals("Technical")) {

				techHistoryMapHolder.put(catId, specializedCurTaskCat);
			} else {
				nonTechHistoryMapHolder.put(catId, specializedCurTaskCat);
			}

		}

		if (specializedOveralCategory == null) {
			specializedOveralCategory = new MultCategory();
			if (currentTask.equals("Technical")) {

				techHistoryMapHolder.put(0, specializedOveralCategory);
			} else {
				nonTechHistoryMapHolder.put(0, specializedOveralCategory);
			}

		}

		if (genericCurTaskCat == null) {
			genericCurTaskCat = new MultCategory();
			if (currentTask.equals("Technical")) {

				genericHistoryMapHolder.put(1, genericCurTaskCat);
			} else {
				genericHistoryMapHolder.put(2, genericCurTaskCat);
			}
		}

		addTaskOutcomeToCategory(specializedCurTaskCat, bucket);
		addTaskOutcomeToCategory(specializedOveralCategory, bucket);
		addTaskOutcomeToCategory(genericCurTaskCat, bucket);
		addTaskOutcomeToCategory(genericOveralCategory, bucket);

		if (currentTask.equals("Technical"))
			evalWorker.increaseTech();
		else
			evalWorker.increaseNonTech();

	}

}
