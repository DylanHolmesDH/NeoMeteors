package me.KillerSmurf.NeoMeteors;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;

public class NMEntityListener extends EntityListener{
	public NeoMeteors plugin;
	public NMEntityListener(NeoMeteors plugin)
	{
		this.plugin=plugin;
	}
	public void onExplosionPrime(ExplosionPrimeEvent e)
	{
		//Check if explosion was made by meteor
		if(plugin.fireballs.contains(e.getEntity()))
		{			
			//Make explosion radius 10
			e.setRadius(plugin.radius);			
		}
	}
	public void onEntityExplode(EntityExplodeEvent e)
	{
		//Check if explosion was made by meteor
		if(plugin.fireballs.contains(e.getEntity()))
		{
			//Tell all people on server about meteor
			plugin.server.broadcastMessage(ChatColor.DARK_GREEN + "A meteor has impacted on the earth!");
			//Make only 5% of blocks drop
			e.setYield((float)plugin.yield/100);
			//Searches through all blocks in a cube defined by explosion radius+ ore threshold
			World world=e.getLocation().getWorld();
			int max=0;
			//Checks for max Chance
			for(int i:plugin.explosionMaterials[1])
			{
				max+=i;
			}
			Random rand=new Random();
			for(int x=0-plugin.radius-plugin.oreThreshold;x<plugin.radius+1+plugin.oreThreshold;x++)
			{				
				for(int y=0-plugin.radius-plugin.oreThreshold;y<plugin.radius+1+plugin.oreThreshold;y++)
				{
					for(int z=0-plugin.radius-plugin.oreThreshold;z<plugin.radius+1+plugin.oreThreshold;z++)
					{
						Location expl=e.getLocation();
						Location loc=new Location(world,x+expl.getX(),y+expl.getY(),z+expl.getZ());						
						//Checking if it is within the radius+threshold, making a sphere
						//Also makes sure it isn't air.
						if(loc.distance(expl)<plugin.radius+plugin.oreThreshold&&world.getBlockTypeIdAt(loc)!=0)
						{
							//Chooses a random block to place, based on the probabilities
							int mat=rand.nextInt(max)+1;
							int prevs=0;
							//Goes through all of the possible materials
							loop: for(int i=0;i<plugin.explosionMaterials[0].length;i++)
							{
								//Checks if the randomly selected material is this one
								if(mat<prevs+plugin.explosionMaterials[1][i])
								{
									//Places block there
									
									world.getBlockAt(loc).setTypeId(plugin.explosionMaterials[0][i]);
									//And then stops checking materials
									break loop;
								}								
								prevs+=plugin.explosionMaterials[1][i];
							}
						}
					}
				}
			}
			plugin.fireballs.remove(e.getEntity());
		}
	}
}
