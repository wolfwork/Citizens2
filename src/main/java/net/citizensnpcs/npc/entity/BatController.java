package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_7_R3.EntityBat;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftBat;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.entity.Bat;
import org.bukkit.util.Vector;

public class BatController extends MobEntityController {
    public BatController() {
        super(EntityBatNPC.class);
    }

    @Override
    public Bat getBukkitEntity() {
        return (Bat) super.getBukkitEntity();
    }

    public static class BatNPC extends CraftBat implements NPCHolder {
        private final CitizensNPC npc;

        public BatNPC(EntityBatNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityBatNPC extends EntityBat implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBatNPC(World world) {
            this(world, null);
        }

        public EntityBatNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
                setFlying(false);
            }
        }

        @Override
        protected String aS() {
            return npc == null || !npc.data().has(NPC.HURT_SOUND_METADATA) ? super.aS() : npc.data().get(
                    NPC.HURT_SOUND_METADATA, super.aS());
        }

        @Override
        protected String aT() {
            return npc == null || !npc.data().has(NPC.DEATH_SOUND_METADATA) ? super.aT() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.aT());
        }

        @Override
        public void bm() {
            if (npc == null) {
                super.bm();
            } else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        @Override
        public boolean bN() {
            if (npc == null) {
                return super.bN();
            }
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.bN();
            if (super.bN()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void collide(net.minecraft.server.v1_7_R3.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new BatNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void h() {
            super.h();
            if (npc != null) {
                npc.update();
            }
        }

        public void setFlying(boolean flying) {
            setStartled(flying);
        }

        @Override
        protected String t() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.t() : npc.data().get(NPC.AMBIENT_SOUND_METADATA, super.t());
        }

        @Override
        protected void w() {
            if (npc == null) {
                super.w();
            }
        }
    }
}