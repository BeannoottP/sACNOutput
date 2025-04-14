package main;

import java.util.HashMap;
import java.util.UUID;

public class Manager {
	
	public final UUID CID;
	public HashMap<Integer, int[]> universeList;
	
	
	public Manager() {
		this.CID = UUID.randomUUID();
		universeList = new HashMap<Integer, int[]>();
		universeList.put(1, new int[]{1, 1});
		
	}
	
	
}