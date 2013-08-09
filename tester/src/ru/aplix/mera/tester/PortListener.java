package ru.aplix.mera.tester;

import java.util.EventListener;


public interface PortListener extends EventListener {

	void portDeselected(PortOption port);

	void portSelected(PortOption port);

}
