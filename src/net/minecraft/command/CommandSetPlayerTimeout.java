package net.minecraft.command;

import net.minecraft.server.MinecraftServer;

public class CommandSetPlayerTimeout extends CommandBase {
   public String getCommandName() {
      return "setidletimeout";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.setidletimeout.usage";
   }

   public void processCommand(ICommandSender sender, String[] args) throws CommandException {
      if (args.length != 1) {
         throw new WrongUsageException("commands.setidletimeout.usage", new Object[0]);
      } else {
         int var3 = CommandBase.parseInt(args[0], 0);
         MinecraftServer.getServer().setPlayerIdleTimeout(var3);
         CommandBase.notifyOperators(sender, this, "commands.setidletimeout.success", var3);
      }
   }
}
