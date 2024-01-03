package org.rpglbot;

import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLObject;
import org.rpgl.json.JsonArray;
import org.rpgl.json.JsonObject;
import org.rpgl.subevent.AttackRoll;
import org.rpgl.subevent.DamageAffinity;
import org.rpgl.subevent.DamageDelivery;
import org.rpgl.subevent.HealingDelivery;
import org.rpgl.subevent.SavingThrow;
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
            case "attack_roll" -> {
                AttackRoll attackRoll = (AttackRoll) subevent;
                int roll = attackRoll.get();
                int armorClass = attackRoll.getTargetArmorClass();
                stringBuilder
                        .append(attackRoll.getSource().getName())
                        .append(" attacks ")
                        .append(attackRoll.getTarget().getName())
                        .append("! ")
                        .append(roll)
                        .append(" vs AC ")
                        .append(armorClass);
                if (attackRoll.getBase() >= attackRoll.getCriticalHitThreshold()) {
                    stringBuilder.append(" (CRITICAL HIT)");
                } else if (attackRoll.getBase() == 1) {
                    stringBuilder.append(" (CRITICAL MISS)");
                } else if (roll >= armorClass) {
                    stringBuilder.append(" (HIT)");
                } else {
                    stringBuilder.append(" (MISS)");
                }
                messages.add(stringBuilder.toString());
            }
            case "saving_throw" -> {
                SavingThrow savingThrow = (SavingThrow) subevent;
                int roll = savingThrow.get();
                int difficultyClass = savingThrow.getSaveDifficultyClass();
                stringBuilder
                        .append(savingThrow.getTarget().getName())
                        .append(" makes a ")
                        .append(savingThrow.getAbility(RPGLClient.CONTEXT))
                        .append(" save! ")
                        .append(roll)
                        .append(" vs DC ")
                        .append(difficultyClass)
                        .append(roll < difficultyClass ? " (FAIL)" : " (PASS)");
                messages.add(stringBuilder.toString());
            }
            case "damage_affinity" -> {
                DamageAffinity damageAffinity = (DamageAffinity) subevent;
                RPGLObject target = damageAffinity.getTarget();
                JsonArray affinities = damageAffinity.json.getJsonArray("affinities"); // <-- magic string here
                for (int i = 0; i < affinities.size(); i++) {
                    String damageType = affinities.getJsonObject(i).getString("damage_type");
                    if (!damageAffinity.isImmune(damageType)) {
                        if (damageAffinity.isResistant(damageType)) {
                            messages.add(String.format("%s is resistant to %s!", target.getName(), damageType));
                        }
                        if (damageAffinity.isVulnerable(damageType)) {
                            messages.add(String.format("%s is vulnerable to %s!", target.getName(), damageType));
                        }
                    } else {
                        messages.add(String.format("%s is immune to %s!", target.getName(), damageType));
                    }
                }
            }
            case "damage_delivery" -> {
                DamageDelivery damageDelivery = (DamageDelivery) subevent;
                JsonObject damage = damageDelivery.getDamage();
                if (!damage.asMap().isEmpty()) {
                    stringBuilder.append(damageDelivery.getTarget().getName()).append(" takes ");
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
            }
            case "healing_delivery" -> {
                HealingDelivery healingDelivery = (HealingDelivery) subevent;
                stringBuilder
                        .append(healingDelivery.getTarget().getName())
                        .append(" heals for ")
                        .append(healingDelivery.getHealing())
                        .append(" hit points!");
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
