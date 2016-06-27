package models;

import java.util.Objects;

/**
 * 
 * Stolen from
 * http://stackoverflow.com/questions/521171/a-java-collection-of-value
 * -pairs-tuples
 */
@SuppressWarnings("javadoc")
public class Pair<L, R> {

	private final L left;
	private final R right;

	public Pair() {
		this.left = null;
		this.right = null;
	}

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(left) ^ Objects.hashCode(right);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Pair))
			return false;
		@SuppressWarnings("rawtypes")
		Pair pairo = (Pair) o;
		return this.left.equals(pairo.getLeft())
				&& this.right.equals(pairo.getRight());
	}

}