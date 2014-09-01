package solveSokoban;

public enum Direction {
	LEFT, RIGHT, UP, DOWN;

	public static Direction revert(Direction d) {
		switch (d) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			throw new RuntimeException("unsupported direction:" + d);
		}
	}
}
