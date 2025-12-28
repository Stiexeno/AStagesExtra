package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

public interface ItemConsumeSource {
    /**
     * @return true if this source is applicable in the current environment
     *         (e.g. mod loaded, correct side, etc.)
     */
    boolean isAvailable();
    
    /**
     * Try to consume items.
     *
     * @param context  shared context for the task execution
     * @return remaining amount after this source ran
     */
    long consume(ItemConsumeContext context);
}
