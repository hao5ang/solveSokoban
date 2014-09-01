package solveSokoban;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SokobanMap implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1177768729712971651L;
	public static final char MAN_CHAR = 'a';
	public static final char BOX_CHAR = '$';
	public static final char FENCE_CHAR = '#';
	public static final char AIM_CHAR = 'O';
	public static final char MAN_AIM_CHAR = '@';
	public static final char BOX_AIM_CHAR = '%';
	public static final char EMPTY_CHAR = ' ';
	public static final char PATH_START_NUM = 10000;

	private char[][] sokobanMap;
	private Position manPosition;
	private LinkedList<Position> aims;

	public SokobanMap(char[][] sokobanMap) {
		this.sokobanMap = sokobanMap;
		validateMapData(this);
		initPositions();
	}

	private void initPositions() {
		this.aims = new LinkedList<Position>();
		for (int i = 0; i < sokobanMap.length; i++) {
			for (int j = 0; j < sokobanMap[i].length; j++) {
				if (sokobanMap[i][j] == SokobanMap.MAN_CHAR || sokobanMap[i][j] == SokobanMap.MAN_AIM_CHAR) {
					this.manPosition = new Position(i, j);
				} else if (sokobanMap[i][j] == SokobanMap.AIM_CHAR || sokobanMap[i][j] == SokobanMap.BOX_AIM_CHAR) {
					aims.add(new Position(i, j));
				}
			}
		}
	}

	public char getValue(Position p) {
		return sokobanMap[p.getRow()][p.getColumn()];
	}

	public void setValue(Position p, char c) {
		this.sokobanMap[p.getRow()][p.getColumn()] = c;
	}

	public Position getNeighbor(Position p, Direction d) {
		Position neighbor = new Position(p);
		switch (d) {
		case UP:
			neighbor.setRow(neighbor.getRow() - 1);
			break;
		case DOWN:
			neighbor.setRow(neighbor.getRow() + 1);
			break;
		case LEFT:
			neighbor.setColumn(neighbor.getColumn() - 1);
			break;
		case RIGHT:
			neighbor.setColumn(neighbor.getColumn() + 1);
			break;
		}
		return neighbor;
	}

	public boolean canPush(Position p, Direction d) {
		if (getValue(p) == AIM_CHAR || getValue(p) == EMPTY_CHAR) {
			return true;
		}
		if (getValue(p) == BOX_CHAR || getValue(p) == BOX_AIM_CHAR) {
			char neighborValue = this.getValue(this.getNeighbor(p, d));
			if (neighborValue == AIM_CHAR || neighborValue == EMPTY_CHAR || neighborValue >= PATH_START_NUM) {
				return true;
			}
		}
		return false;
	}

	public LinkedList<Direction> backBestPath(Position current, Direction d) {
		LinkedList<Direction> bestPath = new LinkedList<Direction>();
		bestPath.addFirst(d);
		Position back = new Position(current);
		while (true) {
			if (getValue(back) == PATH_START_NUM) {
				break;
			}
			for (Direction temD : Direction.values()) {
				Position neighbor = this.getNeighbor(back, temD);
				char neighborValue = this.getValue(neighbor);
				if (neighborValue == this.getValue(back) - 1) {
					bestPath.addFirst(Direction.revert(temD));
					back = neighbor;
					break;
				}
			}
		}
		return bestPath;
	}

	private Position getManPosition() {
		for (int i = 0; i < sokobanMap.length; i++) {
			for (int j = 0; j < sokobanMap[i].length; j++) {
				if (sokobanMap[i][j] == SokobanMap.MAN_CHAR || sokobanMap[i][j] == SokobanMap.MAN_AIM_CHAR) {
					return new Position(i, j);
				}
			}
		}
		throw new RuntimeException("Can not find the man position!");
	}

	public static SokobanMap restoreFromStr(String mapStr) {
		// System.out.println("restore to:\n" + mapStr);
		String[] rows = mapStr.split("\n");
		LinkedList<char[]> list = new LinkedList<char[]>();
		int i = 0;
		for (String row : rows) {
			if (row == null || row.trim().equals("")) {
				continue;
			}
			list.add(row.toCharArray());
		}
		char[][] sokobanMap = new char[list.size()][];
		i = 0;
		for (char[] row : list) {
			sokobanMap[i++] = row;
		}
		SokobanMap map = new SokobanMap(sokobanMap);
		return map;
	}

	private static void validateMapData(SokobanMap sokobanMap) {
		validateAmt(sokobanMap.sokobanMap);
		validateFenceClose(sokobanMap);
	}

	private static void validateFenceClose(SokobanMap soMap) {
		LinkedList<Position> processPositions = new LinkedList<Position>();
		Set<Position> scanedPositions = new HashSet<Position>();
		Position man = soMap.getManPosition();
		validBorder(man, soMap);
		processPositions.add(man);
		Position current = null;
		while ((current = processPositions.poll()) != null) {
			for (Direction d : Direction.values()) {
				Position neighbor = soMap.getNeighbor(current, d);
				if (scanedPositions.contains(neighbor) || soMap.getValue(neighbor) == FENCE_CHAR) {
					continue;
				}
				validBorder(neighbor, soMap);
				scanedPositions.add(neighbor);
				processPositions.addLast(neighbor);
			}
		}
	}

	private static void validBorder(Position p, SokobanMap vMap) {
		if (vMap.getValue(p) != SokobanMap.FENCE_CHAR && onBorder(p, vMap.sokobanMap)) {
			throw new RuntimeException("The map is not closed wrap with the fence!");
		}
	}

	private static boolean onBorder(Position current, char[][] vMap) {
		if (current.getRow() == 0 || current.getColumn() == 0) {
			return true;
		}
		if (current.getRow() == vMap.length - 1 || current.getColumn() == vMap[current.getRow()].length - 1) {
			return true;
		}
		return false;
	}

	private static void validateAmt(char[][] vMap) {
		int manAmt = 0, boxAmt = 0, aimAmt = 0;
		for (char[] row : vMap) {
			for (char cell : row) {
				switch (cell) {
				case SokobanMap.AIM_CHAR:
					aimAmt++;
					break;
				case SokobanMap.BOX_CHAR:
					boxAmt++;
					break;
				case SokobanMap.MAN_CHAR:
					manAmt++;
					break;
				case SokobanMap.MAN_AIM_CHAR:
					manAmt++;
					aimAmt++;
					break;
				case SokobanMap.BOX_AIM_CHAR:
					boxAmt++;
					aimAmt++;
					break;
				}
			}
		}
		if (manAmt != 1) {
			throw new RuntimeException(
					"man only should have one man control position!!! now the man position amount is:" + manAmt);
		}
		if (boxAmt != aimAmt) {
			throw new RuntimeException("BOX char amount is not equels AIM char amount!!! BOX amt:" + boxAmt
					+ ",AIM amt:" + aimAmt);
		}
	}

	public boolean success() {
		for (Position p : aims) {
			if (getValue(p) != BOX_AIM_CHAR) {
				return false;
			}
		}
		return true;
	}

	public boolean deadBox(Position p) {
		if (this.getValue(p) == BOX_AIM_CHAR || this.getValue(p) == AIM_CHAR) {
			return false;
		}
		if (getValue(getNeighbor(p, Direction.UP)) == FENCE_CHAR
				&& getValue(getNeighbor(p, Direction.RIGHT)) == FENCE_CHAR) {
			return true;
		}
		if (getValue(getNeighbor(p, Direction.UP)) == FENCE_CHAR
				&& getValue(getNeighbor(p, Direction.LEFT)) == FENCE_CHAR) {
			return true;
		}
		if (getValue(getNeighbor(p, Direction.DOWN)) == FENCE_CHAR
				&& getValue(getNeighbor(p, Direction.RIGHT)) == FENCE_CHAR) {
			return true;
		}
		if (getValue(getNeighbor(p, Direction.DOWN)) == FENCE_CHAR
				&& getValue(getNeighbor(p, Direction.LEFT)) == FENCE_CHAR) {
			return true;
		}
		return false;
	}

	public boolean move(Direction direction) {
		boolean succ = false;
		Position neighbor = this.getNeighbor(manPosition, direction);
		if (canPush(neighbor, direction)) {
			pushChangeChar(neighbor, direction);
			pushChangeChar(manPosition, direction);
			manPosition = neighbor;
			succ = true;
		}
		// System.out.println("move " + direction + ":" + succ);
		return succ;
	}

	private void pushChangeChar(Position p, Direction direction) {
		char v = getValue(p);
		if (v == AIM_CHAR || v == EMPTY_CHAR || v == FENCE_CHAR) {
			return;
		}
		leaveChangeChar(p);
		if (v == MAN_AIM_CHAR || v == MAN_CHAR) {
			arriveChangeChar(getNeighbor(p, direction), MAN_CHAR);
		} else {
			arriveChangeChar(getNeighbor(p, direction), BOX_CHAR);
		}
	}

	private void arriveChangeChar(Position p, char arr) {
		char v = getValue(p);
		switch (v) {
		case AIM_CHAR:
			if (arr == MAN_CHAR) {
				setValue(p, MAN_AIM_CHAR);
			} else {
				setValue(p, BOX_AIM_CHAR);
			}
			return;
		case EMPTY_CHAR:
			setValue(p, arr);
			return;
		}
	}

	private void leaveChangeChar(Position p) {
		char v = getValue(p);
		switch (v) {
		case MAN_CHAR:
			setValue(p, EMPTY_CHAR);
			return;
		case MAN_AIM_CHAR:
			setValue(p, AIM_CHAR);
			return;
		case BOX_CHAR:
			setValue(p, EMPTY_CHAR);
			return;
		case BOX_AIM_CHAR:
			setValue(p, AIM_CHAR);
			return;
		}
	}

	public LinkedList<LinkedList<Direction>> spreadBestPaths() throws CloneNotSupportedException {
		LinkedList<LinkedList<Direction>> bestPaths = new LinkedList<LinkedList<Direction>>();
		SokobanMap temMap = (SokobanMap) this.clone();
		LinkedList<Position> toSpread = new LinkedList<Position>();
		toSpread.add(manPosition);
		temMap.setValue(manPosition, SokobanMap.PATH_START_NUM);
		Position current = null;
		while ((current = toSpread.poll()) != null) {
			for (Direction d : Direction.values()) {
				Position neighbor = temMap.getNeighbor(current, d);
				if (temMap.getValue(neighbor) == SokobanMap.EMPTY_CHAR
						|| temMap.getValue(neighbor) == SokobanMap.AIM_CHAR) {
					temMap.setValue(neighbor, (char) (temMap.getValue(current) + 1));
					toSpread.add(neighbor);
				} else if (temMap.getValue(neighbor) == SokobanMap.BOX_CHAR
						|| temMap.getValue(neighbor) == SokobanMap.BOX_AIM_CHAR) {
					if (temMap.canPush(neighbor, d) && false == temMap.deadBox(temMap.getNeighbor(neighbor, d))) {
						LinkedList<Direction> bestPath = temMap.backBestPath(current, d);
						bestPaths.add(bestPath);
					}
				}
			}
		}
		return bestPaths;
	}

	public static String getComment() {
		return "// You can design your own map.\n" + //
				"// MAN_CHAR:"
				+ SokobanMap.MAN_CHAR
				+ "\n"
				+ //
				"// SokobanMap.BOX_CHAR:"
				+ SokobanMap.BOX_CHAR
				+ "\n"
				+ //
				"// FENCE_CHAR:"
				+ SokobanMap.FENCE_CHAR
				+ "\n"
				+ //
				"// AIM_CHAR:"
				+ SokobanMap.AIM_CHAR
				+ "\n"
				+ //
				"// MAN_AIM_CHAR:"
				+ SokobanMap.MAN_AIM_CHAR
				+ "\n"
				+ //
				"// BOX_AIM_CHAR:"
				+ SokobanMap.BOX_AIM_CHAR
				+ "\n"
				+ //
				"// EMPTY_POSITION_CHAR:"
				+ SokobanMap.EMPTY_CHAR
				+ "\n\n"
				+ //
				"// Use \"//\" to comment. Empty lines will be ignored.\n"
				+ //
				"// One simple map:\n\n"
				+ //
				"   " + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR
				+ SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR
				+ "\n"
				+ //
				"   " + SokobanMap.FENCE_CHAR + SokobanMap.MAN_CHAR + SokobanMap.BOX_CHAR + SokobanMap.EMPTY_CHAR
				+ SokobanMap.AIM_CHAR + SokobanMap.FENCE_CHAR + "\n"
				+ //
				"   " + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR
				+ SokobanMap.FENCE_CHAR + SokobanMap.FENCE_CHAR + "\n";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Object obj = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception e) {
			return new RuntimeException(e);
		}
		return obj;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (char[] cArr : sokobanMap) {
			for (char c : cArr) {
				sb.append(c);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof SokobanMap) {
			SokobanMap m = (SokobanMap) obj;
			return this.toString().equals(m.toString());
		}
		return false;
	}
}
