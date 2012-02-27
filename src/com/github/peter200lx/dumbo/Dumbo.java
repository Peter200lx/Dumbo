package com.github.peter200lx.dumbo;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Dumbo extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");

	public static boolean debug = false;

	private static boolean permissions;

	public static Material bind;

	public static int cruise;
	public static int thrust;
	public static boolean flyEnabled;
	public static boolean floatEnabled;
	public static boolean tpEnabled;

	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		if(loadConf()) {
			// Register our events
			getServer().getPluginManager().registerEvents(new FlyListener(), this);

			//TODO Add a save config function

			//Print Dumbo loaded message
			if(debug) {
				PluginDescriptionFile pdfFile = this.getDescription();
				log.info( "["+pdfFile.getName() + "] version " + pdfFile.getVersion() +
						" is now loaded with debug enabled" );
			}
		} else {
			log.warning( "[Dumbo] had an error loading config.yml and is now disabled");
			this.setEnabled(false);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		//Safety check for determining console status
		boolean console = true;
		if (sender instanceof Player) {
			console = false;
		}

		if(cmd.getName().equalsIgnoreCase("dumbo")&&(args.length == 1)){
			if(args[0].contentEquals("reload")) {
				if(hasAdminPerm(sender,"dumbo.reload")) {
					this.reloadConfig();
					if(loadConf())
						sender.sendMessage("Configuration file config.yml has been reloaded");
					else {
						sender.sendMessage("[WARNING] Configuration file load error, "+
						"check console logs");
						bind = Material.AIR;
						sender.sendMessage("[WARNING] Flight has been disabled until "+
								"a valid config file is loaded");
					}
				} else {
					sender.sendMessage( "You don't have permission to reload the config file");
				}
				return true;
			} else if (args[0].contentEquals("tool")) {
				if (console) {
					sender.sendMessage("This command can only be run by a player"); 
				} else {
					if((!bind.equals(Material.AIR))	&& flyEnabled &&
							hasPerm(sender,"dumbo.fly") ) {
						sender.sendMessage("The fly tool is bound to the "+bind+". Right-click to fly.");
						if(floatEnabled && hasPerm(sender,"dumbo.float")) 
							sender.sendMessage("Also left-click to float.");
						if(tpEnabled && hasPerm(sender,"dumbo.teleport"))
							sender.sendMessage("Also hold shift to teleport instead of zoom");
					}
				}
				return true;
			}
		}

		//TODO Improve output for control
		if(cmd.getName().equalsIgnoreCase("fly")){
			if(args.length == 0) {
				if(hasAdminPerm(sender,"dumbo.ctrl")) {
					sender.sendMessage("Flying is globally "+(flyEnabled?"enabled":"disabled"));
					sender.sendMessage("Floating is globally "+(floatEnabled?"enabled":"disabled"));
					sender.sendMessage("teleporting is globally "+(tpEnabled?"enabled":"disabled"));
				} else {
					sender.sendMessage("You can: "+
							((flyEnabled&&hasPerm(sender,"dumbo.fly"))?"fly":"")+
							((floatEnabled&&hasPerm(sender,"dumbo.float"))?", hover":"")+
							((tpEnabled&&hasPerm(sender,"dumbo.teleport"))?", teleport":""));
				}
				sender.sendMessage("You can: "+(flyEnabled?"enabled":"disabled"));
				sender.sendMessage("Floating is globally "+(floatEnabled?"enabled":"disabled"));
				sender.sendMessage("teleporting is globally "+(tpEnabled?"enabled":"disabled"));
				return true;
			} else if(hasAdminPerm(sender,"dumbo.ctrl")&&(args.length == 1)) {
				if(args[0].contentEquals("on")) {
					flyEnabled = true;
					sender.sendMessage("Flying is now globally enabled");
					return true;
				} else if (args[0].contentEquals("off")) {
					flyEnabled = false;
					sender.sendMessage("Flying is now globally enabled");
					return true;
				} else if (args[0].contentEquals("toggle")) {
					flyEnabled = !flyEnabled;
					sender.sendMessage("Flying is now globally "+(flyEnabled?"enabled":"disabled"));
					return true;
				} else if (args[0].contentEquals("hover")) {
					floatEnabled = !floatEnabled;
					sender.sendMessage("Floating is now globally "+(floatEnabled?"enabled":"disabled"));
					return true;
				} else if (args[0].contentEquals("teleport") ||
						 args[0].contentEquals("tel")) {
					tpEnabled = !tpEnabled;
					sender.sendMessage("teleporting is now globally "+(tpEnabled?"enabled":"disabled"));
					return true;
				}
			} else if(hasAdminPerm(sender,"dumbo.ctrl")&&(args.length == 2)) {
				if (args[0].contentEquals("thrust")) {
					try {
						thrust = Integer.parseInt(args[1]);
						if(thrust < 0)
							thrust = 8;
						sender.sendMessage("Flap thrust is set to "+thrust);
					} catch (NumberFormatException e) {
						sender.sendMessage("Entered value of '"+args[1]+"' is not a valid number");
					}
				} else if (args[0].contentEquals("cruise")) {
					try {
						cruise = Integer.parseInt(args[1]);
						if(cruise < 0)
							cruise = 110;
						sender.sendMessage("Cruising altitude is set to "+cruise);
					} catch (NumberFormatException e) {
						sender.sendMessage("Entered value of '"+args[1]+"' is not a valid number");
					}
				}
			}
		}
		return false;
	}

	public static Boolean hasAdminPerm(CommandSender p, String what) {
		if(permissions)
			return p.hasPermission(what);
		else if(p.isOp())
			return true;
		else
			return false;
	}

	public static Boolean hasPerm(CommandSender p,String what) {
		if(permissions)
			return p.hasPermission(what);
		else
			return true;
	}

	private Boolean loadConf() {
		// Load and/or initialize configuration file
		if(!this.getConfig().isSet("fly")) {
			this.saveDefaultConfig();
			log.info( "[Dumbo][loadConf] config.yml copied from .jar (likely first run)" );
		}

		//Reload and hold config for this function
		FileConfiguration conf = this.getConfig();

		//Check and set the debug printout flag
		Boolean old = debug;
		debug = conf.getBoolean("debug", false);
		if(debug) log.info( "[Dumbo][loadConf] Debugging is enabled");
		if(old && (!debug))
			log.info("[Dumbo][loadConf] Debugging has been disabled");

		//Check and set the permissions flag
		permissions = conf.getBoolean("permissions", true);
		if(debug) log.info( "[Dumbo][loadConf] permmissions are "+permissions);

		int id = conf.getInt("fly.bind", 288);
		if( id > 0 ) {
			Material type = Material.getMaterial(id);
			if(type != null) {
				bind = type;
				if(debug) log.info( "[Dumbo][loadConf] Fly tool is bound to " + type);
			} else {
				log.warning("[Dumbo] Binding for fly tool of "+id+" does not map to a material.");
				return false;
			}
		} else {
			log.warning("[Dumbo] Binding for fly tool of "+id+" does not map to a material.");
			return false;
		}

		thrust = conf.getInt("fly.thrust", 8);
		if(debug) log.info("[Dumbo][loadConf] Flap thrust is set to "+thrust);
		cruise = conf.getInt("fly.cruise", 110);
		if(debug) log.info("[Dumbo][loadConf] Cruising altitude is set to "+cruise);

		flyEnabled = conf.getBoolean("fly.enabled", true);
		if(debug) log.info("[Dumbo][loadConf] Flying enabled is set to "+flyEnabled);
		floatEnabled = conf.getBoolean("fly.float", true);
		if(debug) log.info("[Dumbo][loadConf] Floating enabled is set to "+floatEnabled);
		tpEnabled = conf.getBoolean("fly.teleport", false);
		if(debug) log.info("[Dumbo][loadConf] Teleport Flying enabled is set to "+tpEnabled);

		return true;
	}
}
