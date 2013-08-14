package ru.aplix.mera.scales.ap;

import static purejavacomm.CommPortIdentifier.getPortIdentifiers;
import static ru.aplix.mera.scales.ap.AutoProtocol.AUTO_PROTOCOL;

import java.util.ArrayList;
import java.util.Enumeration;

import purejavacomm.CommPortIdentifier;
import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


public class APBackendFactory implements ScalesBackendFactory {

	@Override
	public ScalesProtocol getScalesProtocol() {
		return AUTO_PROTOCOL;
	}

	@Override
	public String[] scalesPortIds() {

		final Enumeration<CommPortIdentifier> ids =
				getPortIdentifiers();
		final ArrayList<String> portIds = new ArrayList<>();

		while (ids.hasMoreElements()) {
			portIds.add(ids.nextElement().getName());
		}

		return portIds.toArray(new String[portIds.size()]);
	}

}
