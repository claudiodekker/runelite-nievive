package com.nievive;

import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Nievive"
)
public class NievivePlugin extends Plugin {
    @Inject
    protected Client client;

    @Inject
    protected ClientThread clientThread;

    private static final int STEVE_VARBIT_ID = 5037;

    protected int realSteveVarbitValue = -1;

    protected boolean varbitReplaced = false;

    @Override
    protected void shutDown() {
        this.resetVarbit();
    }

    protected void resetVarbit() {
        if (!this.varbitReplaced) {
            return;
        }

        clientThread.invokeLater(() -> {
            client.setVarbit(STEVE_VARBIT_ID, this.realSteveVarbitValue);
            this.realSteveVarbitValue = -1;
            this.varbitReplaced = false;
        });
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        WorldPoint location = player.getWorldLocation();
        if (location == null) {
            return;
        }

        if (this.realSteveVarbitValue == -1) {
            this.realSteveVarbitValue = client.getVarbitValue(STEVE_VARBIT_ID);
        }

        if (!this.varbitReplaced) {
            this.varbitReplaced = true;
            clientThread.invokeLater(() -> client.setVarbit(STEVE_VARBIT_ID, 0));
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarbitId() != STEVE_VARBIT_ID) {
            return;
        }

        if (this.varbitReplaced && this.realSteveVarbitValue != -1 && varbitChanged.getValue() == this.realSteveVarbitValue) {
            this.varbitReplaced = false;
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        // NPC Contact Spell Window
        if (event.getGroupId() == 75) {
            Widget steveHead = client.getWidget(75, 32);
            Widget steveLabel = client.getWidget(75, 33);

            clientThread.invokeLater(() -> {
                steveLabel.setText("Nieve");
                steveHead.setModelId(16282);
            });
        }

        // When talking to Steve
        if (event.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID) {
            Widget npcChatName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
            clientThread.invokeLater(() -> npcChatName.setText("Nieve"));
        }

        // Nieve's Grave
        if (event.getGroupId() == 221) {
            Widget nieveGraveHeaderTitle = client.getWidget(221, 2);
            nieveGraveHeaderTitle.setText("Steve");
            Widget nieveGraveHeaderOne = client.getWidget(221, 4);
            nieveGraveHeaderOne.setText("Once, this place was a monument to Nieve");
            Widget nieveGraveHeaderTwo = client.getWidget(221, 5);
            nieveGraveHeaderTwo.setText("Now, this is where we remember Steve");
            Widget nieveGraveHeaderThree = client.getWidget(221, 4);
            nieveGraveHeaderThree.setText("Once, this place was a monument to Nieve");
            Widget nieveGraveHonour = client.getWidget(221, 12);
            nieveGraveHonour.setText(nieveGraveHonour.getText().replace("Nieve's honour", "Steve's absence"));
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded) {
        // Hover entries (Spellbook, NPC contact item etc.)
        if (menuEntryAdded.getOption().equals("Steve")) {
            menuEntryAdded.getMenuEntry().setOption("Nieve");
        }
    }

    @Subscribe
    protected void onChatMessage(ChatMessage event) {
        // When doing "Examine" on Nieve's grave.
        if (event.getMessage().equals("In memory of Nieve, she looks rich and dead.")) {
            final MessageNode messageNode = event.getMessageNode();
            messageNode.setRuneLiteFormatMessage("In memory of Steve, he's in a different place now.");

            client.refreshChat();
        }
    }
}
