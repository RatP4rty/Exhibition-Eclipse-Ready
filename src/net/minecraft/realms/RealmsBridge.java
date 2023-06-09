package net.minecraft.realms;

import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsBridge extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private GuiScreen previousScreen;

   public void switchToRealms(GuiScreen p_switchToRealms_1_) {
      this.previousScreen = p_switchToRealms_1_;

      try {
         Class var2 = Class.forName("com.mojang.realmsclient.RealmsMainScreen");
         Constructor var3 = var2.getDeclaredConstructor(RealmsScreen.class);
         var3.setAccessible(true);
         Object var4 = var3.newInstance(this);
         Minecraft.getMinecraft().displayGuiScreen(((RealmsScreen)var4).getProxy());
      } catch (Exception var5) {
         LOGGER.error("Realms module missing", var5);
      }

   }

   public void init() {
      Minecraft.getMinecraft().displayGuiScreen(this.previousScreen);
   }
}
