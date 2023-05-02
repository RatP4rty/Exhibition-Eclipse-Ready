package exhibition.module.impl.movement;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.Setting;
import exhibition.module.impl.player.Scaffold;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.management.notifications.user.Notifications;
import exhibition.util.PlayerUtil;

import exhibition.util.Timer2;
import exhibition.util.misc.ChatUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;


public class ZoomFly
extends Module
{
public static String BOOST = "BOOST";
public static String SPEED = "SPEED";
public static final String MODE = "FLYMODE";
Timer2 kickTimer = new Timer2();
private double flyHeight;
private double startY;
private double lastDist;
private double moveSpeed;
private int stage;
int counter, level;
boolean GG;
boolean b2;
private int hypixelCounter;
private int hypixelCounter2;
public ZoomFly(ModuleData data)
{
  super(data);
  this.settings.put("BOOST", new Setting("BOOST", (int)0.5, "Boost speed.", 0.1, 0.4, 5.0));
  this.settings.put(SPEED, new Setting(SPEED, Float.valueOf(2.0F), "Movement speed.", 0.25D, 0.25D, 5.0D));
  this.settings.put("FLYMODE", new Setting("FLYMODE", new Options("Fly Mode", "Hypixel", new String[] { "Vanilla", "AntiKick", "Hypixel" }), "Fly method."));
}

	public void damagePlayer(int damage) {
		if (damage < 1)
			damage = 1;
		if (damage > MathHelper.floor_double(mc.thePlayer.getMaxHealth()))
			damage = MathHelper.floor_double(mc.thePlayer.getMaxHealth());

		double offset = 0.0625;
		if (mc.thePlayer != null && mc.getNetHandler() != null && mc.thePlayer.onGround) {
			for (int i = 0; i <= ((3 + damage) / offset); i++) { // TODO: teach rederpz (and myself) how math works
				mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
						mc.thePlayer.posY + offset, mc.thePlayer.posZ, false));
				mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
						mc.thePlayer.posY, mc.thePlayer.posZ, (i == ((3 + damage) / offset))));
			}
		}
	}
	
public double round(final double value, final int places) 
{
    if (places < 0) {
        throw new IllegalArgumentException();
    }
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
}

public static double getBaseMoveSpeed()
{
  double baseSpeed = 0.2873D;
  if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
  {
    int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
    baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
  }
  return baseSpeed;
}

public void updateFlyHeight()
{
  double h = 1.0D;
  AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.0625D, 0.0625D, 0.0625D);
  for (this.flyHeight = 0.0D; this.flyHeight < mc.thePlayer.posY; this.flyHeight += h)
  {
    AxisAlignedBB nextBox = box.offset(0.0D, -this.flyHeight, 0.0D);
    if (mc.theWorld.checkBlockCollision(nextBox))
    {
      if (h < 0.0625D) {
        break;
      }
      this.flyHeight -= h;
      h /= 2.0D;
    }
  }
}

public void goToGround()
{
  if (this.flyHeight > 300.0D) {
    return;
  }
  double minY = mc.thePlayer.posY - this.flyHeight;
  if (minY <= 0.0D) {
    return;
  }
  for (double y = mc.thePlayer.posY; y > minY;)
  {
    y -= 8.0D;
    if (y < minY) {
      y = minY;
    }
    C03PacketPlayer.C04PacketPlayerPosition packet = new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true);
    
    mc.thePlayer.sendQueue.addToSendQueue(packet);
  }
  for (double y = minY; y < mc.thePlayer.posY;)
  {
    y += 8.0D;
    if (y > mc.thePlayer.posY) {
      y = mc.thePlayer.posY;
    }
    C03PacketPlayer.C04PacketPlayerPosition packet = new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true);
    
    mc.thePlayer.sendQueue.addToSendQueue(packet);
  }
}

public void onEnable()
{
	
 
	if (((Options)((Setting)this.settings.get("FLYMODE")).getValue()).getSelected().equals("Hypixel")) {
		  this.damagePlayer(1);
		  GG = true;
		 

	}
	new Timer().schedule(new TimerTask(){
        @Override
        public void run() {
        	 GG = false;
	
            this.cancel();
        }
    }, 260);

	mc.timer.timerSpeed = 1.0F;
    this.startY = mc.thePlayer.posY;
    level = 1;
	moveSpeed = 0.1D;
	b2 = true;
	lastDist = 0.0D;
}

