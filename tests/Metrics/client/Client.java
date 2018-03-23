package Metrics.client;

import Metrics.provider.*;

public class Client {
    private Client() {
	// Do nothing
    }

    public static void m1() {
	Provider.m1();
    }
}
