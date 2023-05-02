package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.event.impl.EventStep;
import exhibition.management.notifications.user.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.Setting;
import exhibition.module.data.SettingsMap;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.PlayerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Timer;

public class Speed
extends Module {
    private final String MODE = "MODE";
    private double speed;
    private double lastDist;
    public static int stage;
    private Setting step = new Setting("STEP", false, "Disables speed while stepping up multiple stairs/slabs.");
    int steps;
    private int level = 1;
    private double[] values = {0.1D, 0.09316090325960147D, 1.81D, 2.049D, 0.66D};
	private double distance;
	
    public Speed(ModuleData data) {
        super(data);
        this.settings.put("STEP", this.step);
        this.settings.put("MODE", new Setting("MODE", new Options("Speed Mode", "Hypixel", new String[]{"Hop", "Hypixel", "OnGround", "YPort", "OldHop", "OldSlow"}), "Speed bypass method."));
    }

    private double defaultSpeed() {
        double baseSpeed = 0.2873;
        if (Speed.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = Speed.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    @Override
    public void onEnable() {
        Module[] modules;
        if (Speed.mc.thePlayer != null) {
            this.speed = this.defaultSpeed();
        }
        this.lastDist = 0.0;
        stage = 2;
        Speed.mc.timer.timerSpeed = 1.0f;
        for (Module module : modules = new Module[]{(Module)Client.getModuleManager().get(ZoomFly.class), (Module)Client.getModuleManager().get(Scaffold.class),(Module)Client.getModuleManager().get(LongJump.class)}) {
            if (!module.isEnabled()) continue;
            module.toggle();
            Notifications.getManager().post("Movement Check", "Disabled extra modules.", 250L, Notifications.Type.NOTIFY);
        
        }
    }

    @Override
    public void onDisable() {
        Speed.mc.timer.timerSpeed = 1.0f;
    }

    @RegisterEvent(events={EventMove.class, EventMotionUpdate.class, EventStep.class})
    public void onEvent(Event event) {
        Object eventStep;
        String currentMode = ((Options)((Setting)this.settings.get("MODE")).getValue()).getSelected();
        double gay;
        double xDist;
        this.setSuffix(currentMode);
        if (event instanceof EventStep && ((Boolean)this.step.getValue()).booleanValue() && !((EventStep)(eventStep = (EventStep)event.cast())).isPre()) {
            ++this.steps;
            if (this.steps > 2) {
                stage = -1;
            }
        }
        switch (currentMode) {
            case "HPort": {
                Event em;
                if (stage < 1) {
                    ++stage;
                    this.lastDist = 0.0;
                    break;
                }
                if (event instanceof EventMove) {
                    em = (EventMove)event;
                    if (Speed.mc.thePlayer.onGround || stage == 3) {
                        if (!Speed.mc.thePlayer.isCollidedHorizontally && Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) {
                            if (stage == 2) {
                                this.speed *= 1.374;
                                stage = 3;
                            } else if (stage == 3) {
                                stage = 2;
                                double difference = 0.66 * (this.lastDist - this.defaultSpeed());
                                this.speed = this.lastDist - difference;
                            } else {
                                List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                                if (collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) {
                                    stage = 1;
                                }
                            }
                        } else {
                            Speed.mc.timer.timerSpeed = 1.0f;
                        }
                        this.speed = Math.max(this.speed, this.defaultSpeed());
                        this.setMotion((EventMove)em, this.speed);
                    }
                }
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                if (stage == 3) {
                    gay = 0.398936;
                    if (Speed.mc.thePlayer.isPotionActive(Potion.jump)) {
                        gay = (float)(Speed.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f;
                    }
                    ((EventMotionUpdate)em).setY(((EventMotionUpdate)em).getY() + gay);
                }
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                break;
            }
            case "Hypixel": {
            	Event em;  
                if (event instanceof EventMove) {
                	double LiquidSlowly2 =  Minecraft.getMinecraft().thePlayer.ticksExisted % 1 == 0 ? 0.055D : 0.075D; 
             	em = (EventMove)event;
                
             	if (mc.thePlayer.isSneaking()) {
	                return;
	            }
                if (stage < 1) {
                    ++stage;
                    this.distance = 0.0;
                    break;
                } 

                if (this.steps > 2) {
                    this.steps = 0;
                }
                if(mc.thePlayer.onGround) {
         	    	  this.level =2;
         	    }
               
                if (this.round(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == this.round(0.138D, 3)) {
	                mc.thePlayer.motionY -= 0.10D;
	                mc.thePlayer.posY -= em.y -= this.values[1];
	                }

                
                if(PlayerUtil.MovementInput()) {
    	            double[] var2 = new double[]{0.368D, 0.55D, 0.249D, 0.171D, 0.427D};
    	            Timer var10000;
    	             if(mc.thePlayer.onGround) {
    	               var10000 = this.mc.timer;
    	              } else {
    	               var10000 = this.mc.timer;
    	             mc.timer.timerSpeed =  mc.thePlayer.motionY <= -0.009999999776482582D && !this.isBlockUnder(Material.air)?1.02F: 1.04F;
    	            }
    	         } 
               
                if (stage == 2 && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) && Speed.mc.thePlayer.isCollidedVertically && Speed.mc.thePlayer.onGround) {
                	this.level = 3;
                	double gay2 = 0.418;
                    if (Speed.mc.thePlayer.isPotionActive(Potion.jump)) {
                        gay2 += (double)((float)(Speed.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f);
                    }
                    Speed.mc.thePlayer.motionY = gay2;
                    ((EventMove)em).setY(gay2);
                    this.speed *= 1.72D+LiquidSlowly2;

                } else if (stage == 3) {
                	this.level = 4;
                	double difference = 0.67* (this.distance - this.getBaseMovementSpeed());
                     speed = this.distance - difference;
             
                } else {
                    List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                    if ((collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) && stage > 0) {
                        stage = Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f ? 1 : 0;
                    }else if ((mc.thePlayer.onGround) && ((mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D)).size() > 0) || (mc.thePlayer.isCollidedVertically))) {
                        this.level = 1;
                    }
                    this.speed = this.distance - this.distance / 158.0D;
                               }
                  
                this.speed = Math.max(this.speed, this.defaultSpeed());
                    double forward = Speed.mc.thePlayer.movementInput.moveForward;
                    double strafe = Speed.mc.thePlayer.movementInput.moveStrafe;
                    float yaw = Speed.mc.thePlayer.rotationYaw;
                    if (forward == 0.0 && strafe == 0.0) {
    		            ((EventMove) em).setX(0.0);
    		            ((EventMove) em).setZ(0.0);
    		        }
    		        else {
    		            if (forward != 0.0) {
    		                if (strafe > 0.0) {
    		                    yaw += ((forward > 0.0) ? -48 : 48);
    		                }
    		                else if (strafe < 0.0) {
    		                    yaw += ((forward > 0.0) ? 48 : -48);
    		                }
    		                strafe = 0.0;
    		                if (forward > 0.0) {
    		                    forward = 1.0;
    		                }
    		                else if (forward < 0.0) {
    		                    forward = -1.0;
    		                }
    		            }
    		        }
                    ((EventMove) em).setX(forward * speed* Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
    	            ((EventMove) em).setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
    	             Speed.mc.thePlayer.stepHeight = 0.6f;
    	             if(forward == 0.0F && strafe == 0.0F) {
    	 	            em.x = 0.0D;
    	 	            em.z = 0.0D;
    	 	            Minecraft.getMinecraft().thePlayer.motionX *= Minecraft.getMinecraft().thePlayer.motionZ *= 0.0D;
    	 	         } else if(forward != 0.0F) {
    	 	            float var10000;
    	 	            if(strafe >= 1.0F) {
    	 	               var10000 = yaw + (forward > 0.0F?-45.0F:45.0F);
    	 	               strafe = 0.0F;
    	 	            } else if(strafe <= -1.0F) {
    	 	               var10000 = yaw + (forward > 0.0F?45.0F:-45.0F);
    	 	               strafe = 0.0F;
    	 	            }

    	 	            if(forward > 0.0F) {
    	 	               forward = 1.0F;
    	 	            } else if(forward < 0.0F) {
    	 	               forward = -1.0F;
    	 	            }
    	 	         }
                    ++this.stage;
                 
                }
                 PlayerUtil.setSpeed(PlayerUtil.getSpeed()-0.08D);
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.distance = Math.sqrt(xDist * xDist + zDist * zDist);
               break;
            }
            case "Hop": {
                Event em;
                if (event instanceof EventMove) {
                    em = (EventMove)event;
                    if (Speed.mc.thePlayer.moveForward == 0.0f && Speed.mc.thePlayer.moveStrafing == 0.0f) {
                        this.speed = this.defaultSpeed();
                    }
                    if (stage == 1 && Speed.mc.thePlayer.isCollidedVertically && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        this.speed = 1.35 + this.defaultSpeed() - 0.01;
                    }
                    if (stage == 2 && Speed.mc.thePlayer.isCollidedVertically && Speed.mc.thePlayer.onGround && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        Speed.mc.thePlayer.motionY = 0.41999998688697815;
                        ((EventMove)em).setY(0.41999998688697815);
                        Speed.mc.thePlayer.isAirBorne = true;
                        this.speed *= 1.533;
                    } else if (stage == 3) {
                        double difference = 0.72 * (this.lastDist - this.defaultSpeed()+0.08);
                        this.speed = this.lastDist - difference;
                    } else {
                        List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f ? 1 : 0;
                        }
                        this.speed = this.lastDist - this.lastDist / 159.0;
                    }
                    this.speed = Math.max(this.speed, this.defaultSpeed());
                    if (stage > 0) {
                        this.setMotion((EventMove)em, this.speed);
                    }
                    if (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                break;
            }
            case "OnGround": {
                EventMotionUpdate em;
                if (!(event instanceof EventMotionUpdate) || !(em = (EventMotionUpdate)event).isPre()) break;
                Speed.mc.timer.timerSpeed = 1.085f;
                double forward = Speed.mc.thePlayer.movementInput.moveForward;
                double strafe = Speed.mc.thePlayer.movementInput.moveStrafe;
                if (!(forward == 0.0 && strafe == 0.0 || Speed.mc.thePlayer.isJumping || Speed.mc.thePlayer.isInWater() || Speed.mc.thePlayer.isOnLadder() || Speed.mc.thePlayer.isCollidedHorizontally)) {
                    em.setY(Speed.mc.thePlayer.posY + (Speed.mc.thePlayer.ticksExisted % 2 != 0 ? 0.4 : 0.0));
                }
                this.speed = Math.max(Speed.mc.thePlayer.ticksExisted % 2 == 0 ? 2.1 : 1.3, this.defaultSpeed());
                float yaw = Speed.mc.thePlayer.rotationYaw;
                if (forward == 0.0 && strafe == 0.0) {
                    Speed.mc.thePlayer.motionX = 0.0;
                    Speed.mc.thePlayer.motionZ = 0.0;
                    break;
                }
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += (float)(forward > 0.0 ? -45 : 45);
                    } else if (strafe < 0.0) {
                        yaw += (float)(forward > 0.0 ? 45 : -45);
                    }
                    strafe = 0.0;
                    if (forward > 0.0) {
                        forward = 0.15;
                    } else if (forward < 0.0) {
                        forward = -0.15;
                    }
                }
                if (strafe > 0.0) {
                    strafe = 0.15;
                } else if (strafe < 0.0) {
                    strafe = -0.15;
                }
                Speed.mc.thePlayer.motionX = forward * this.speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * this.speed * Math.sin(Math.toRadians(yaw + 90.0f));
                Speed.mc.thePlayer.motionZ = forward * this.speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * this.speed * Math.cos(Math.toRadians(yaw + 90.0f));
                break;
            }
            case "YPort": {
                Event em;
                if (stage < 1) {
                    ++stage;
                    this.lastDist = 0.0;
                    break;
                }
                if (event instanceof EventMove) {
                    em = (EventMove)event;
                    if (Speed.mc.thePlayer.onGround || stage == 3) {
                        if (!Speed.mc.thePlayer.isCollidedHorizontally && Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) {
                            if (stage == 2) {
                                this.speed *= 2.149;
                                stage = 3;
                            } else if (stage == 3) {
                                stage = 2;
                                double difference = 0.66 * (this.lastDist - this.defaultSpeed());
                                this.speed = this.lastDist - difference;
                            } else {
                                List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                                if (collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) {
                                    stage = 1;
                                }
                            }
                        } else {
                            Speed.mc.timer.timerSpeed = 1.0f;
                        }
                        this.speed = Math.max(this.speed, this.defaultSpeed());
                        this.setMotion((EventMove)em, this.speed);
                    }
                }
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                if (stage == 3) {
                    gay = 0.40453293;
                    if (Speed.mc.thePlayer.isPotionActive(Potion.jump)) {
                        gay = (float)(Speed.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f;
                    }
                    ((EventMotionUpdate)em).setY(((EventMotionUpdate)em).getY() + gay);
                }
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                break;
            }
            case "OldHop": {
                Event em;
                if (event instanceof EventMove) {
                    em = (EventMove)event;
                    if (Speed.mc.thePlayer.moveForward == 0.0f && Speed.mc.thePlayer.moveStrafing == 0.0f) {
                        this.speed = this.defaultSpeed();
                    }
                    if (stage == 1 && Speed.mc.thePlayer.isCollidedVertically && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        this.speed = 0.25 + this.defaultSpeed() - 0.01;
                    } else if (stage == 2 && Speed.mc.thePlayer.isCollidedVertically && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        Speed.mc.thePlayer.motionY = 0.4;
                        ((EventMove)em).setY(0.4);
                        this.speed *= 2.149;
                    } else if (stage == 3) {
                        double difference = 0.66 * (this.lastDist - this.defaultSpeed());
                        this.speed = this.lastDist - difference;
                    } else {
                        List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = 1.35 * this.defaultSpeed() - 0.01 > this.speed ? 0 : (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f ? 1 : 0);
                        }
                        this.speed = this.lastDist - this.lastDist / 159.0;
                    }
                    this.speed = Math.max(this.speed, this.defaultSpeed());
                    if (stage > 0) {
                        this.setMotion((EventMove)em, this.speed);
                    }
                    if (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                break;
            }
            case "OldSlow": {
                Event em;
                if (event instanceof EventMove) {
                    em = (EventMove)event;
                    if (Speed.mc.thePlayer.moveForward == 0.0f && Speed.mc.thePlayer.moveStrafing == 0.0f) {
                        this.speed = this.defaultSpeed();
                    }
                    if (stage == 1 && Speed.mc.thePlayer.isCollidedVertically && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        this.speed = 0.25 + this.defaultSpeed() - 0.01;
                    } else if (stage == 2 && Speed.mc.thePlayer.isCollidedVertically && (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f)) {
                        Speed.mc.thePlayer.motionY = 0.4;
                        ((EventMove)em).setY(0.4);
                        this.speed *= 1.749;
                    } else if (stage == 3) {
                        double difference = 0.66 * (this.lastDist - this.defaultSpeed());
                        this.speed = this.lastDist - difference;
                    } else {
                        List collidingList = Speed.mc.theWorld.getCollidingBoundingBoxes(Speed.mc.thePlayer, Speed.mc.thePlayer.boundingBox.offset(0.0, Speed.mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || Speed.mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = 1.35 * this.defaultSpeed() - 0.01 > this.speed ? 0 : (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f ? 1 : 0);
                        }
                        this.speed = this.lastDist - this.lastDist / 159.0;
                    }
                    this.speed = Math.max(this.speed, this.defaultSpeed());
                    if (stage > 0) {
                        this.setMotion((EventMove)em, this.speed);
                    }
                    if (Speed.mc.thePlayer.moveForward != 0.0f || Speed.mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (!(event instanceof EventMotionUpdate) || !((EventMotionUpdate)(em = (EventMotionUpdate)event)).isPre()) break;
                xDist = Speed.mc.thePlayer.posX - Speed.mc.thePlayer.prevPosX;
                double zDist = Speed.mc.thePlayer.posZ - Speed.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
            }
        }
    }
    
    public static double getBaseMovementSpeed() {
        double baseSpeed = 0.2873;
        if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }
   
    public boolean isBlockUnder(Material blockMaterial) {
	      return mc.theWorld.getBlockState(this.underPlayer()).getBlock().getMaterial() == blockMaterial;
	   }

	public BlockPos underPlayer() {
        return new BlockPos(this.mc.thePlayer.posX, this.mc.thePlayer.getEntityBoundingBox().minY - 1.0, this.mc.thePlayer.posZ);
    }
	
    private void setMotion(EventMove em, double speed) {
        double forward = Speed.mc.thePlayer.movementInput.moveForward;
        double strafe = Speed.mc.thePlayer.movementInput.moveStrafe;
        float yaw = Speed.mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            em.setX(0.0).setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            em.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
            em.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
        }
    }
    public double round(double var1, int var3) {
	      if(var3 < 0) {
	         throw new IllegalArgumentException();
	      } else {
	         BigDecimal var4 = new BigDecimal(var1);
	         var4 = var4.setScale(var3, RoundingMode.HALF_UP);
	         return var4.doubleValue();
	      }
	   }
}

