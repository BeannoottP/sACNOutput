package main;

import java.util.Arrays;

public class Packet {
	
	final static int PACKET_TYPE_DATA = 1;
	final static int PACKET_TYPE_SYNC = 2;
	final static int PACKET_TYPE_UNIVERSE = 3;
	
	int type;
	int universe;
	
	Manager manager;
	
	byte[] rootLayer;
	byte[] framingLayer;
	byte[] pduLayer;
	
	byte[] dmxData;

	byte[] fullPacket;
	
	public Packet(Manager m, int t, int u, byte[] data) {
		//save dmx data, universes, type, and more?
		type = t;
		manager = m;
		universe = u;
		
		dmxData = data;
		
		createRootLayer();
		createFramingLayer();
		if (type != 2) {
			createPDULayer();
		}
		//todo final packet adjustments see below
		createFullPacket();
		
		//TODO dynamic size adjustment??
	}

	private void createRootLayer() {
		rootLayer = new byte[38];
		rootLayer[0] = 0x00; rootLayer[1] = 0x10; //preamble size
		rootLayer[2] = 0x00; rootLayer[3] = 0x00; //postamble size
		
		//ACN Packet Identifier E1.19
		rootLayer[4] = 0x41; rootLayer[5] = 0x53; rootLayer[6] = 0x43; rootLayer[7] = 0x2d;
		rootLayer[8] = 0x45; rootLayer[9] = 0x31; rootLayer[10] = 0x2e; rootLayer[11] =  0x31;
		rootLayer[12] = 0x37; rootLayer[13] = 0x00; rootLayer[14] = 0x00; rootLayer[15] = 0x00;
		
		//flag + size + vector
		switch (type) {
			case PACKET_TYPE_DATA: rootLayer[16] = 0x72; rootLayer[17] = 0x6e; //full packet is always sent
								   rootLayer[18] = 0x00; rootLayer[19] = 0x00; rootLayer[20] = 0x00; rootLayer[21] = 0x04; break; //data vector 
			case PACKET_TYPE_SYNC: rootLayer[16] = 0x70; rootLayer[17] = 0x21; //static size always
								   rootLayer[18] = 0x00; rootLayer[19] = 0x00; rootLayer[20] = 0x00; rootLayer[21] = 0x08; break; //extended vector
			case PACKET_TYPE_UNIVERSE: 	int flagsAndLength = (0x7 << 12) | (120 - 16 + (manager.universeList.size() * 2));
								   rootLayer[16] = (byte) ((flagsAndLength >> 8) & 0xFF);
								   rootLayer[17] = (byte) (flagsAndLength & 0xFF);
								   rootLayer[18] = 0x00; rootLayer[19] = 0x00; rootLayer[20] = 0x00; rootLayer[21] = 0x08; break; //extended vector + size set dynamically later based on final size
		}
		
		//UUID
		long msb = manager.CID.getMostSignificantBits();
	    long lsb = manager.CID.getLeastSignificantBits();
	    for (int i = 22; i < 30; i++) {
	        rootLayer[i] = (byte)(msb >> (8 * (7 - i)));
	        rootLayer[8 + i] = (byte)(lsb >> (8 * (7 - i)));
	    }
		
	}
	
