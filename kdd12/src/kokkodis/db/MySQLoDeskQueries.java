package kokkodis.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import kokkodis.factory.EvalWorker;
import kokkodis.factory.ModelCategory;
import kokkodis.odesk.ODeskTrain;
import kokkodis.utils.odesk.TestUtils;
import kokkodis.utils.odesk.TrainUtils;

public class MySQLoDeskQueries extends Queries {

	public MySQLoDeskQueries() {
		super();
	}

	public void connect() {
		try {
			dbname = "tagsdb";
			// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Properties props = new Properties();
			props.put("user", username);
			props.put("password", password);
			props.put("databaseName", dbname);
			conn = DriverManager
					.getConnection("jdbc:mysql://localhost/odesk?user=root&password=11r88a4m");
			/*
			 * conn = DriverManager.getConnection(
			 * "jdbc:sqlserver://hyperion.stern.nyu.edu:1433" //
			 * "jdbc:sqlserver://localhost:2000;" , props);
			 */
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private HashMap<Integer, Integer> mapCategories() {
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		try {

			String selectString = "select catId,level1 "
					+ "from `odesk`.`catMapping` ";
			PreparedStatement prepStmt = conn.prepareStatement(selectString);
			prepStmt.execute();
			ResultSet rs = prepStmt.getResultSet();
			while (rs.next()) {
				hm.put(rs.getInt("catId"), rs.getInt("level1"));
			}
		} catch (SQLException e) {

			e.printStackTrace();

		}
		return hm;
	}

	public void rawDataToBinomialModel(String level, String approach,
			String model, boolean test) {

		if (!test) {
			System.out.println("Quering DB.");
			System.out.println("Level:" + level);
			System.out.println("Approch:" + approach);
		}
		HashMap<Integer, HashMap<Integer, ModelCategory>> dataMapHolder = null;

		HashMap<Integer, EvalWorker> dataMapHolderEval = null;

		HashMap<Integer, Integer> cats = mapCategories();

		HashSet<Integer> techCats = new HashSet<Integer>();
		techCats.add(1);
		techCats.add(2);
		techCats.add(6);

		HashSet<Integer> nonTechCats = new HashSet<Integer>();
		nonTechCats.add(3);
		nonTechCats.add(4);
		nonTechCats.add(5);

		try {

			String selectString;
			if (!test) {

				dataMapHolder = new HashMap<Integer, HashMap<Integer, ModelCategory>>();
				selectString = "select developer,jobCategory, "
						+ "(availability+communication+cooperation+deadlines+quality+skills+total)/7 as score "
						+ "from `odesk`.`train`   order by date";
			} else {
				dataMapHolderEval = new HashMap<Integer, EvalWorker>();
				selectString = "select developer,jobCategory,"
						+ "(availability+communication+cooperation+deadlines+quality+skills+total)/7 as score "
						+ "from `odesk`.`test`   order by date";
			}
			PreparedStatement prepStmt = conn.prepareStatement(selectString);
			prepStmt.execute();
			ResultSet rs = prepStmt.getResultSet();
			if (level.equals("Technical")) {

				manipulateData(rs, approach, model, cats, dataMapHolder, level,
						techCats);

			} else if (level.equals("Non-technical")) {

				manipulateData(rs, approach, model, cats, dataMapHolder, level,
						nonTechCats);
			} else if (level.equals("Generic")) {
				manipulateData(rs, approach, model, cats, dataMapHolder, level,
						null);
			} else if (test) {
				manipulateData(rs, approach, model, cats, dataMapHolderEval);
			}

		} catch (SQLException e) {

			e.printStackTrace();

		}

	}

	/**
	 * This is for evaluating
	 * 
	 * @param rs
	 * @param approach
	 * @param model
	 * @param cats
	 * @param dataMapHolderEval
	 * @throws SQLException
	 */

	protected void manipulateData(ResultSet rs, String approach, String model,
			HashMap<Integer, Integer> cats,
			HashMap<Integer, EvalWorker> dataMapHolderEval) throws SQLException {

		TestUtils tu = new TestUtils();
		if (model.equals("Binomial")) {
			while (rs.next()) {
				Integer catId = cats.get(rs.getInt("jobCategory"));

				if (catId != null) {
					int developerId = rs.getInt("developer");

					double score = rs.getDouble("score");
					double actualTaskScore = (score / 5.0);

					boolean succesfullOutcome = ((actualTaskScore > ODeskTrain.scoreTh)) ? true
							: false;

					EvalWorker tmp = dataMapHolderEval.get(developerId);
					String currentTask = (tu.getGenericCat(catId) == 1) ? "Technical"
							: "Non-technical";
					String workerType = null;
					if (tmp == null) {
						tmp = new EvalWorker();
						workerType = currentTask;
					} else {

						workerType = tmp.getWorkerType();
					}
					catId = tu.adjustODeskCategory(workerType, catId);
					tu.updateEvalWorker(dataMapHolderEval, developerId, catId,
							succesfullOutcome, actualTaskScore, approach,
							workerType, currentTask, model);
				}
			}
		} else if (model.equals("Multinomial")) {
			while (rs.next()) {
				Integer catId = cats.get(rs.getInt("jobCategory"));

				if (catId != null) {

					int developerId = rs.getInt("developer");

					double score = rs.getDouble("score");
					double actualTaskScore = (score / 5.0);
					int bucket = tu.getBucket(actualTaskScore);

					EvalWorker tmp = dataMapHolderEval.get(developerId);
					String currentTask = (tu.getGenericCat(catId) == 1) ? "Technical"
							: "Non-technical";
					String workerType = null;
					if (tmp == null) {
						tmp = new EvalWorker();
						workerType = currentTask;
					} else {

						workerType = tmp.getWorkerType();
					}
					catId = tu.adjustODeskCategory(workerType, catId);

					tu.updateEvalWorker(dataMapHolderEval, developerId, catId,
							bucket, actualTaskScore, approach, workerType,
							currentTask, model);
				}

			}

		}
	}

	/**
	 * This is for training!
	 * 
	 * @param rs
	 * @param approach
	 * @param model
	 * @param cats
	 *            Categories mapping
	 * @param dataMapHolder
	 * @param level
	 *            Technical, nontechnical or generic
	 * @param catMapping
	 *            hashmap for adjusting levels
	 * @throws SQLException
	 */

	protected void manipulateData(ResultSet rs, String approach, String model,
			HashMap<Integer, Integer> cats,
			HashMap<Integer, HashMap<Integer, ModelCategory>> dataMapHolder,
			String level, HashSet<Integer> catMapping) throws SQLException {

		TrainUtils tu = new TrainUtils();
		if (model.equals("Binomial")) {
			while (rs.next()) {
				Integer catId = cats.get(rs.getInt("jobCategory"));

				if (catId != null) {

					if (catMapping == null || catMapping.contains(catId)) {

						catId = tu.adjustODeskCategory(level, catId);

						int developerId = rs.getInt("developer");

						double score = rs.getDouble("score");
						double actualTaskScore = (score / 5.0);

						boolean succesfullOutcome = ((actualTaskScore > ODeskTrain.scoreTh)) ? true
								: false;

						tu.updateWorkerHistoryAndPrintTuple(dataMapHolder,
								developerId, catId, succesfullOutcome,
								actualTaskScore, approach, model);
					}
				}
			}
		} else {
			System.out.println("Multinomial!");
			while (rs.next()) {
				Integer catId = cats.get(rs.getInt("jobCategory"));

				if (catId != null) {

					if (catMapping == null || catMapping.contains(catId)) {

						catId = tu.adjustODeskCategory(level, catId);

						int developerId = rs.getInt("developer");

						double score = rs.getDouble("score");
						double actualTaskScore = (score / 5.0);

						int bucket = tu.getBucket(actualTaskScore);

						tu.updateWorkerHistoryAndPrintTuple(dataMapHolder,
								developerId, catId, bucket, actualTaskScore,
								approach, model);
					}
				}

			}
		}
	}

	public static void main(String[] args) {
		MySQLoDeskQueries q = new MySQLoDeskQueries();
		q.connect();
		q.printTransitions();
	}

	private void printTransitions() {
		try {
			int[][] transitions = new int[6][6];
			HashMap<Integer, Integer> cats = mapCategories();
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			String selectString;

			selectString = "select developer,jobCategory "

			+ "from `odesk`.`train`   order by date";

			PreparedStatement prepStmt = conn.prepareStatement(selectString);
			prepStmt.execute();
			ResultSet rs = prepStmt.getResultSet();
			while (rs.next()) {
				int developerId = rs.getInt("developer");
				Integer catId = cats.get(rs.getInt("jobCategory"));
				if (catId != null) {
					catId--;
					Integer prevCat = hm.get(developerId);
					if (prevCat == null)
						hm.put(developerId, catId);
					else {
						transitions[prevCat][catId]++;
						hm.put(developerId, catId);
					}
				}
			}

			for (int i = 0; i < 6; i++) {
				double linesum=0;
				for (int j = 0; j < 6; j++) {
					linesum+=transitions[i][j];
					
				}for (int j = 0; j < 6; j++) {
					DecimalFormat myFormatter = new DecimalFormat("#.###");
					String output = myFormatter.format((double)transitions[i][j]/linesum);
					System.out.print( output+ " & ");
					
				}
				
				System.out.println("//");
			}

		} catch (SQLException e) {

			e.printStackTrace();

		}
	}

}
