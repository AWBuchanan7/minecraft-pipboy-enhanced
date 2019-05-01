package infiniteinvo.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import infiniteinvo.core.InfiniteInvo;

public class II_UpdateNotification
{
	boolean hasChecked = false;
	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!InfiniteInvo.proxy.isClient() || hasChecked)
		{
			return;
		}
		
		hasChecked = true;

	}
	
	public int compareVersions(String ver1, String ver2)
	{
		int[] oldNum;
		int[] newNum;
		String[] oldNumString;
		String[] newNumString;
		
		try
		{
			oldNumString = ver1.split("\\.");
			newNumString = ver2.split("\\.");
			
			oldNum = new int[]{Integer.valueOf(oldNumString[0]), Integer.valueOf(oldNumString[1]), Integer.valueOf(oldNumString[2])};
			newNum = new int[]{Integer.valueOf(newNumString[0]), Integer.valueOf(newNumString[1]), Integer.valueOf(newNumString[2])};
		} catch(Exception e)
		{
			return -2;
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(oldNum[i] < newNum[i])
			{
				return -1; // New version available
			} else if(oldNum[i] > newNum[i])
			{
				return 1; // Debug version ahead of release
			}
		}
		
		return 0;
	}
	
}