	private void createFramingLayer() {
		switch (type) {
			case PACKET_TYPE_DATA: 
				framingLayer = new byte[77];
				framingLayer[0] = 0x72; framingLayer[1] = 0x58; //flags + fixed length 638 - 38 = 600
				framingLayer[2] = 0x00; framingLayer[3] = 0x00; framingLayer[4] = 0x00; framingLayer[5] = 0x02; // DATA_PACKET vector
				System.arraycopy(stringToByte("sACNaroni Java"), 0, framingLayer, 6, 64);
				framingLayer[70] = 0x64; //prio, defaulted to 100 TODO dynamic prio
				framingLayer[71] = 0x00; framingLayer[72] = 0x00; //TODO syncing address, this discards any syncing ideas
				framingLayer[73] = (byte) manager.universeList.get(universe)[0]; //sequence num
				manager.universeList.get(universe)[0]++;
				framingLayer[74] = 0x00; //TODO options
				
				//universe
				framingLayer[75] = (byte) ((universe >> 8) & 0xFF);
				framingLayer[76] = (byte) (universe & 0xFF);
				break;
				
			case PACKET_TYPE_SYNC: //TODO
				framingLayer = new byte[11];
				framingLayer[0] = 0x70; framingLayer[1] = 0x0b; //flags + fixed length 49 - 38 = 11
				framingLayer[2] = 0x00; framingLayer[3] = 0x00; framingLayer[4] = 0x00; framingLayer[5] = 0x01; // EXTENDED_SYNCHRONIZATION vector
				break; //TODO will finish later
				
			case PACKET_TYPE_UNIVERSE: 
				framingLayer = new byte[74];
				int flagsAndLength = (0x7 << 12) | (120 - 38 + (manager.universeList.size() * 2));
				framingLayer[0] = (byte) ((flagsAndLength >> 8) & 0xFF);
				framingLayer[1] = (byte) (flagsAndLength & 0xFF);
				framingLayer[2] = 0x00; framingLayer[3] = 0x00; framingLayer[4] = 0x00; framingLayer[5] = 0x02; // EXTENDED_DISCOVERY vector
				System.arraycopy(stringToByte("sACNaroni Java"), 0, framingLayer, 6, 64);
				break;
				
		}
		
	}
	
	private void createPDULayer() {
		switch (type) {
		case PACKET_TYPE_DATA:
			pduLayer = new byte[523];
			//flags and static length of 523
			pduLayer[0] = 0x72; pduLayer[1] = 0x0B;
			pduLayer[2] = 0x02; //vector given by standard
			pduLayer[3] = (byte) 0xa1; //data type give by standard
			//first property address
			pduLayer[4] = 0x00; pduLayer[5] = 0x00;
			//increment value
			pduLayer[6] = 0x00; pduLayer[7] = 0x01;
			// size of data delivery + start code, statically 513
			pduLayer[8] = 0x02; pduLayer[9] = 0x01;
			//start code + dmx data !!!!
			pduLayer[10] = 0x00; //standard start code
			System.arraycopy(dmxData, 0, pduLayer, 11, 512);
			break;
			
		case PACKET_TYPE_SYNC:
			pduLayer = new byte[0];
			break;
		
		case PACKET_TYPE_UNIVERSE:
			pduLayer = new byte[8 + (manager.universeList.size() * 2)];
			
			int flagsAndLength = (0x7 << 12) | pduLayer.length;
			pduLayer[0] = (byte) ((flagsAndLength >> 8) & 0xFF);
			pduLayer[1] = (byte) (flagsAndLength & 0xFF);
			
			pduLayer[2] = 0x00; pduLayer[3] = 0x00; pduLayer[4] = 0x00; pduLayer[5] = 0x01; //vector UNIVERSE_DISCOVERY_UNIVERSE_LIST
			//page number and final page TODO more than one page
			pduLayer[6] = 0x00; pduLayer[7] = 0x00;
			Integer[] sortedUniverses = manager.universeList.keySet().toArray(new Integer[0]);
			Arrays.sort(sortedUniverses);
			for (int i = 0; i < sortedUniverses.length; i++) {
			    pduLayer[8 + i * 2]     = (byte) ((sortedUniverses[i] >> 8) & 0xFF); // High byte
			    pduLayer[8 + i * 2 + 1] = (byte) (sortedUniverses[i] & 0xFF);        // Low byte
			}
			break;
			
			
		}
		
		
		
		
		
		
	}
	

	private void createFullPacket() {
		fullPacket = new byte[rootLayer.length + framingLayer.length + pduLayer.length];
		System.arraycopy(rootLayer, 0, fullPacket, 0, rootLayer.length);
		System.arraycopy(framingLayer, 0, fullPacket, rootLayer.length, framingLayer.length);
		System.arraycopy(pduLayer, 0, fullPacket, rootLayer.length + framingLayer.length, pduLayer.length);
	}

	
	
	
	private byte[] stringToByte(String s) { //this could be optimized massively 
		byte[] returnString = new byte[64];
		for (int i = 0; i < s.length(); i++) {
			returnString[i] = (byte) s.charAt(i);
		}
		return returnString;
	}



}
