package kokkodis.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.doodleproject.numerics4j.random.BetaRandomVariable;
import net.sf.doodleproject.numerics4j.random.GammaRandomVariable;

import kokkodis.factory.BinCategory;
import kokkodis.factory.ModelCategory;
import kokkodis.factory.MultCategory;
import kokkodis.odesk.ODeskTrain;

public class Utils {

	public static double[] qualities = { 0.2, 0.4, 0.6, 0.8, 1 };
	private static double[] binomialPrior = {9,1};
	public static double[] multinomialPrior =
	//{1,1,1,1,16};
	{ 2, 1, 1, 4, 8};
	public static double multinomialPriorTotal = 
		//20;
		16;
	
	public Utils() {
	}
	
	public HashMap<Integer, Integer> mapCategories() {
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		hm.put(32, 1); // books
		hm.put(5, 2);// movies
		hm.put(46, 3); // music
		hm.put(324, 4); // vgames

		return hm;
	}

	public void printMap(HashMap<Integer, Integer> cats) {
		ODeskTrain.print("Map:");
		Set<Entry<Integer, Integer>> set = cats.entrySet();
		Iterator<Entry<Integer, Integer>> setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<Integer, Integer> me = setIt.next();
			ODeskTrain.print(me.getKey() + "," + me.getValue());

		}
	}

	public void addTaskOutcomeToCategory(ModelCategory modelCategory,
			boolean succesfulOutcome) {

		if (succesfulOutcome) {
			((BinCategory) modelCategory).setX(((BinCategory) modelCategory)
					.getX() + 1);
			((BinCategory) modelCategory).setN(((BinCategory) modelCategory)
					.getN() + 1);
		} else {
			((BinCategory) modelCategory).setN(((BinCategory) modelCategory)
					.getN() + 1);
		}
	}

	public void addTaskOutcomeToCategory(MultCategory curTaskCat, int bucket) {
		curTaskCat.getBucketSuccesses()[bucket]++;
		curTaskCat.increaseTotalTrials();

	}

	public double fix(double l) {
		if (l < 0.01)
			return 0.01;
		if (l > 0.99)
			return 0.99;
		return l;
	}

	public double getLogit(double q) {
		return Math.log(q / (1 - q));

	}

	public void createDirs(File f) {
		if (!f.exists()) {
			System.out.println("Creating directory..:" + f);
			System.out.println("New dir.." + f.mkdir());
		}

	}

	public double inverseLogit(double r) {
		double eTor = Math.exp(r);
		return eTor / (1 + eTor);
	}

	public double getBinomialPointEstimate(double x, double n) {

		return (x + binomialPrior[0]) / (n + binomialPrior[0]+binomialPrior[1]);
	}

	public double getDirichletPointEstimate(double[] q_ijk) {
		double sum = 0;

		for (int i = 0; i < q_ijk.length; i++) {
			sum += q_ijk[i] * qualities[i];
		}

		// System.out.println(sum);
		return sum;
	}

	public double getDistroEstimate(double x, double n) {
		double alpha = x + binomialPrior[0];
		double beta = n - x + binomialPrior[1];
		double res = (new BetaRandomVariable(alpha, beta)).nextRandomVariable();
		//System.out.println("Res:"+res);
		return res;
	}

	/**
	 * 
	 * @param bucketSuccesses
	 * @return
	 */
	public double getDirichletDistroEstimate(double[] bucketSuccesses) {

		// double [] prior = {4,1, 2, 2, 2,3, 2.5, 10,20};
		
		double[] newDistro = new double[(int) ODeskTrain.K];
		double sum = 0;
		for (int i = 0; i < bucketSuccesses.length; i++) {
			double alpha_k = bucketSuccesses[i] + multinomialPrior[i];
			newDistro[i] = new GammaRandomVariable(alpha_k, alpha_k)
					.nextRandomVariable();
			sum += newDistro[i];
		}
		for (int i = 0; i < newDistro.length; i++) {
			newDistro[i] = newDistro[i] / sum;
		}

		double de = getExpectation(newDistro);
		return de;
	}

	private double getExpectation(double[] newDistro) {
		double res = 0;

		for (int i = 0; i < ODeskTrain.K; i++)
			res += newDistro[i] * qualities[i];
		return res;
	}

	public Integer getGenericCat(Integer catId) {
		if (catId == 1)
			return 1;
		else if (catId == 2)
			return 1;
		else if (catId == 3)
			return 2;
		else if (catId == 4)
			return 2;
		else if (catId == 5)
			return 2;
		else
			return 1;
	}

	public int adjustODeskCategory(String level, int catId) {

		if (level.equals("Technical")) {
			if (catId == 6)
				return 3;
			else
				return catId;
		}// Just to fit in the model with m=4;
		if (level.equals("Non-technical"))
			return catId - 2;
		else
			return getGenericCat(catId);
	}

	public int getBucket(double actualTaskScore) {
		if (actualTaskScore <= qualities[0])
			return 0;
		if (actualTaskScore <= qualities[1])
			return 1;
		if (actualTaskScore <= qualities[2])
			return 2;
		if (actualTaskScore <= qualities[3])
			return 3;

		return 4;

	}

}
