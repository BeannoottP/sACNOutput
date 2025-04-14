package main;

import java.lang.reflect.Array;
import java.net.*;
import java.util.Arrays;
import java.util.UUID;

public class Test {

	public static void main(String[] args) throws Exception {
		while(true) {
		InetAddress multicastAddress = InetAddress.getByName("127.0.0.1");
		int port = 5568;
		byte[] packet = createSACNPacket(1, new byte[] { (byte)255, (byte)128, (byte)64 }); // RGB data

		DatagramSocket socket = new DatagramSocket();
		
		Manager m = new Manager();
		Packet p = new Packet(m, 1, 2);
		DatagramPacket datagram = new DatagramPacket(p.rootLayer, p.rootLayer.length, multicastAddress, port);
		System.out.println("sending");
		while (true) {
			socket.send(datagram);
		}
		//socket.close();
		//System.out.println("sACN packet sent.");
		}
	}

	private static byte[] createSACNPacket(int universe, byte[] dmxData) {
		return null;
	}
}
