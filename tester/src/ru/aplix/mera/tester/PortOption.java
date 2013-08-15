package ru.aplix.mera.tester;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.*;
import ru.aplix.mera.scales.config.ScalesConfig;


public final class PortOption
		implements Comparable<PortOption>,
		MeraConsumer<ScalesPortHandle, ScalesStatusMessage> {

	private final TesterApp app;
	private final ScalesPortId portId;
	private final String[] idWords;
	private final ScalesErrorListener errorListener;
	private ScalesPortHandle handle;

	PortOption(TesterApp app) {
		this.app = app;
		this.portId = null;
		this.idWords = new String[0];
		this.errorListener = null;
	}

	PortOption(TesterApp app, ScalesPortId portId) {
		this.app = app;
		this.portId = portId;
		this.idWords = splitId(portId.getPortId());
		this.errorListener = new ScalesErrorListener(app);
	}

	public final ScalesPortId getPortId() {
		return this.portId;
	}

	public final ScalesPortHandle getHandle() {
		return this.handle;
	}

	public final void select() {
		if (getPortId() == null) {
			return;
		}
		this.app.log("Порт: " + this);

		final ScalesPort port = getPortId().getPort();

		updateConfig(
				this.app.getContent()
				.getToolBar()
				.getPortSelector()
				.getConfig());
		this.handle = port.subscribe(this);
	}

	public final void deselect() {
		if (getPortId() == null) {
			return;
		}
		this.handle.unsubscribe();
		this.app.log("Отключено\n");
	}

	@Override
	public void consumerSubscribed(ScalesPortHandle handle) {
		this.handle = handle;
		handle.listenForErrors(this.errorListener);
	}

	@Override
	public void messageReceived(ScalesStatusMessage message) {
	}

	@Override
	public void consumerUnsubscribed(ScalesPortHandle handle) {
	}

	@Override
	public int compareTo(PortOption o) {

		final ScalesPortId pid1 = getPortId();
		final ScalesPortId pid2 = o.getPortId();

		if (pid1 == null) {
			return pid2 == null ? 0 : -1;
		}
		if (pid2 == null) {
			return 1;
		}

		final String proto1 = pid1.getProtocol().getProtocolId();
		final String proto2 = pid2.getProtocol().getProtocolId();
		final int protoCmp = proto1.compareTo(proto2);

		if (protoCmp != 0) {
			return protoCmp;
		}

		final String[] id1 = this.idWords;
		final String[] id2 = o.idWords;
		final int len = Math.min(id1.length, id2.length);

		for (int i = 0; i < len; ++i) {

			final int cmp = id1[i].compareTo(id2[i]);

			if (cmp != 0) {
				return cmp;
			}
		}

		return id1.length - id2.length;
	}

	@Override
	public int hashCode() {
		if (this.portId == null) {
			return 0;
		}

		final int prime = 31;
		int result = 1;
		result = prime * result + this.portId.getProtocol().hashCode();
		result = prime * result + Arrays.hashCode(this.idWords);

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final PortOption other = (PortOption) obj;

		if (this.portId == null) {
			if (other.portId != null) {
				return false;
			}
		} else if (!this.portId.getProtocol().equals(
				other.portId.getProtocol())) {
			return false;
		}
		if (!Arrays.equals(this.idWords, other.idWords)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.portId == null) {
			return "Порт";
		}
		return this.portId.toString();
	}

	void updateConfig(ScalesConfig config) {

		final ScalesPortId portId = getPortId();

		if (portId == null) {
			return;
		}

		final ScalesPort port = portId.getPort();
		final ScalesConfig oldConfig = port.getConfig();
		final ScalesConfig newConfig = oldConfig.update(config);

		if (newConfig != oldConfig) {
			port.setConfig(newConfig);
		}
	}

	private static String[] splitId(String id) {

		final int len = id.length();
		final StringBuilder word = new StringBuilder(len);
		ArrayList<String> words = null;
		boolean number = false;

		for (int i = 0; i < len; ++i) {

			final char c = id.charAt(i);

			if (Character.isWhitespace(c)) {
				if (word.length() != 0) {
					words = endWord(word, words);
				}
				continue;
			}
			if (word.length() == 0) {
				word.append(c);
				number = Character.isDigit(c);
				continue;
			}
			if (Character.isDigit(c) == number) {
				word.append(c);
				continue;
			}
			number = !number;
			words = endWord(word, words);
			word.append(c);
		}

		if (words == null) {
			return new String[] {word.toString()};
		}
		if (word.length() != 0) {
			words = endWord(word, words);
		}

		return words.toArray(new String[words.size()]);
	}

	private static ArrayList<String> endWord(
			StringBuilder word,
			ArrayList<String> words) {

		final ArrayList<String> result;

		if (words == null) {
			result = new ArrayList<>();
		} else {
			result = words;
		}
		result.add(word.toString());
		word.setLength(0);

		return result;
	}

	private static final class ScalesErrorListener
			implements MeraConsumer<ScalesErrorHandle, ScalesErrorMessage> {

		private final TesterApp app;

		ScalesErrorListener(TesterApp app) {
			this.app = app;
		}

		@Override
		public void consumerSubscribed(ScalesErrorHandle handle) {
		}

		@Override
		public void messageReceived(ScalesErrorMessage message) {

			final Throwable cause = message.getCause();

			if (cause == null) {
				this.app.log("ERROR: " + message.getErrorMessage());
				return;
			}
			logStackTrace(cause);
		}

		@Override
		public void consumerUnsubscribed(ScalesErrorHandle handle) {
		}

		private void logStackTrace(Throwable cause) {

			final StringWriter out = new StringWriter();
			final PrintWriter writer = new PrintWriter(out);

			try {
				writer.print("ERROR: ");
				cause.printStackTrace(writer);
			} finally {
				writer.close();
			}

			this.app.log(out.toString());
		}

	}

}