public void onDisable() {
  if(PlayerUtil.MovementInput()) {
	ZoomFly.mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(ZoomFly.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
  }  
	this.hypixelCounter = 0;
  this.hypixelCounter2 = 100;
   mc.timer.timerSpeed = 1.0f;
	level = 1;
	moveSpeed = 0.1D;
   	b2 = false;
	lastDist = 0.0D;
}

/**
 * @param event
 */
@RegisterEvent(events={EventMove.class, EventMotionUpdate.class})
public void onEvent(Event event)
{
	Module[] modules;
	for (Module module : modules = new Module[]{(Module)Client.getModuleManager().get(Speed.class), (Module)Client.getModuleManager().get(LongJump.class), (Module)Client.getModuleManager().get(Scaffold.class)}) {
         if (!module.isEnabled()) continue;
         module.toggle();
         Notifications.getManager().post("Movement Check", "Disabled extra modules.", 250L, Notifications.Type.NOTIFY);
     }
	if ((event instanceof EventMotionUpdate))
  {
		
		if(GG) {
			  mc.thePlayer.setPosition(mc.thePlayer.posX+1, mc.thePlayer.posY, mc.thePlayer.posZ+1);
		       mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);    
		}
	  EventMotionUpdate em = (EventMotionUpdate)event;
    double speed = ((Number)((Setting)this.settings.get(SPEED)).getValue()).floatValue();
    double fly = ((Number)((Setting)this.settings.get(BOOST)).getValue()).floatValue();
    if (em.isPre())
    {
      setSuffix(((Options)((Setting)this.settings.get("FLYMODE")).getValue()).getSelected());
      switch (((Options)((Setting)this.settings.get("FLYMODE")).getValue()).getSelected())
      {
      case "Hypixel":
          final double xDist = this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX;
          final double zDist = this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ;
          this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
          if (Minecraft.getMinecraft().thePlayer.onGround) {
              Minecraft.getMinecraft().thePlayer.motionY = 0.42f;
         
     	   }
       
          
          ++counter;
 			if (Minecraft.getMinecraft().thePlayer.moveForward == 0
 					&& Minecraft.getMinecraft().thePlayer.moveStrafing == 0) {
 				
 				Minecraft.getMinecraft().thePlayer.setPosition(
 				
 						Minecraft.getMinecraft().thePlayer.posX + 1.0D,
 						Minecraft.getMinecraft().thePlayer.posY + 1.0D,
 						Minecraft.getMinecraft().thePlayer.posZ + 1.0D);
 				Minecraft.getMinecraft().thePlayer.setPosition(Minecraft.getMinecraft().thePlayer.prevPosX,
 						Minecraft.getMinecraft().thePlayer.prevPosY,
 						Minecraft.getMinecraft().thePlayer.prevPosZ);
 				Minecraft.getMinecraft().thePlayer.motionX = 0.0D;
 				Minecraft.getMinecraft().thePlayer.motionZ = 0.0D;
 		   		
 			}
 			Minecraft.getMinecraft().thePlayer.motionY = 0.0D;
 			if (Minecraft.getMinecraft().gameSettings.keyBindJump.pressed)
 				Minecraft.getMinecraft().thePlayer.motionY += 0.5f;
 			if (Minecraft.getMinecraft().gameSettings.keyBindSneak.pressed)
 				Minecraft.getMinecraft().thePlayer.motionY -= 0.5f;
 			
 			if (counter != 1 && counter == 2) {
 				
 				Minecraft.getMinecraft().thePlayer.setPosition(Minecraft.getMinecraft().thePlayer.posX,
 						Minecraft.getMinecraft().thePlayer.posY + 1.0E-10D,
 						Minecraft.getMinecraft().thePlayer.posZ);
 				counter = 0;
 				
 			}
        break;
      case "Vanilla": 
        if (mc.thePlayer.movementInput.jump) {
          mc.thePlayer.motionY = (speed * 0.6D);
        } else if (mc.thePlayer.movementInput.sneak) {
          mc.thePlayer.motionY = (-speed * 0.6D);
        } else {
          mc.thePlayer.motionY = 0.0D;
        }
      case "AntiKick": /** Damage fly, stand on the edge of a block, 8+ fall*/
        if (mc.thePlayer.movementInput.jump) {
          mc.thePlayer.motionY = (speed * 0.6D);
        } else if (mc.thePlayer.movementInput.sneak) {
          mc.thePlayer.motionY = (-speed * 0.6D);
        } else {
          mc.thePlayer.motionY = 0.0D;
        }
        updateFlyHeight();
        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        if (((this.flyHeight <= 290.0D) && (this.kickTimer.delay(500.0F))) || ((this.flyHeight > 290.0D) && 
          (this.kickTimer.delay(100.0F))))
        {
          goToGround();
          this.kickTimer.reset();
        }
        break;
      }
    }
  }
  if ((event instanceof EventMove))
  {
    EventMove em = (EventMove)event;
    double speed = 0;
    double forward = mc.thePlayer.movementInput.moveForward;
    double strafe = mc.thePlayer.movementInput.moveStrafe;
    float yaw = mc.thePlayer.rotationYaw;
    if ((forward == 0.0D) && (strafe == 0.0D))
    {
      em.setX(0.0D);
      em.setZ(0.0D);
    }
    else
    {
      if (forward != 0.0D)
      {
        if (strafe > 0.0D) {
          yaw += (forward > 0.0D ? -45 : 45);
        } else if (strafe < 0.0D) {
          yaw += (forward > 0.0D ? 45 : -45);
        }
        strafe = 0.0D;
        if (forward > 0.0D) {
          forward = 1.0D;
        } else if (forward < 0.0D) {
          forward = -1.0D;
        }
      }
      if (((Options)((Setting)this.settings.get("FLYMODE")).getValue()).getSelected().equals("Hypixel")) {
    	  if(!GG) {
    		  Minecraft.getMinecraft().thePlayer.cameraYaw = 0.1f;
    	           
    	  
       
			if (b2) {
				if (level != 1 || Minecraft.getMinecraft().thePlayer.moveForward == 0.0F
						&& Minecraft.getMinecraft().thePlayer.moveStrafing == 0.0F) {
					if (level == 2) {
						level = 3;
						em.setY(mc.thePlayer.motionY = 0.42);
						moveSpeed *= 2.14519999D;
						} else if (level == 3) {
						level = 4;
						double difference = (Minecraft.getMinecraft().thePlayer.ticksExisted % 2 == 0 ? 0.004D : 0.0083D)
								* (lastDist - getBaseMoveSpeed());
						moveSpeed = lastDist - difference;
						
					} else {
						if (Minecraft.getMinecraft().theWorld
								.getCollidingBoundingBoxes(Minecraft.getMinecraft().thePlayer,
										Minecraft.getMinecraft().thePlayer.boundingBox.offset(0.0D,
												Minecraft.getMinecraft().thePlayer.motionY, 0.0D))
								.size() > 0 || Minecraft.getMinecraft().thePlayer.isCollidedVertically) {
							level = 1;
						   		
						}
						moveSpeed = lastDist - lastDist / 158.0D;
					}
				} else {
					level = 2;
					int amplifier = Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)
							? Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed)
									.getAmplifier() + 2
							: 1;
				 		
					double boost = Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed) ? 1.76
							: ((Number)((Setting)this.settings.get(SPEED)).getValue()).floatValue();
					  mc.thePlayer.motionX *= 0.0D;
				      mc.thePlayer.motionZ *= 0.0D;      
					moveSpeed = boost * getBaseMoveSpeed();
				
					
				}
				speed = Math.max(moveSpeed,getBaseMoveSpeed());
				
			
				
      em.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * 
              Math.sin(Math.toRadians(yaw + 90.0F)));
      em.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * 
              Math.cos(Math.toRadians(yaw + 90.0F)));
    }
    	  }
  } 
 }
}
  
}
}