package infiniteinvo.core;

import org.apache.logging.log4j.Logger;

import infiniteinvo.core.proxies.CommonProxy;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = InfiniteInvo.MODID, version = InfiniteInvo.VERSION, name = InfiniteInvo.NAME, guiFactory = "infiniteinvo.handlers.ConfigGuiFactory")
public class InfiniteInvo
{
    public static final String MODID = "infiniteinvo";
    public static final String VERSION = "CI_MOD_VERSION";
    public static final String BRANCH = "CI_MOD_BRANCH";
    public static final String HASH = "CI_MOD_HASH";
    public static final String NAME = "InfiniteInvo";
    public static final String PROXY = "infiniteinvo.core.proxies";
    public static final String CHANNEL = "I_INVO_CHAN";
	
    /**
     * A container for all the configurable settings in the mod
     */
    public static class II_Settings	{
    	public static int invoSize = 126;
    	public static boolean xpUnlock = false;
    	public static int unlockedSlots = 0;
    	public static int unlockCost = 10;
    	public static boolean keepUnlocks = false;
    	public static int extraRows = 2;
    	public static int extraColumns = 1;
    	public static int unlockIncrease = 1;
    	public static boolean hideUpdates = true;
    	public static boolean useOrbs = false;
    	public static boolean IT_Patch = true;

    }
    
	@Instance(MODID)
	public static InfiniteInvo instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network ;
	public static Logger logger;
	
	/**
	 * Purely used for returning faking filled slots
	 */
	public static Item locked;
	public static Item unlock;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);    	
    	proxy.registerHandlers();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
}
