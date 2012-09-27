package kokkodis.synthetic;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.utils.PrintToFile;
import flanagan.math.PsRandom;

public class DataGenerationHierarchy {

	public static int noOfClusters = 3;
	public static int noOfCatsInClaster = 3;

	/**
	 * @param args
	 */
	public static String rawData = "/Users/mkokkodi/Desktop/bigFiles/kdd/synthetic/rawData/RS/clustered"
			+ noOfClusters;

	public static HashMap<Integer, Integer> categoryToCluster;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		categoryToCluster = new HashMap<Integer, Integer>();
		int index = 0;
		for (int k = 0; k < noOfCatsInClaster; k++) {
			for (int i = 1; i <= noOfCatsInClaster; i++) {

				System.out.println("Cat:" + index + " cluster:" + k);
				categoryToCluster.put(index, k);
				index++;
			}
		}
		System.out.println("Starting...");
		for (int categories = 9; categories < 10; categories += 2) {
			PrintToFile trainFile = new PrintToFile();
			trainFile
					.openFile(new File(rawData + "train" + categories + ".csv"));
			PrintToFile testFile = new PrintToFile();
			testFile.openFile(new File(rawData + "test" + categories + ".csv"));

			double[] categoriesDistribution = new double[categories];

			HashMap<Integer, double[][]> clustersTransitionMatrix = new HashMap<Integer, double[][]>();

			for (int cluster = 0; cluster < noOfClusters; cluster++) {

				double[][] probs = new double[noOfCatsInClaster][noOfCatsInClaster];
				for (int i = 0; i < noOfCatsInClaster; i++) {
					probs[i][i] = Math.random() / 2 + 1
							/ (double) noOfCatsInClaster;
					double sum = probs[i][i];
					for (int j = 0; j < noOfCatsInClaster - 1; j++) {
						if (i != j) {
							probs[i][j] = (1 - sum) * Math.random();
							sum += probs[i][j];
						}
					}
					if (i != noOfCatsInClaster - 1)
						probs[i][noOfCatsInClaster - 1] = 1 - sum;
					else {
						probs[i][i] += 1 - sum;
					}

				}
				clustersTransitionMatrix.put(cluster, probs);

			}

			double sum = 0;
			for(int j=0; j<categories; j++){
				categoriesDistribution[j] = 1.0/(double)categories;
				//System.out.println(categoriesDistribution[j]);
			}
		/*	for (int j = 0; j < categories - 1; j++) {
				categoriesDistribution[j] = (1 - sum) * Math.random();
				sum += categoriesDistribution[j];
			}

			categoriesDistribution[categories - 1] = 1 - sum;
*/
			trainFile.writeToFile("##");
			trainFile.writeToFile("#Transition Probabilities for " + categories
					+ " categories.");

			for (Entry<Integer, double[][]> e : clustersTransitionMatrix
					.entrySet()) {
				trainFile.writeToFile("# cluster:" + e.getKey());
				for (int i = 0; i < noOfCatsInClaster; i++) {
					trainFile.writeNoLN_ToFile("# ");
					for (int j = 0; j < noOfCatsInClaster; j++)
						trainFile.writeNoLN_ToFile(" " + e.getValue()[i][j]);
					trainFile.writeToFile("");
				}
			}
			trainFile.writeToFile("##");
			trainFile
					.writeNoLN_ToFile("# Categories distribution -> used to choose the initial category. ");
			trainFile.writeToFile("#");
			for (int i = 0; i < categories; i++)
				trainFile.writeToFile("# Prob(category =" + (i + 1) + ")="
						+ categoriesDistribution[i]);

			trainFile.writeToFile("###############");
			trainFile.writeToFile("id,task,cat,quality");
			testFile.writeToFile("id,task,cat,quality");
			for (int i = 0; i < 10000; i++) {
				int initCat = getInitialCat(categoriesDistribution);
			//	System.out.println(initCat);
				double[] userQualities = getUserQualities(initCat, categories);
				double[] userQualitiesDeviations = getDeviations(categories);
				int curCat = initCat;
				PsRandom psr = new PsRandom();
				int numberOfReviews = (int) Math.floor(Math.random() * 50);
				boolean test = (Math.random() > 0.8) ? true : false;
				for (int l = 0; l < numberOfReviews; l++) {
					double res = 2;
					while (res > 1)
						res = psr.nextGaussian(userQualities[initCat],
								userQualitiesDeviations[initCat]);

					if (test)
						testFile.writeToFile(i + "," + l + "," + (curCat + 1)
								+ "," + res);
					else {
						trainFile.writeToFile(i + "," + l + "," + (curCat + 1)
								+ "," + res);
					}
				//	System.out.println("Init cat:" + initCat);
					int curCluster = categoryToCluster.get(curCat);
					//System.out.println(curCluster);
					if (Math.random() > 0.7) {
						curCat = getNextCatFromOtherClusters(curCluster);
					} else
						curCat = getNextCat(
								clustersTransitionMatrix.get(curCluster),
								initCat % 3, curCluster);
				}
			}
			trainFile.closeFile();
			testFile.closeFile();
		}
		System.out.println("Data generation completed. ");
	}

	private static int getNextCatFromOtherClusters(int curCluster) {
		if (curCluster == 1) {
			return (int) (2 + Math.round(Math.random() * 6));
		} else if (curCluster == 2) {
			if (Math.random() > 0.5)
				return (int) (Math.round(Math.random() * 2));
			else
				return (int) (5 + Math.round(Math.random() * 3));
		} else
			return (int) (Math.round(Math.random() * 5));

	}

	private static int getNextCat(double[][] transitionProbabilities, int curCat,int cluster) {
		//System.out.println(curCat);

		double random = Math.random();

		if (random < transitionProbabilities[curCat][0])
			return 0 + cluster * noOfClusters;
		double sum = transitionProbabilities[curCat][0];
		for (int i = 1; i < transitionProbabilities[curCat].length; i++) {
			sum += transitionProbabilities[curCat][i];
		//	System.out.println("sum:" + sum);
			if (random <= sum)
				return i + cluster * noOfClusters;
		}
	//	System.out.println("Random:" + random + " , sum:" + sum);
		return -1;
	}

	private static double[] getDeviations(int categories) {
		double[] deviations = new double[categories];
		for (int i = 0; i < categories; i++)
			deviations[i] = (double) Math.random() / 5;
		return deviations;
	}

	private static double[] getUserQualities(int initCat, int categories) {
		double[] qualities = new double[categories];
		qualities[initCat] = Math.random() / 2 + 0.5;
		for (int i = 0; i < categories; i++) {
			if (i != initCat)
				qualities[i] = Math.random();

		}
		return qualities;
	}

	private static int getInitialCat(double[] categoriesDistribution) {
		double random = Math.random();
		if (random < categoriesDistribution[0])
			return 0;
		double sum = categoriesDistribution[0];
		for (int j = 1; j < categoriesDistribution.length; j++) {
			sum += categoriesDistribution[j];
			if (random < sum)
				return j;
		}
		return -1;
	}
}
