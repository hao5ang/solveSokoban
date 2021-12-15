package solveSokoban;

import java.io.Serializable;

public class Position implements Serializable {
	private int row;
	private int column;

	public Position() {

	}

	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public Position(Position p) {
		this.row = p.row;
		this.column = p.column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public int hashCode() {
		return this.row * this.column + this.column;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof Position) {
			Position p = (Position) obj;
			return p.getRow() == this.getRow() && p.getColumn() == this.getColumn();
		}
		return false;
	}

	@Override
	public String toString() {
		return "position[" + this.row + "," + this.column + "]";
	}

}
