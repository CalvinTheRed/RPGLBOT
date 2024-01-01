package org.rpglbot;

import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLObject;
import org.rpgl.json.JsonObject;
import org.rpgl.subevent.AttackRoll;
import org.rpgl.subevent.DamageDelivery;
import org.rpgl.subevent.Subevent;

import java.util.Map;
import java.util.Stack;

public class CustomContext extends RPGLContext {

    private final Stack<String> messages = new Stack<>();

    @Override
    public boolean isObjectsTurn(RPGLObject object) {
        return false;
    }

    @Override
    public void viewCompletedSubevent(Subevent subevent) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (subevent.getSubeventId()) {
            case "damage_delivery" -> {
                DamageDelivery damageDelivery = (DamageDelivery) subevent;
                stringBuilder.append(damageDelivery.getTarget().getName()).append(" takes ");
                JsonObject damage = damageDelivery.getDamage();
                int numDamageTypes = damage.asMap().size();
                for (Map.Entry<String, ?> damageEntry : damage.asMap().entrySet()) {
                    stringBuilder
                            .append(damage.getInteger(damageEntry.getKey()))
                            .append(" ")
                            .append(damageEntry.getKey())
                            .append(numDamageTypes > 1 ? ", " : " ");
                    numDamageTypes--;
                }
                stringBuilder.append("damage!\n");

                JsonObject targetHealthData = damageDelivery.getTarget().getHealthData();
                int current = targetHealthData.getInteger("current");
                int temporary = targetHealthData.getInteger("temporary");
                stringBuilder.append("Health: ").append(current);
                if (temporary > 0) {
                    stringBuilder.append(" (+").append(temporary).append(")");
                }
                messages.add(stringBuilder.toString());
            }
            case "attack_roll" -> {
                AttackRoll attackRoll = (AttackRoll) subevent;
                stringBuilder
                        .append(attackRoll.getSource().getName())
                        .append(" attacks ")
                        .append(attackRoll.getTarget().getName())
                        .append("! ")
                        .append(attackRoll.get())
                        .append(" vs AC ")
                        .append(attackRoll.getTargetArmorClass());
                if (attackRoll.getBase() >= attackRoll.getCriticalHitThreshold()) {
                    stringBuilder.append(" (CRITICAL HIT)");
                } else if (attackRoll.getBase() == 1) {
                    stringBuilder.append(" (CRITICAL MISS)");
                } else if (attackRoll.get() >= attackRoll.getTargetArmorClass()) {
                    stringBuilder.append(" (HIT)");
                } else {
                    stringBuilder.append(" (MISS)");
                }
                messages.add(stringBuilder.toString());
            }
        }
    }

    public Stack<String> getMessages() {
        return this.messages;
    }

    public void clearMessages() {
        this.messages.clear();
    }
}
