package scott.wemessage.commons.types;

import scott.wemessage.commons.utils.StringUtils;

public enum MessageEffect {

    INVISIBLE_INK("invisibleink"),
    GENTLE("gentle"),
    LOUD("loud"),
    CONFETTI("confetti"),
    FIREWORKS("fireworks"),
    SHOOTING_STAR("star"),
    NONE("NONE");

    private String effectName;

    MessageEffect(String effectName){
        this.effectName = effectName;
    }

    public String getEffectName() {
        return effectName;
    }

    public static MessageEffect from(String name){
        if (StringUtils.isEmpty(name)) return NONE;

        for (MessageEffect effect : MessageEffect.values()){
            if (StringUtils.containsIgnoreCase(name, effect.getEffectName())){
                return effect;
            }
        }
        return NONE;
    }
}