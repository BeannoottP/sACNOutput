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
		DatagramPacket datagram = new DatagramPacket(packet, packet.length, multicastAddress, port);
		System.out.println("sending");

			socket.send(datagram);
			for (byte b : packet) {
	            System.out.print((b & 0xFF) + " ");  // Prints each byte as a decimal value
	        }
		socket.close();
		System.out.println("sACN packet sent.");
		}
	}

	private static byte[] createSACNPacket(int universe, byte[] dmxData) {
	    final int DMX_SLOT_COUNT = dmxData.length;
	    final int ROOT_LAYER_LENGTH = 38;
	    final int FRAMING_LAYER_LENGTH = 77;
	    final int DMP_LAYER_HEADER_LENGTH = 11; // not including DMX data
	    final int DMX_START_CODE_LENGTH = 1;

	    int totalLength = ROOT_LAYER_LENGTH + FRAMING_LAYER_LENGTH + DMP_LAYER_HEADER_LENGTH + DMX_START_CODE_LENGTH + DMX_SLOT_COUNT;
	    byte[] packet = new byte[totalLength];

	    UUID cid = UUID.randomUUID();
	    String sourceName = "Java sACN";

	    // Root Layer (offsets 0 to 37)
	    packet[0] = 0x00; packet[1] = 0x10; // Preamble Size
	    packet[2] = 0x00; packet[3] = 0x00; // Post-amble Size
	    byte[] acnId = {0x41, 0x53, 0x43, 0x2d, 0x45, 0x31, 0x2e, 0x31, 0x37, 0x00, 0x00, 0x00};
	    System.arraycopy(acnId, 0, packet, 4, acnId.length);

	    packet[16] = (byte)0x70; // Flags and Length (high nibble = 7)
	    packet[17] = (byte)((totalLength - 16) >> 8);
	    packet[18] = (byte)((totalLength - 16) & 0xFF);

	    packet[19] = 0x00; // reserved
	    packet[20] = 0x00; packet[21] = 0x00; packet[22] = 0x00; packet[23] = 0x04; // Root Vector (Data Packet)
	    byte[] cidBytes = new byte[16];
	    long msb = cid.getMostSignificantBits();
	    long lsb = cid.getLeastSignificantBits();
	    for (int i = 0; i < 8; i++) {
	        cidBytes[i] = (byte)(msb >> (8 * (7 - i)));
	        cidBytes[8 + i] = (byte)(lsb >> (8 * (7 - i)));
	    }
	    System.arraycopy(cidBytes, 0, packet, 24, 16);

	    // Framing Layer (offsets 38 to 114)
	    int framingOffset = 38;
	    int framingLength = totalLength - framingOffset;
	    packet[framingOffset] = (byte)0x70;
	    packet[framingOffset + 1] = (byte)(framingLength >> 8);
	    packet[framingOffset + 2] = (byte)(framingLength & 0xFF);
	    packet[framingOffset + 3] = 0x00;
	    packet[framingOffset + 4] = 0x00; packet[framingOffset + 5] = 0x00; packet[framingOffset + 6] = 0x02; // Vector
	    byte[] nameBytes = sourceName.getBytes();
	    System.arraycopy(nameBytes, 0, packet, framingOffset + 7, Math.min(nameBytes.length, 64));
	    packet[framingOffset + 71] = 100; // Priority
	    packet[framingOffset + 73] = 1;   // Sequence Number
	    packet[framingOffset + 77] = (byte)((universe >> 8) & 0xFF);
	    packet[framingOffset + 78] = (byte)(universe & 0xFF);

	    // DMP Layer (offsets 115 to end)
	    int dmpOffset = framingOffset + FRAMING_LAYER_LENGTH;
	    int dmpLength = totalLength - dmpOffset;
	    packet[dmpOffset] = (byte)0x70;
	    packet[dmpOffset + 1] = (byte)(dmpLength >> 8);
	    packet[dmpOffset + 2] = (byte)(dmpLength & 0xFF);
	    packet[dmpOffset + 3] = 0x02;  // DMP Vector
	    packet[dmpOffset + 4] = (byte)0xA1; // Address & Data Type
	    packet[dmpOffset + 5] = 0x00; packet[dmpOffset + 6] = 0x00; // First Property Address
	    packet[dmpOffset + 7] = 0x00; packet[dmpOffset + 8] = 0x01; // Address Increment
	    packet[dmpOffset + 9] = (byte)(((DMX_SLOT_COUNT + 1) >> 8) & 0xFF); // +1 for start code
	    packet[dmpOffset + 10] = (byte)((DMX_SLOT_COUNT + 1) & 0xFF);

	    packet[dmpOffset + 11] = 0x00; // DMX Start Code
	    System.arraycopy(dmxData, 0, packet, dmpOffset + 12, DMX_SLOT_COUNT);

	    return packet;
	}
}
