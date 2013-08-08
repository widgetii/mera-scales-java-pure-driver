package ru.aplix.mera.scales.backend;


interface Weighing {

	void start();

	void suspend();

	void resume();

	void stop();

}
