package ru.aplix.mera.scales.backend;


class NoWeighing implements Weighing {

	static final Weighing NO_WEIGHING = new NoWeighing();

	private NoWeighing() {
	}

	@Override
	public void start() {
	}

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void stop() {
	}

}
