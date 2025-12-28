package com.alessandro.astages.integration.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

public class AFTBTasks
{
    public static final TaskType SINK_ITEM_TASK = TaskTypes.register(FTBQuestsAPI.rl("sink_item_task"), SinkItemTask::new, () -> Icon.getIcon("minecraft:item/book"));
    
    public static void init()
    {
        SINK_ITEM_TASK.setGuiProvider((gui, quest, callback) ->
        {
            ItemStackConfig c = new ItemStackConfig(false, false);
            
            new SelectItemStackScreen(c, accepted ->
            {
                gui.run();
                if (accepted)
                {
                    SinkItemTask itemTask = new SinkItemTask(0L, quest);
                    itemTask.setStackAndCount(c.getValue(), c.getValue().getCount());
                    callback.accept(itemTask, itemTask.getType().makeExtraNBT());
                }
            }).openGui();
        });
    }
}
