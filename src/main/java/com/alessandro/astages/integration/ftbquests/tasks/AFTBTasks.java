package com.alessandro.astages.integration.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

public class AFTBTasks
{
    public static final TaskType SINK_ITEM_TASK = TaskTypes.register(FTBQuestsAPI.rl("sink_item_task"), SinkItemTask::new, () -> Icon.getIcon("minecraft:item/book"));
    
    public static void init()
    {
    }
}
