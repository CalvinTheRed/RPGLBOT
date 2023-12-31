package org.rpglbot;

import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLObject;
import org.rpgl.json.JsonObject;
import org.rpgl.subevent.AttackRoll;
import org.rpgl.subevent.DamageAffinity;
import org.rpgl.subevent.DamageDelivery;
import org.rpgl.subevent.Subevent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomContext extends RPGLContext {

    private final List<String> messages = new LinkedList<>();

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
                stringBuilder.append("damage!");
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
                    stringBuilder.append(" (HIT)\nHealth: ");
                    JsonObject targetHealthData = attackRoll.getTarget().getHealthData();
                    int current = targetHealthData.getInteger("current");
                    int temporary = targetHealthData.getInteger("temporary");
                    stringBuilder.append(current);
                    if (temporary > 0) {
                        stringBuilder.append(" (+").append(temporary).append(")");
                    }
                } else {
                    stringBuilder.append(" (MISS)");
                }
                messages.add(stringBuilder.toString());
            }
        }
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<>(this.messages);
        this.messages.clear();
        return messages;
    }
}
