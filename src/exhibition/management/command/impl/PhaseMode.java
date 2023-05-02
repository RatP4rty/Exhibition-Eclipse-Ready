package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.util.misc.ChatUtil;

public class PhaseMode extends Command {
   public static PhaseMode.Phase phase;

   public PhaseMode(String[] names, String description) {
      super(names, description);
   }

   public void fire(String[] args) {
      if (args == null) {
         ChatUtil.printChat("\2474[\247cE\2474]\2478 Current phase mode:\247c " + phase.name());
         this.printUsage();
      } else if (args.length > 0) {
         PhaseMode.Phase[] var2 = PhaseMode.Phase.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            PhaseMode.Phase type = var2[var4];
            if (type.name().toLowerCase().contains(args[0].toLowerCase())) {
               phase = type;
               ChatUtil.printChat("\2474[\247cE\2474]\2478 Phase mode has been set to:\247c " + type.name());
            }
         }

      }
   }

   public String getUsage() {
      return "<Spider, Skip, Normal, FullBlock, Silent>";
   }

   static {
      phase = PhaseMode.Phase.Normal;
   }

   public static enum Phase {
      Spider,
      Skip,
      Normal,
      FullBlock,
      Silent;
   }
}
