package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_7_R3.EntityZombie;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

public class ZombieController extends MobEntityController {
    public ZombieController() {
        super(EntityZombieNPC.class);
    }

    @Override
    public Zombie getBukkitEntity() {
        return (Zombie) super.getBukkitEntity();
    }

    public static class EntityZombieNPC extends EntityZombie implements NPCHolder {
        private final CitizensNPC npc;

        public EntityZombieNPC(World world) {
            this(world, null);
        }

        public EntityZombieNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected void a(double d0, boolean flag) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag);
            }
        }

        @Override
        protected String aS() {
            return npc == null ? super.aS() : npc.data().get(NPC.HURT_SOUND_METADATA, super.aS());
        }

        @Override
        protected String aT() {
            return npc == null ? super.aT() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.aT());
        }

        @Override
        protected void b(float f) {
            if (npc == null || !npc.isFlyable()) {
                super.b(f);
            }
        }

        @Override
        public boolean bN() {
            if (npc == null)
                return super.bN();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.bN();
            if (super.bN()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void bm() {
            super.bm();
            if (npc != null) {
                npc.update();
            }
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
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
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
                bukkitEntity = new ZombieNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean h_() {
            if (npc == null || !npc.isFlyable()) {
                return super.h_();
            } else {
                return false;
            }
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

    public static class ZombieNPC extends CraftZombie implements NPCHolder {
        private final CitizensNPC npc;

        public ZombieNPC(EntityZombieNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}