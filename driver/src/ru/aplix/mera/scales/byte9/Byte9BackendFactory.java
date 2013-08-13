package ru.aplix.mera.scales.byte9;

import static purejavacomm.CommPortIdentifier.getPortIdentifiers;
import static ru.aplix.mera.scales.byte9.Byte9Protocol.BYTE9_PROTOCOL;

import java.util.ArrayList;
import java.util.Enumeration;

import purejavacomm.CommPortIdentifier;
import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


public class Byte9BackendFactory implements ScalesBackendFactory {

	@Override
	public ScalesProtocol getScalesProtocol() {
		return BYTE9_PROTOCOL;
	}

	@Override
	public String[] scalesPortIds() {

		final Enumeration<CommPortIdentifier> ids =
				getPortIdentifiers();
		final ArrayList<String> deviceIds = new ArrayList<>();

		while (ids.hasMoreElements()) {
			deviceIds.add(ids.nextElement().getName());
		}

		return deviceIds.toArray(new String[deviceIds.size()]);
	}

}
