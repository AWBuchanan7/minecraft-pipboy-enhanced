package infiniteinvo.handlers;

import infiniteinvo.client.inventory.GuiBigInventory;
import infiniteinvo.client.inventory.GuiButtonUnlockSlot;
import infiniteinvo.client.inventory.InvoScrollBar;
import infiniteinvo.core.InfiniteInvo.II_Settings;
import infiniteinvo.core.InfiniteInvo;
import infiniteinvo.core.InvoPacket;
import infiniteinvo.inventory.BigContainerPlayer;
import infiniteinvo.inventory.BigInventoryPlayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Level;

public class EventHandler
{
	public static File worldDir;
	public static HashMap<String, Integer> unlockCache = new HashMap<String, Integer>();
	public static HashMap<String, Container> lastOpened = new HashMap<String, Container>();
	
	@SubscribeEvent
	public void onEntityConstruct(EntityConstructing event) // More reliable than on entity join
	{
		if(event.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getEntity();
		}
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getEntity();
			
			if(event.getWorld().isRemote)
			{
				NBTTagCompound requestTags = new NBTTagCompound();
				requestTags.setInteger("ID", 1);
				requestTags.setInteger("World", event.getWorld().provider.getDimension());
				requestTags.setString("Player", player.getCommandSenderEntity().getName());
				InfiniteInvo.instance.network.sendToServer(new InvoPacket(requestTags));
			}
		} else if(event.getEntity() instanceof EntityItem)
		{
			EntityItem itemDrop = (EntityItem)event.getEntity();
			
			if(itemDrop.getItem() != null && itemDrop.getItem().getItem() == InfiniteInvo.locked)
			{
				itemDrop.setDead();
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event)
	{
		if(event.pickedUp != null && event.pickedUp.getItem() != null && event.pickedUp.getItem().getItem() == Items.BONE && !event.pickedUp.world.isRemote)
		{
			if(!event.player.getCommandSenderEntity().getName().equals(event.pickedUp.getName()));
			{
				if(event.pickedUp.getName() == null || event.pickedUp.getName().isEmpty())
				{
					return;
				}
				
				EntityPlayer player = event.pickedUp.world.getPlayerEntityByName(event.pickedUp.getName());
				
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityLiving(LivingUpdateEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			
			boolean flag = true;
			for(int i = 9; i < player.inventory.mainInventory.size(); i++)
			{
				ItemStack stack = player.inventory.mainInventory.get(i);
				
				if(player.inventory instanceof BigInventoryPlayer && (i >= ((BigInventoryPlayer)player.inventory).getUnlockedSlots() || i - 9 >= InfiniteInvo.II_Settings.invoSize) && !event.getEntityLiving().world.isRemote && !player.capabilities.isCreativeMode)
				{
					if(stack != null && stack.getItem() != InfiniteInvo.locked)
					{
						player.entityDropItem(stack.copy(), 0);
						player.inventory.setInventorySlotContents(i, null);
						player.inventory.markDirty();
						stack = null;
					}
					
					if(stack == null)
					{
						player.inventory.setInventorySlotContents(i, new ItemStack(InfiniteInvo.locked));
						player.inventory.markDirty();
					}
					flag = false;
					continue;
				}
				
				if(stack != null && stack.getItem() == Items.COOKED_PORKCHOP && stack.getCount() >= stack.getMaxStackSize())
				{
					continue;
				} else
				{
					flag = false;
				}
			}
			
			if(!event.getEntityLiving().isEntityAlive())
			{
				if(!InfiniteInvo.II_Settings.keepUnlocks && !event.getEntityLiving().world.getGameRules().getBoolean(("keepInventory")))
				{
					unlockCache.remove(event.getEntityLiving().getCommandSenderEntity().getName());
					unlockCache.remove(event.getEntityLiving().getUniqueID().toString());
				}
			}
			
		}
	}
	
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			if(!InfiniteInvo.II_Settings.keepUnlocks && !event.getEntityLiving().world.getGameRules().getBoolean("keepInventory"))
			{
				unlockCache.remove(event.getEntityLiving().getCommandSenderEntity().getName());
				unlockCache.remove(event.getEntityLiving().getUniqueID().toString());
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		if(event.getGui() != null && event.getGui().getClass() == GuiInventory.class && !(event.getGui() instanceof GuiBigInventory))
		{
			event.setGui(new GuiBigInventory(Minecraft.getMinecraft().player));
		} else if(event.getGui() == null && Minecraft.getMinecraft().player.inventoryContainer instanceof BigContainerPlayer)
		{
			// Reset scroll and inventory slot positioning to make sure it doesn't screw up later
			((BigContainerPlayer)Minecraft.getMinecraft().player.inventoryContainer).scrollPos = 0;
			((BigContainerPlayer)Minecraft.getMinecraft().player.inventoryContainer).UpdateScroll();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onMouseInput(MouseInputEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		KeyBinding pickBlock = mc.gameSettings.keyBindPickBlock;
		
		if(pickBlock.isPressed() && mc.objectMouseOver != null && InfiniteInvo.II_Settings.invoSize > 27)
		{
			KeyBinding.setKeyBindState(pickBlock.getKeyCode(), false);
			
			if (!net.minecraftforge.common.ForgeHooks.onPickBlock(mc.objectMouseOver, player, mc.world))
			{
				return;
			}
			
			if(player.capabilities.isCreativeMode)
			{
                int j = 36 + player.inventory.currentItem;
                mc.playerController.sendSlotPacket(player.inventory.getStackInSlot(player.inventory.currentItem), j);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiPostInit(InitGuiEvent.Post event)
	{
		if(event.getGui() instanceof GuiBigInventory)
		{
			((GuiBigInventory)event.getGui()).redoButtons = true;
		} else if(event.getGui() instanceof GuiContainer)
		{
			GuiContainer gui = (GuiContainer)event.getGui();
			Container container = gui.inventorySlots;
			
			event.getButtonList().add(new InvoScrollBar(256, 0, 0, 1, 1, "", container, gui));
			
			if(event.getGui() instanceof GuiInventory)
			{
				final ScaledResolution scaledresolution = new ScaledResolution(event.getGui().mc);
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
				event.getButtonList().add(new GuiButtonUnlockSlot(event.getButtonList().size(), i/2 - 50, j - 40, 100, 20, event.getGui().mc.player));
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(!event.getWorld().isRemote && worldDir == null && Minecraft.getMinecraft().player.getServer().isServerRunning())
		{
			MinecraftServer server = Minecraft.getMinecraft().player.getServer();
			
			if(InfiniteInvo.proxy.isClient())
			{
				worldDir = server.getFile("saves/" + server.getFolderName());
			} else
			{
				worldDir = server.getFile(server.getFolderName());
			}

			new File(worldDir, "data/").mkdirs();
			LoadCache(new File(worldDir, "data/SlotUnlockCache"));
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote && worldDir != null && Minecraft.getMinecraft().player.getServer().isServerRunning())
		{
			new File(worldDir, "data/").mkdirs();
			SaveCache(new File(worldDir, "data/SlotUnlockCache"));
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(!event.getWorld().isRemote && worldDir != null && Minecraft.getMinecraft().player.getServer().isServerRunning())
		{
			new File(worldDir, "data/").mkdirs();
			SaveCache(new File(worldDir, "data/SlotUnlockCache"));
			
			worldDir = null;
			unlockCache.clear();
		}
	}
	
	public static void SaveCache(File file)
	{
		try
		{
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(unlockCache);
			
			oos.close();
			fos.close();
		} catch(Exception e)
		{
			InfiniteInvo.logger.log(Level.ERROR, "Failed to save slot unlock cache", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void LoadCache(File file)
	{
		try
		{
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			FileInputStream fis = new FileInputStream(file);
			
			if(fis.available() <= 0)
			{
				fis.close();
				return;
			}
			
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			unlockCache = (HashMap<String,Integer>)ois.readObject();
			
			ois.close();
			fis.close();
		} catch(Exception e)
		{
			InfiniteInvo.logger.log(Level.ERROR, "Failed to load slot unlock cache", e);
		}
	}
}
