package infiniteinvo.client.inventory;

import infiniteinvo.core.InfiniteInvo.II_Settings;
import infiniteinvo.core.InfiniteInvo;
import infiniteinvo.core.InvoPacket;
import infiniteinvo.core.XPHelper;
import infiniteinvo.inventory.BigInventoryPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

public class GuiButtonUnlockSlot extends GuiButton
{
	EntityPlayer player;
	
	public GuiButtonUnlockSlot(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, EntityPlayer player)
	{
		super(p_i1020_1_, p_i1020_2_, p_i1020_3_, "");
		this.player = player;
		this.enabled = false;
	}
	
	public GuiButtonUnlockSlot(int p_i1021_1_, int p_i1021_2_, int p_i1021_3_, int p_i1021_4_, int p_i1021_5_, EntityPlayer player)
	{
		super(p_i1021_1_, p_i1021_2_, p_i1021_3_, p_i1021_4_, p_i1021_5_, "");
		this.player = player;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		this.visible = InfiniteInvo.II_Settings.xpUnlock;
		int cost = (InfiniteInvo.II_Settings.unlockCost + (player.getEntityData().getInteger("INFINITE_INVO_UNLOCKED") * InfiniteInvo.II_Settings.unlockIncrease));
		
		if(player.inventory instanceof BigInventoryPlayer)
		{ 
			this.enabled = XPHelper.getPlayerXP(player) >= (InfiniteInvo.II_Settings.useOrbs? cost : XPHelper.getLevelXP(cost)) && InfiniteInvo.II_Settings.xpUnlock && ((BigInventoryPlayer)player.inventory).getUnlockedSlots() - 9 < InfiniteInvo.II_Settings.invoSize;
		} else
		{
			this.enabled = false;
		}
		
		this.displayString = this.enabled? StatCollector.translateToLocal("infiniteinvo.unlockslot") : (InfiniteInvo.II_Settings.useOrbs? XPHelper.getPlayerXP(player) : player.experienceLevel) + " / " + cost + " XP";
		
		super.drawButton(mc, mx, my);
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my)
    {
		boolean flag = super.mousePressed(mc, mx, my);
		
		if(flag)
		{
			enabled = false;
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 0);
			tags.setInteger("World", player.worldObj.provider.dimensionId);
			tags.setString("Player", player.getCommandSenderName());
			tags.setInteger("InvoSize", InfiniteInvo.II_Settings.invoSize);
			InfiniteInvo.instance.network.sendToServer(new InvoPacket(tags));
		}
		
		return flag;
    }
}
