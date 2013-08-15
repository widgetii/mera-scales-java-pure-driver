package ru.aplix.mera.util;


public class CyclicBuffer<T> {

	private final T[] items;
	private int length;

	public CyclicBuffer(T[] items) {
		if (items.length == 0) {
			throw new IllegalArgumentException("Empty cyclic buffer");
		}
		this.items = items;
	}

	public final T[] items() {
		return this.items;
	}

	public final int capacity() {
		return this.items.length;
	}

	public final int length() {
		return this.length;
	}

	public void add(T item) {
		if (this.length < this.items.length) {
			this.items[this.length++] = item;
			return;
		}

		final int last = this.items.length - 1;

		System.arraycopy(this.items, 1, this.items, 0, last);
		this.items[last] = item;
	}

	public final T last() {
		if (this.length == 0) {
			return null;
		}
		return this.items[this.length - 1];
	}

	public final void clear() {
		this.length = 0;
	}

	@Override
	public String toString() {
		if (this.items == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('[');
		for (int i = 0; i < this.length; ++i) {
			if (i > 0) {
				out.append(", ");
			}
			out.append(this.items[i]);
		}
		out.append(']');

		return out.toString();
	}

}
