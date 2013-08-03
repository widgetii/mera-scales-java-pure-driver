package ru.aplix.mera.scales;

import java.util.Arrays;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Scales status updates handle.
 *
 * <p>It can be used to subscribe to other scale message types, such as weight
 * updates. When this subscription revoked, all subscriptions registered
 * through it are also revoked.</p>
 */
public final class ScalesStatusHandle
		extends MeraHandle<ScalesStatusHandle, ScalesStatusMessage> {

	private final ScalesBackend backend;
	private MeraHandle<?, ?>[] handles;

	ScalesStatusHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super ScalesStatusHandle,
					? super ScalesStatusMessage> consumer) {
		super(backend.statusSubscriptions(), consumer);
		this.backend = backend;
	}

	/**
	 * Subscribe on weight updates.
	 *
	 * @param consumer weight updates consumer.
	 *
	 * @return weight updates handle.
	 */
	public final WeightHandle requestWeigth(
			MeraConsumer<
					? super WeightHandle,
					? super WeightMessage> consumer) {
		return addHandle(
				this.backend.weightSubscriptions().subscribe(consumer));
	}

	final void unsubscribeAll() {
		if (this.handles == null) {
			return;
		}
		for (MeraHandle<?, ?> handle : this.handles) {
			handle.unsubscribe();
		}
		this.handles = null;
	}

	private <H extends MeraHandle<H, ?>> H addHandle(H handle) {
		if (this.handles == null) {
			this.handles = new MeraHandle<?, ?>[] {handle};
		} else {

			final int len = this.handles.length;

			this.handles = Arrays.copyOf(this.handles, len + 1);
			this.handles[len] = handle;
		}
		return handle;
	}

}
