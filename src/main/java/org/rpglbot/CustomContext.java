package org.rpglbot;

import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLEffect;
import org.rpgl.core.RPGLFactory;
import org.rpgl.core.RPGLObject;
import org.rpgl.json.JsonArray;
import org.rpgl.json.JsonObject;
import org.rpgl.subevent.AttackRoll;
import org.rpgl.subevent.DamageAffinity;
import org.rpgl.subevent.DamageDelivery;
import org.rpgl.subevent.DamageRoll;
import org.rpgl.subevent.GiveEffect;
import org.rpgl.subevent.HealingDelivery;
import org.rpgl.subevent.SavingThrow;
import org.rpgl.subevent.Subevent;
import org.rpgl.uuidtable.UUIDTable;

import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class CustomContext extends RPGLContext {

    private final Stack<String> messages = new Stack<>();

    @Override
    public boolean isObjectsTurn(RPGLObject object) {
        return Objects.equals(object, RPGLClient.currentTurnObject());
    }

    @Override
    public void viewCompletedSubevent(Subevent subevent) {
        switch (subevent.getSubeventId()) {
            case "attack_roll" -> viewAttackRoll((AttackRoll) subevent);
            case "saving_throw" -> viewSavingThrow((SavingThrow) subevent);
            case "damage_affinity" -> viewDamageAffinity((DamageAffinity) subevent);
            case "damage_delivery" -> viewDamageDelivery((DamageDelivery) subevent);
            case "damage_roll" -> viewDamageRoll((DamageRoll) subevent);
            case "give_effect" -> viewGiveEffect((GiveEffect) subevent);
            case "healing_delivery" -> viewHealingDelivery((HealingDelivery) subevent);
        }
    }

    public Stack<String> getMessages() {
        return this.messages;
    }

    public void clearMessages() {
        this.messages.clear();
    }

    private void viewAttackRoll(AttackRoll attackRoll) {
        int roll = attackRoll.get();
        int armorClass = attackRoll.getTargetArmorClass();
        String hitOrMiss;
        if (attackRoll.getBase() >= attackRoll.getCriticalHitThreshold()) {
            hitOrMiss = "CRITICAL HIT";
        } else if (attackRoll.getBase() == 1) {
            hitOrMiss = "CRITICAL MISS";
        } else if (roll >= armorClass) {
            hitOrMiss = "HIT";
        } else {
            hitOrMiss = "MISS";
        }
        messages.add(String.format("%s attacks %s! %s vs AC %s (%s)",
                attackRoll.getSource().getName(),
                attackRoll.getTarget().getName(),
                roll,
                armorClass,
                hitOrMiss
        ));
    }

    private void viewSavingThrow(SavingThrow savingThrow) {
        int roll = savingThrow.get();
        int difficultyClass = savingThrow.getDifficultyClass();
        messages.add(String.format("%s makes a %s save! %s vs DC %s (%s)",
                savingThrow.getTarget().getName(),
                savingThrow.getAbility(RPGLClient.CONTEXT),
                roll,
                difficultyClass,
                roll < difficultyClass ? "FAIL" : "PASS"
        ));
    }

    private void viewDamageAffinity(DamageAffinity damageAffinity) {
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

    private void viewDamageDelivery(DamageDelivery damageDelivery) {
        StringBuilder stringBuilder = new StringBuilder();
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

    private void viewDamageRoll(DamageRoll damageRoll) {
//        // this is where a "name" field might be helpful for display purposes...
//        StringBuilder stringBuilder = new StringBuilder();
//        JsonArray damage = damageRoll.getDamage();
//        for (int i = 0; i < damage.size(); i++) {
//            JsonObject damageJson = damage.getJsonObject(i);
//            JsonArray dice = damageJson.getJsonArray("dice");
//            int bonus = damageJson.getInteger("bonus");
//            for (int j = 0; j < dice.size(); j++) {
//                if (j == 0) {
//                    stringBuilder.append('(');
//                }
//                stringBuilder.append(dice.getJsonObject(j).getInteger("roll"));
//                if (j < dice.size() - 1) {
//                    stringBuilder.append(" + ");
//                } else if (bonus > 0) {
//                    stringBuilder.append(") + ");
//                } else {
//                    stringBuilder.append(')');
//                }
//            }
//            if (bonus > 0) {
//                stringBuilder.append(bonus).append(' ').append(damageJson.getString("damage_type"));
//            } else if (dice.size() > 0) {
//                stringBuilder.append(' ').append(damageJson.getString("damage_type"));
//            }
//            if (i < damage.size() - 1) {
//                stringBuilder.append(" + ");
//            }
//        }
//        String message = stringBuilder.toString();
//        if (!Objects.equals(message, "")) {
//            messages.add(message);
//        }
    }

    private void viewGiveEffect(GiveEffect giveEffect) {
        RPGLEffect temporaryEffect = RPGLFactory.newEffect(giveEffect.json.getString("effect")); // <--- magic string
        messages.add(String.format("Applied effect %s to %s!",
                temporaryEffect.getName(),
                giveEffect.getTarget().getName()
        ));
        UUIDTable.unregister(temporaryEffect.getUuid());
    }

    private void viewHealingDelivery(HealingDelivery healingDelivery) {
        messages.add(String.format("%s heals for %s hit points!",
                healingDelivery.getTarget().getName(),
                healingDelivery.getHealing()
        ));
    }

}
