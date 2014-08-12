package net.gnomeffinway.depenizen.commands.worldguard;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.gnomeffinway.depenizen.Depenizen;
import net.gnomeffinway.depenizen.support.Support;
import net.gnomeffinway.depenizen.support.Supported;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionCommand extends AbstractCommand {

    // <--[command]
    // @Name region
    // @Syntax region [{add} <cuboid>/remove <world>] [id:<name>]
    // @Group Depenizen
    // @Plugin WorldGuard
    // @Required 2
    // @Stable untested
    // @Short Adds or removes a protected region.
    // @Author Morphan1

    // @Description
    // Adds a protected region to a region manager based on the specified cuboid,
    // or removes a protected region from a region manager based on the specified
    // world. Currently, this command only supports cuboid-shaped regions.

    // @Tags None

    // @Usage
    // Use to add a region based on a cuboid.
    // - region add cu@l@123,0,321,world|l@321,256,123,world id:MyRegion

    // @Usage
    // Use to remove a region from a world.
    // - region remove w@world id:MyRegion

    // -->

    private enum Action { ADD, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("region_id")
                    && arg.matchesPrefix("id"))
                scriptEntry.addObject("region_id", arg.asElement());

            if (!scriptEntry.hasObject("cuboid")
                    && arg.matchesArgumentType(dCuboid.class))
                scriptEntry.addObject("cuboid", arg.asType(dCuboid.class));

            if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class))
                scriptEntry.addObject("world", arg.asType(dWorld.class));

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                scriptEntry.addObject("action", arg.asElement());

        }

        if (!scriptEntry.hasObject("region_id"))
            throw new InvalidArgumentsException("Must specify a region id!");

        if (!scriptEntry.hasObject("cuboid") && (!scriptEntry.hasObject("action")
                || scriptEntry.getElement("action").asString().equalsIgnoreCase("ADD")))
            throw new InvalidArgumentsException("Must specify a valid cuboid!");

        if (!scriptEntry.hasObject("world") && scriptEntry.hasObject("action")
                && scriptEntry.getElement("action").asString().equalsIgnoreCase("REMOVE"))
            throw new InvalidArgumentsException("Must specify a valid world!");

        if (!scriptEntry.hasObject("action"))
            scriptEntry.addObject("action", new Element("ADD"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element region_id = scriptEntry.getElement("region_id");
        dCuboid cuboid = scriptEntry.getdObject("cuboid");
        dWorld w = scriptEntry.getdObject("world");
        World world = w != null ? w.getWorld() : cuboid != null ? cuboid.getWorld() : null;
        Element action = scriptEntry.getElement("action");

        if (world == null)
            throw new CommandExecutionException("No valid world found!");

        dB.report(scriptEntry, getName(), region_id.debug() + (cuboid != null ? cuboid.debug() : "")
                + aH.debugObj("world", world.getName()) + action.debug());

        if (action.asString().equalsIgnoreCase("REMOVE")) {
            WorldGuardPlugin worldGuard = Supported.get("WORLDGUARD").getPlugin();
            worldGuard.getRegionManager(world).removeRegion(region_id.asString());
            return;
        }

        Location low = cuboid.getLow(0);
        Location high = cuboid.getHigh(0);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(region_id.asString(),
                new BlockVector(low.getX(), low.getY(), low.getZ()),
                new BlockVector(high.getX(), high.getY(), high.getZ()));

        WorldGuardPlugin worldGuard = Supported.get("WORLDGUARD").getPlugin();
        worldGuard.getRegionManager(cuboid.getWorld()).addRegion(region);

    }

}