package main;

import java.lang.reflect.Array;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Test {

	public static void main(String[] args) throws Exception {
		while(true) {
		InetAddress multicastAddress = InetAddress.getByName("239.255.0.1");
		int port = 5568;
		byte[] packet = createSACNPacket(1, new byte[] { (byte)255, (byte)128, (byte)64 }); // RGB data

		DatagramSocket socket = new DatagramSocket();
		
		Manager m = new Manager();
		byte[] data = new byte[512];
		new Random().nextBytes(data);
		Packet dmx = new Packet(m, 1, 1, data);
		Packet q = new Packet(m, 3, 1, data);
		DatagramPacket dmxSender = new DatagramPacket(dmx.fullPacket, dmx.fullPacket.length, multicastAddress, port);
		DatagramPacket univSender = new DatagramPacket(q.fullPacket, q.fullPacket.length, multicastAddress, port);
		System.out.println("sending");
		while (true) {
			TimeUnit.MILLISECONDS.sleep(300);
			dmx = new Packet(m, 1, 1, data);
			q = new Packet(m, 3, 1, data);
			dmxSender = new DatagramPacket(dmx.fullPacket, dmx.fullPacket.length, multicastAddress, port);
			univSender = new DatagramPacket(q.fullPacket, q.fullPacket.length, multicastAddress, port);
			socket.send(dmxSender);
			TimeUnit.MILLISECONDS.sleep(300);
			dmx = new Packet(m, 1, 1, data);
			q = new Packet(m, 3, 1, data);
			dmxSender = new DatagramPacket(dmx.fullPacket, dmx.fullPacket.length, multicastAddress, port);
			univSender = new DatagramPacket(q.fullPacket, q.fullPacket.length, multicastAddress, port);
			socket.send(dmxSender);
			TimeUnit.MILLISECONDS.sleep(300);
			dmx = new Packet(m, 1, 1, data);
			q = new Packet(m, 3, 1, data);
			dmxSender = new DatagramPacket(dmx.fullPacket, dmx.fullPacket.length, multicastAddress, port);
			univSender = new DatagramPacket(q.fullPacket, q.fullPacket.length, multicastAddress, port);
			socket.send(dmxSender);
			TimeUnit.MILLISECONDS.sleep(300);
			dmx = new Packet(m, 1, 1, data);
			q = new Packet(m, 3, 1, data);
			dmxSender = new DatagramPacket(dmx.fullPacket, dmx.fullPacket.length, multicastAddress, port);
			univSender = new DatagramPacket(q.fullPacket, q.fullPacket.length, multicastAddress, port);
			socket.send(dmxSender);
			socket.send(univSender);
		}
		//socket.close();
		//System.out.println("sACN packet sent.");
		}
	}

	private static byte[] createSACNPacket(int universe, byte[] dmxData) {
		return null;
	}
}
