package solveSokoban;

import java.util.LinkedList;

public class MapNode {
	private MapNode preNode;
	private LinkedList<Direction> directionChange;
	private LinkedList<Direction> bestPath = new LinkedList<Direction>();
	private SokobanMap map;
	private int stepCnt;

	public MapNode() {
	}

	public MapNode(MapNode preNode, LinkedList<Direction> directionChange, SokobanMap map) {
		this.preNode = preNode;
		if (directionChange == null) {
			directionChange = new LinkedList<Direction>();
		}
		this.directionChange = directionChange;
		this.map = map;
		this.stepCnt = directionChange.size();
		if (preNode != null) {
			this.stepCnt += preNode.getStepCnt();
		}
	}

	public LinkedList<Direction> getBestPath() {
		return bestPath;
	}

	public void setBestPath(LinkedList<Direction> bestPath) {
		this.bestPath = bestPath;
	}

	public int getStepCnt() {
		return stepCnt;
	}

	public void setStepCnt(int stepCnt) {
		this.stepCnt = stepCnt;
	}

	public MapNode getPreNode() {
		return preNode;
	}

	public void setPreNode(MapNode preNode) {
		this.preNode = preNode;
	}

	public LinkedList<Direction> getDirectionChange() {
		return directionChange;
	}

	public void setDirectionChange(LinkedList<Direction> directionChange) {
		this.directionChange = directionChange;
	}

	public SokobanMap getMap() {
		return map;
	}

	public void setMap(SokobanMap map) {
		this.map = map;
	}

	public void moveForward() throws CloneNotSupportedException {
		// System.out.println("@@ \n" + this.map);
		Direction d = getDirectionChange().poll();
		// System.out.println("move:" + d);
		getBestPath().add(d);
		SokobanMap tem = (SokobanMap) map.clone();
		tem.move(d);
		setMap(tem);
		// System.out.println(this.map);
	}

}
