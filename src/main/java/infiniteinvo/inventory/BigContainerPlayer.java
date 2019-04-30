package infiniteinvo.inventory;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import infiniteinvo.core.II_Settings;
import infiniteinvo.core.InfiniteInvo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class BigContainerPlayer extends ContainerPlayer
{
	public int scrollPos = 0;
	public BigInventoryPlayer invo;
	/**
	 * A more organised version of 'inventorySlots' that doesn't include the hotbar
	 */
	Slot[] slots = new Slot[MathHelper.clamp_int(II_Settings.invoSize, 27, Integer.MAX_VALUE - 100)];
	Slot[] hotbar = new Slot[9];
	Slot[] crafting = new Slot[4];
	Slot result;
	private int craftSize = 3;//did not exist before, was magic'd as 2 everywhere
	private final EntityPlayer thePlayer;

	@SuppressWarnings("unchecked")
	public BigContainerPlayer(BigInventoryPlayer invo, boolean isLocal, EntityPlayer player) {
		super(invo, isLocal, player);
		this.invo = (BigInventoryPlayer)invo;
		this.thePlayer = player;
		inventorySlots = Lists.newArrayList();//undo everything done by super()
		craftMatrix = new InventoryCrafting(this, craftSize, craftSize);

		int shiftxOut = 9, 
				shiftyOut = 6,
				shiftx = -7,
				shifty = 0,
				slotNumber = 0;

		this.addSlotToContainer(new SlotCrafting(invo.player, this.craftMatrix, this.craftResult,  slotNumber, 144+shiftxOut, 36+shiftyOut));
		int cx;
		int cy;
		int var4,var5;
		boolean onHold = false;
		int[] holdSlot = new int[5];
		int[] holdX = new int[5];
		int[] holdY = new int[5];
		int h = 0;

		for (var4 = 0; var4 < this.craftSize; ++var4) { //-1 here kills the bottom row
			onHold = false;
			if (var4 == this.craftSize-1) onHold = true; //hold right and bottom column

			for (var5 = 0; var5 < this.craftSize; ++var5) {  
				if (var5 == this.craftSize-1) onHold = true; //hold right and bottom column

				slotNumber = var5 + var4 * this.craftSize;
				cx = 88 + var5 * 18+shiftx;
				cy = 26 + var4 * 18+shifty;

				//if craftsize is not 3, then dont put anything on hold
				if (this.craftSize == 3 && onHold) {
					//save these to add at the end
					holdSlot[h] = slotNumber;
					holdX[h] = cx;
					holdY[h] = cy;
					h++;
				} else {
					//add only the initial 2x2 grid now (numbers 1-4 inclusive, 0 is the output slot id)
					//System.out.println("("+slotNumber+","+cx+","+cy+");");
					this.addSlotToContainer(new Slot(this.craftMatrix, slotNumber, cx , cy ));
					//	System.out.println("("+slotNumber+", 2x2);"+cx+","+cy);
				}   
			}
		}

		for (var4 = 0; var4 < 4; ++var4) {
			slotNumber = invo.getSizeInventory() - 1 - var4; //it was slotarmor

			cx = 8;
			cy = 8 + var4 * 18;
			final int k = var4;
			this.addSlotToContainer(new Slot(player.inventory, slotNumber, cx, cy) {
				/**
				 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1
				 * is the case of armor slots)
				 */
				public int getSlotStackLimit() {
					return 1;
				}
				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
				 */
				public boolean isItemValid(ItemStack stack)
				{
					if (stack == null) return false;
					return stack.getItem().isValidArmor(stack, k, thePlayer);
				}
				@SideOnly(Side.CLIENT)
				public String getSlotTexture()
				{
					return ItemArmor.EMPTY_SLOT_NAMES[k];
				}
			}); 
		}

		//inventory is 3 rows by 9 columns
		for (var4 = 0; var4 < 3; ++var4) {
			for (var5 = 0; var5 < 9; ++var5) {
				slotNumber = var5 + (var4 + 1) * 9;
				cx = 8 + var5 * 18;
				cy = 84 + var4 * 18;
				this.addSlotToContainer(new Slot(player.inventory,slotNumber , cx, cy)); 
			}
		}

		for (var4 = 0; var4 < 9; ++var4) {
			slotNumber = var4;
			cx = 8 + var4 * 18;
			cy = 142;
			this.addSlotToContainer(new Slot(player.inventory, var4, cx, cy)); 

		}

		for(int i = 9; i < 36; i++) {
			// Add all the previous inventory slots to the organised array
			Slot os = (Slot)this.inventorySlots.get(i);

			Slot ns = new SlotLockable(os.inventory, os.getSlotIndex(), os.xDisplayPosition, os.yDisplayPosition);
			ns.slotNumber = os.slotNumber;
			this.inventorySlots.set(i, ns);
			ns.onSlotChanged();
			slots[i - 9] = ns;
		}

		/*
		 * Hotbar
		 */
		for(int i = 36; i < 45; i++) {
			hotbar[i - 36] = (Slot)this.inventorySlots.get(i);
		}

		/*
		 * Crafting Slots
		 */
		for(int i = 1; i < 5; i++) {
			crafting[i - 1] = (Slot)this.inventorySlots.get(i);
		}


		/*
		 * Set display positon for the crafting "output" slot
		 */
		result = (Slot)this.inventorySlots.get(0);
		result.xDisplayPosition = 153;
		result.yDisplayPosition = 42;

		/*
		 * Set display positon for the crafting slots (initial four)
		 * 
		 * The positioning is based on the previous positioning algorithm from each mod, interestingly enough.
		 * Here's what that brought me to this functional point:
		 * hs.xDisplayPosition = 88 + ((i%2) * 18);
		 * hs.yDisplayPosition = 43 + ((i/2) * 18);
		 * 144+shiftxOut, 36+shiftyOut
		 * cx = 88 + var5 * 18+shiftx;
		 * cy = 26 + var4 * 18+shifty;
		 * 
		 */
		for(int i = 0; i < 4; i++) {
			Slot hs = crafting[i];

			hs.xDisplayPosition = 88 + (i%2) * 18 + shiftx;
			hs.yDisplayPosition = 26 + (i/2) * 18 + shifty;
		}

		/*
		 * Set display positon for the hot bar
		 * 
		 */
		for(int i = 0; i < 9; i++)
		{
			Slot hs = hotbar[i];
			hs.xDisplayPosition = 8 + (i * 18);
			hs.yDisplayPosition = 142 + (18 * II_Settings.extraRows);
		}

		/*
		 * What was this again?
		 */
		for (int i = 3; i < MathHelper.ceiling_float_int((float)II_Settings.invoSize/9F); ++i) {
			for (int j = 0; j < 9; ++j) {
				if (j + (i * 9) >= II_Settings.invoSize && II_Settings.invoSize > 27) {
					break;
				} else {
					// Moved off screen to avoid interaction until screen scrolls over the row
					Slot ns = new SlotLockable(invo, j + (i + 1) * 9, -999, -999);
					slots[j + (i * 9)] = ns;
					this.addSlotToContainer(ns);
				}
			}
		}


		/*
		 * Crafting Slots (Remaining Five)
		 * Finally, add the five new slots to the 3x3 crafting grid
		 * In the original mod they end up being (45-49 inclusive)
		 * But here we have 126 slots before them, not 44!
		 * I just added the difference (126 - 44 = *82*) for now.
		 * 
		 * There's clearly a better way of doing this.
		 */
		for (int i : holdX) { i += 82; }
		for (int i : holdY) { i += 82; }
		for(h = 0; h < 5; ++h) {
			slotNumber = holdSlot[h];
			cx = holdX[h];
			cy = holdY[h];
			this.addSlotToContainer(new Slot(this.craftMatrix, slotNumber, cx, cy ));

		}

		this.UpdateScroll();
	}

	@Override
	public Slot getSlotFromInventory(IInventory invo, int id) {
		Slot slot = super.getSlotFromInventory(invo, id);
		if(slot == null) {
			Exception e = new NullPointerException();
			InfiniteInvo.logger.log(Level.FATAL, e.getStackTrace()[1].getClassName() + "." + e.getStackTrace()[1].getMethodName() + ":" + e.getStackTrace()[1].getLineNumber() + " is requesting slot " + id + " from inventory " + invo.getInventoryName() + " (" + invo.getClass().getName() + ") and got NULL!", e);
		}
		return slot;
	}

	public void UpdateScroll() {
		if(scrollPos > MathHelper.ceiling_float_int((float)II_Settings.invoSize/(float)(9 + II_Settings.extraColumns)) - (3 + II_Settings.extraRows))
		{
			scrollPos = MathHelper.ceiling_float_int((float)II_Settings.invoSize/(float)(9 + II_Settings.extraColumns)) - (3 + II_Settings.extraRows);
		}

		if(scrollPos < 0)
		{
			scrollPos = 0;
		}

		for(int i = 0; i < MathHelper.ceiling_float_int((float)MathHelper.clamp_int(II_Settings.invoSize, 27, Integer.MAX_VALUE)/(float)(9 + II_Settings.extraColumns)); i++)
		{
			for (int j = 0; j < 9 + II_Settings.extraColumns; ++j)
			{
				int index = j + (i * (9 + II_Settings.extraColumns));
				if(index >= II_Settings.invoSize && index >= 27)
				{
					break;
				} else
				{
					if(i >= scrollPos && i < scrollPos + 3 + II_Settings.extraRows && index < invo.getUnlockedSlots() - 9 && index < II_Settings.invoSize)
					{
						Slot s = slots[index];
						s.xDisplayPosition = 8 + j * 18;
						s.yDisplayPosition = 84 + (i - scrollPos) * 18;
					} else
					{
						Slot s = slots[index];
						s.xDisplayPosition = -999;
						s.yDisplayPosition = -999;
					}
				}
			}
		}
	}

	/**
	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
	{
		int vLocked = invo.getUnlockedSlots() < 36? 36 - invo.getUnlockedSlots() : 0;
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(p_82846_2_);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (p_82846_2_ == 0) // Crafting result
			{
				if (!this.mergeItemStack(itemstack1, 9, 45, true))
				{
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			else if (p_82846_2_ >= 1 && p_82846_2_ < 5) // Crafting grid
			{
				if (!this.mergeItemStack(itemstack1, 9, 45, false))
				{
					return null;
				}
			}
			else if (p_82846_2_ >= 5 && p_82846_2_ < 9) // Armor
			{
				if (!this.mergeItemStack(itemstack1, 9, 45, false))
				{
					return null;
				}
			}
			else if (itemstack.getItem() instanceof ItemArmor && !((Slot)this.inventorySlots.get(5 + ((ItemArmor)itemstack.getItem()).armorType)).getHasStack()) // Inventory to armor
			{
				int j = 5 + ((ItemArmor)itemstack.getItem()).armorType;

				if (!this.mergeItemStack(itemstack1, j, j + 1, false))
				{
					return null;
				}
			}
			else if ((p_82846_2_ >= 9 && p_82846_2_ < 36) || (p_82846_2_ >= 45 && p_82846_2_ < invo.getUnlockedSlots() + 9))
			{
				if (!this.mergeItemStack(itemstack1, 36, 45, false))
				{
					return null;
				}
			}
			else if (p_82846_2_ >= 36 && p_82846_2_ < 45) // Hotbar
			{
				if (!this.mergeItemStack(itemstack1, 9, 36 - vLocked, false) && (invo.getUnlockedSlots() - 36 <= 0 || !this.mergeItemStack(itemstack1, 45, 45 + (invo.getUnlockedSlots() - 36), false)))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 9, invo.getUnlockedSlots() + 9, false)) // Full range
			{
				return null;
			}

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(p_82846_1_, itemstack1);
		}

		return itemstack;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		//System.out.println("ContainerPlayerCrafting onContainerClosed");
		super.onContainerClosed(playerIn);

		for (int i = 0; i < craftSize*craftSize; ++i)
		{
			ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);

			if (itemstack != null)
			{
				playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
			}
		}

		this.craftResult.setInventorySlotContents(0, (ItemStack)null);
	}
}
