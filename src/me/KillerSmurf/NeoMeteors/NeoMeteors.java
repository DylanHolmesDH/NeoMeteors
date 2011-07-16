package me.KillerSmurf.NeoMeteors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;


public class NeoMeteors extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	
	public static Server server;
	public int distance=50;
	public int radius=10;
	public int spawnChance=8;
	public int spawnDelay=1200;
	public int yield=5;
	//Materials left behind when meteor explodes. Left is the IDs, right is chance to spawn
	public int[][] explosionMaterials={{49,73,14,15,56},{200,48,31,64,15}};
	//How far away from the explosion radius ores will be left behind
	public int oreThreshold=2;
	
	@Override
	public void onDisable() {
		log.info("[NeoMeteors]Disabled!");
		
	}
	public NMEntityListener entityListener=new NMEntityListener(this);
	@Override
	public void onEnable() {
		setupPermissions();
		setupConfig();
		server=getServer();
		PluginManager pm = server.getPluginManager();
		pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Normal, this);
		//Random chance of a meteor hitting around a player. Checks every minute
		server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run()
			{
				Random rand=new Random();
				//8% chance per player
				if(rand.nextInt(100)<spawnChance*server.getOnlinePlayers().length)
				{
					//Chooses a random player
					Player player=server.getOnlinePlayers()[rand.nextInt(server.getOnlinePlayers().length)];
					//Spawn a meteor at a random x and z around the player, at a maximum of distance meters away
					Location loc=player.getLocation();
					makeMeteor(player.getWorld(),loc.getX()+rand.nextInt(distance*2)-distance,loc.getZ()+rand.nextInt(distance*2)-distance);
				}				
			}
		}, spawnDelay, spawnDelay);
		log.info("[NeoMeteors] Enabled!");
	}
	//Permissions
	public static PermissionHandler permissionHandler;
	
	public boolean permissions=false;
	 
	private void setupPermissions() 
	 {
	       Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

	       if (this.permissionHandler == null) {
	           if (permissionsPlugin != null) {
	               this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	               permissions=true;
	           } else {
	               log.info("Permission system not detected, defaulting to OP");	               
	           }
	       }
	 }
	ArrayList fireballs=new ArrayList();
	public void makeMeteor(World world,double x, double z)
	{
		Random rand=new Random();
		//spawn new fireball going down at 15 degrees and facing a random direction
		//A bit buggy because of the fact that you need to set Yaw/Pitch and Velocity
		Fireball fireball=(world.spawn(new Location(world,x,190,z,(float)rand.nextInt(360),-15), Fireball.class));	
		fireball.setDirection(fireball.getDirection().setY(-1));
		fireballs.add(fireball);
	}
	int meteorStorm=15;
	public boolean onCommand(CommandSender sender,Command cmd,String commandLabel,String[] args)	
	{
		Player player;
		try
		{
			player=(Player)sender;
		}
		catch(Exception e)
		{
			sender.sendMessage("You need to be in-game to use that!");
			return true;
		}
		if(commandLabel.equalsIgnoreCase("meteor"))
		{
			if(!permissions)
			{
				if(!player.isOp())
				{
					player.sendMessage(ChatColor.DARK_RED+"You don't have permission to use that!");
					return true;
				}
			}
			else if(!permissionHandler.has(player,"NeoMeteors.Meteor"))
			{
				player.sendMessage(ChatColor.DARK_RED+"You don't have permission to use that!");
				return true;
			}
			final Location loc=player.getTargetBlock(null, 250).getLocation();
			makeMeteor(loc.getWorld(),loc.getX(),loc.getZ());
			if(args.length>0&&(args[0].equalsIgnoreCase("storm")||args[0].equalsIgnoreCase("shower")))
			{
				if(!permissions)
				{
					if(!player.isOp())
					{
						player.sendMessage(ChatColor.DARK_RED+"You don't have permission to use that!");
						return true;
					}
				}
				else if(!permissionHandler.has(player,"NeoMeteors.Storm"))
				{
					player.sendMessage(ChatColor.DARK_RED+"You don't have permission to use that!");
					return true;
				}
				final Random rand=new Random();
				final int id=server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
					public void run()
					{
						makeMeteor(loc.getWorld(),loc.getX()+rand.nextInt(20)-10,loc.getZ()+rand.nextInt(20)-10);
					}
				}, 1, 1);
				server.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					public void run()
					{
						server.getScheduler().cancelTask(id);
					}
				}, meteorStorm);
			}
		}
		return true;
	}
	
	public static String mainDir="plugins/NeoMeteors";
	public static File config=new File(mainDir+File.separator+"config.txt");
	public static Properties prop=new Properties();
	
	public void setupConfig()
	{
		new File(mainDir).mkdir();
		if(!config.exists())
		{
			try
			{
				log.info("[NeoMeteors] Creating config file...");
				config.createNewFile();
				FileOutputStream out=new FileOutputStream(config);
				prop.put("Meteor Spawn Chance",Integer.toString(8));
				prop.put("Meteor Spawn Delay",Integer.toString(1200));
				prop.put("Meteor Storm Number",Integer.toString(15));
				prop.put("Crash Materials","49,73,14,15,56");
				prop.put("Crash Material Chances","200,48,31,64,15");
				prop.put("Meteor Distance From Player","50");
				prop.put("Explosion Radius","10");
				prop.put("Material Threshold","10");
				prop.put("Explosion Yield","5");
				prop.store(out,"");
				log.info("[NeoMeteors] Created config file");
			}
			catch(IOException e)
			{
				e.printStackTrace();
				log.info("[NeoMeteors] Could not create config, using defaults");
				return;
			}
		}
		try
		{
			FileInputStream in=new FileInputStream(config);
			prop.load(in);
			spawnChance=Integer.parseInt(prop.getProperty("Meteor Spawn Chance"));
			spawnDelay=Integer.parseInt(prop.getProperty("Meteor Spawn Delay"));
			meteorStorm=Integer.parseInt(prop.getProperty("Meteor Storm Number"));
			String[] matArray=(prop.getProperty("Crash Materials").split(","));
			for(int i=0;i<matArray.length;i++)
			{
				explosionMaterials[0][i]=Integer.parseInt(matArray[i]);
			}
			String[] chanceArray=prop.getProperty("Crash Material Chances").split(",");
			for(int i=0;i<chanceArray.length;i++)
			{
				explosionMaterials[1][i]=Integer.parseInt(chanceArray[i]);
			}
			distance=Integer.parseInt(prop.getProperty("Meteor Distance From Player"));
			radius=Integer.parseInt(prop.getProperty("Explosion Radius"));
			oreThreshold=Integer.parseInt(prop.getProperty("Material Threshold"));
			yield=Integer.parseInt(prop.getProperty("Explosion Yield"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			log.info("[NeoMeteors] Could not load config file. Try reloading");			
		}		
	}
	
}
