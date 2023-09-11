package de.kherud.llama;

import java.nio.IntBuffer;


/**
 * Container that allows slicing an {@link IntBuffer}
 * with arbitrary slicing lengths on Java versions older than 13.
 * Does not extend IntBuffer because the super constructor
 * requires memory segment proxies, and we can't access the delegate's.
 * Does not implement Buffer because the {@link java.nio.Buffer#slice(int, int)}
 * method is specifically blocked from being implemented or used on older jdk versions.
 * Unfortunately this can't be generified with {@link SliceableByteBuffer}, since there is no shared interface.
 */
class SliceableIntBuffer {
	final IntBuffer delegate;

	private final int offset;

	private final int capacity;

	SliceableIntBuffer(IntBuffer delegate) {
		this.delegate = delegate;
		this.capacity = delegate.capacity();
		this.offset = 0;
	}

	SliceableIntBuffer(IntBuffer delegate, int offset, int capacity) {
		this.delegate = delegate;
		this.offset = offset;
		this.capacity = capacity;
	}

	SliceableIntBuffer slice(int offset, int length) {
		// Where the magic happens
		// Wrapping is equivalent to the slice operation so long
		// as you keep track of your offsets and capacities.
		// So, we use this container class to track those offsets and translate
		// them to the correct frame of reference.
		return new SliceableIntBuffer(
				IntBuffer.wrap(
						this.delegate.array(),
						this.offset + offset,
						length
				),
				this.offset + offset,
				length
		);

	}

	int capacity() {
		return capacity;
	}

	SliceableIntBuffer put(int index, int i) {
		delegate.put(offset + index, i);
		return this;
	}

	int get(int index) {
		return delegate.get(offset + index);
	}

	void clear() {
		// Clear set the limit and position
		// to 0 and capacity respectively,
		// but that's not what the buffer was initially
		// after the wrap() call, so we manually
		// set the limit and position to what they were
		// after the wrap call.
		delegate.clear();
		delegate.limit(offset + capacity);
		delegate.position(offset);
	}

}
