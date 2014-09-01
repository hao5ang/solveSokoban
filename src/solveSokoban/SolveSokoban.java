package solveSokoban;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class SolveSokoban implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1340445421487831855L;

	public static final File mapFile = new File("map.txt");

	private SokobanMap orginalMap;
	private SokobanMap map;

	private Set<SokobanMap> duplicateCheck = new HashSet<SokobanMap>();
	private LinkedList<Direction> trace = new LinkedList<Direction>();
	private Set<SokobanMap> traceSet = new HashSet<SokobanMap>();
	private LinkedList<Direction> succTrace = null;
	private Set<SokobanMap> succSet = new HashSet<SokobanMap>();
	private LinkedList<Solution> solutions = new LinkedList<Solution>();
	private LinkedList<Direction> bestTrace = null;

	public static void main(String[] args) throws Exception {
		SolveSokoban solve = new SolveSokoban();
		SokobanMap mem = (SokobanMap) solve.orginalMap.clone();
		System.out.println(mem.toString());
		solve.solve();
		LinkedList<Direction> directions = solve.getBestSuccTrace();
		if (directions == null) {
			System.out.println("\n\nThere is no solutioin for this map!!! ");
		} else {
			System.out.println("steps:" + directions.size());
			Thread.sleep(1500);
			int i = 0;
			for (Direction d : directions) {
				System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
				System.out.println("step " + (++i) + "/" + directions.size());
				mem.move(d);
				System.out.println(mem.toString());
				Thread.sleep(500);
			}
			System.out.println("finished!");
			System.out.println(directions);
		}
	}

	public LinkedList<Direction> getBestSuccTrace() {
		return bestTrace;
	}

	public SolveSokoban() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void solve() throws Exception {
		solve(false);
		System.out.println(solutions.size());
		for (Solution solution : solutions) {
			System.out.println(solution.succSet.size());
			this.map = (SokobanMap) this.orginalMap.clone();
			this.duplicateCheck.clear();
			this.trace.clear();
			this.traceSet.clear();
			this.succTrace = null;
			this.succSet = solution.succSet;
			solve(true);
			System.out.println(succTrace.size());
			if (bestTrace == null || bestTrace.size() > succTrace.size()) {
				bestTrace = new LinkedList<Direction>();
				bestTrace.addAll(succTrace);
			}
		}
	}

	private void solve(boolean optimize) throws Exception {
		// System.out.println("trace size:" + trace.size());
		// System.out.println("solve:\n" + this.map.toString());
		// Thread.sleep(1000);

		if (optimize) {
			if (traceSet.contains(map) || duplicateCheck.contains(map) && !succSet.contains(map)) {
				// System.out.println("- contains!");
				return;
			}
		} else {
			if (duplicateCheck.contains(map)) {
				return;
			}
		}
		// if (succTrace != null && trace.size() >= succTrace.size() - 1) {
		// System.out.println("- too long!");
		// return;
		// }
		if (map.success()) {
			if (optimize) {
				if (succTrace == null || trace.size() < succTrace.size()) {
					succSet.clear();
					succSet.addAll(traceSet);
					succTrace = new LinkedList<Direction>();
					succTrace.addAll(trace);
					// System.out.println("- success!\n\n###############\n\nsuccess!!");
					// Thread.sleep(500);
				}
			} else {
				Solution solution = new Solution();
				solution.lastDirection = trace.getLast();
				solution.succMap = (SokobanMap) map.clone();
				solution.succSet = new HashSet<SokobanMap>();

				boolean add = true;
				Iterator<Solution> it = solutions.iterator();
				while (it.hasNext()) {
					Solution s = it.next();
					if (s.equals(solution)) {
						if (s.succSet.size() <= solution.succSet.size()) {
							add = false;
						} else {
							it.remove();
						}
						break;
					}
				}
				if (add) {
					solution.succSet.addAll(traceSet);
					solutions.add(solution);
				}
			}
			return;
		}

		SokobanMap mem = (SokobanMap) map.clone();
		traceSet.add(mem);
		duplicateCheck.add(mem);
		LinkedList<LinkedList<Direction>> bestPaths = map.spreadBestPaths();
		for (LinkedList<Direction> bestPath : bestPaths) {
			for (Direction d : bestPath) {
				map.move(d);
				trace.addLast(d);
			}
			solve(optimize);
			for (int i = 0; i < bestPath.size(); i++) {
				trace.removeLast();
			}
			map = (SokobanMap) mem.clone();
		}
		traceSet.remove(mem);
	}

	private void createMapFile() throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(mapFile));
		try {
			String comment = SokobanMap.getComment();
			br.write(comment);
		} finally {
			br.close();
		}
	}

	private void init() throws Exception {
		if (!mapFile.exists() || mapFile.length() == 0) {
			createMapFile();
		}
		initMap();
	}

	private void initMap() throws CloneNotSupportedException {
		String str = readDataFromFile();
		orginalMap = SokobanMap.restoreFromStr(str);
		map = (SokobanMap) orginalMap.clone();
	}

	private String readDataFromFile() {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(mapFile));
			boolean ignore = false;
			try {
				String temStr = null;
				while ((temStr = br.readLine()) != null) {
					if (temStr.trim().startsWith("//")) {
						continue;
					}

					if (temStr.contains("*/")) {
						ignore = false;
						continue;
					}

					if (temStr.startsWith("/*")) {
						ignore = true;
					}

					if (ignore || temStr.trim().equals("")) {
						continue;
					}
					sb.append(temStr);
					sb.append("\n");
				}
				return sb.toString();
			} finally {
				br.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class Solution {
		SokobanMap succMap;
		Direction lastDirection;
		Set<SokobanMap> succSet;

		@Override
		public int hashCode() {
			return succMap.hashCode() * 10 + lastDirection.ordinal();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj instanceof Solution) {
				Solution temSolution = (Solution) obj;
				if (temSolution.succMap == null) {
					return temSolution.succMap == this.succMap && temSolution.lastDirection == this.lastDirection;
				}
				return temSolution.succMap.equals(this.succMap) && temSolution.lastDirection == this.lastDirection;
			}
			return false;
		}
	}
}
