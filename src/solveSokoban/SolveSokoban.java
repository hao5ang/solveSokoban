package solveSokoban;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SolveSokoban implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1340445421487831855L;

	public static final File mapFile = new File("map.txt");

	private SokobanMap map;

	private Set<SokobanMap> duplicateCheck = new HashSet<SokobanMap>();
	private LinkedList<MapNode> processList = new LinkedList<MapNode>();
	private LinkedList<Direction> succTrace = null;

	public static void main(String[] args) throws Exception {
		SolveSokoban solve = new SolveSokoban();
		SokobanMap tem = (SokobanMap) solve.map.clone();
		System.out.println(tem.toString());
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
				tem.move(d);
				System.out.println(tem.toString());
				Thread.sleep(500);
			}
			System.out.println("finished!");
			System.out.println(directions);
		}
	}

	public LinkedList<Direction> getBestSuccTrace() {
		return succTrace;
	}

	public SolveSokoban() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void solve() throws Exception {
		processList.add(new MapNode(null, new LinkedList<Direction>(), this.map));
		MapNode node = null;
		while ((node = processList.poll()) != null) {
			// System.out.println(node.getMap());
			if (solve(node)) {
				break;
			}
		}
		processList.clear();
	}

	private boolean solve(MapNode node) throws Exception {

		SokobanMap map = node.getMap();

		if (duplicateCheck.contains(map)) {
			return false;
		}
		if (map.success()) {
			succTrace = backFindDirections(node);
			return true;
		}

		if (node.getDirectionChange().size() > 0) {
			node.moveForward();
			processList.add(node);
		} else {
			duplicateCheck.add(map);
			SokobanMap tem = (SokobanMap) map.clone();
			LinkedList<LinkedList<Direction>> bestPaths = tem.spreadBestPaths();

			for (LinkedList<Direction> bestPath : bestPaths) {
				MapNode next = new MapNode(node, bestPath, map);
				next.moveForward();
				processList.add(next);
			}
		}
		return false;
	}

	private LinkedList<Direction> backFindDirections(MapNode node) {
		LinkedList<Direction> directions = new LinkedList<Direction>();
		directions.addAll(node.getBestPath());
		while ((node = node.getPreNode()) != null) {
			LinkedList<Direction> subDirections = node.getBestPath();
			Direction d;
			while ((d = subDirections.pollLast()) != null) {
				directions.addFirst(d);
			}
		}
		return directions;
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
		map = SokobanMap.restoreFromStr(str);
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

		public Solution() {
		}

		public Solution(SokobanMap succMap, Direction lastDirection, Set<SokobanMap> succSet) {
			this.succMap = succMap;
			this.lastDirection = lastDirection;
			this.succSet = succSet;
		}

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
