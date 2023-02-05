package com.nievive;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
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

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            this.resetVarbit();
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        if (this.realSteveVarbitValue == -1) {
            this.realSteveVarbitValue = client.getVarbitValue(STEVE_VARBIT_ID);
        }

        if (this.realSteveVarbitValue == 1 && !this.varbitReplaced) { // Only replace if Steve's around.
            this.varbitReplaced = true;
            clientThread.invokeLater(() -> client.setVarbit(STEVE_VARBIT_ID, 0));
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarbitId() != STEVE_VARBIT_ID) {
            return;
        }

        if (!this.varbitReplaced) {
            this.realSteveVarbitValue = varbitChanged.getValue();
            return;
        }

        if (varbitChanged.getValue() == this.realSteveVarbitValue) {
            this.varbitReplaced = false;
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (!this.varbitReplaced) {
            // Attempt to "reset" Steve's character model when talking to him directly.
            // Does not always work, as some states of Steve/Nieve seem to be based off of an inaccessible variable.
            Widget npcChatHead = client.getWidget(WidgetInfo.DIALOG_NPC_HEAD_MODEL);
            if (npcChatHead != null && npcChatHead.getModelId() == 6797) {
                npcChatHead.setModelId(6798);
            }
            return;
        }

        // NPC Contact Spell Window
        if (event.getGroupId() == 75) {
            Widget steveHead = client.getWidget(75, 32);
            Widget steveLabel = client.getWidget(75, 33);

            clientThread.invokeLater(() -> {
                if (steveHead == null || steveLabel == null) {
                    return;
                }

                steveLabel.setText("Nieve");
                steveHead.setModelId(16282);
            });
        }

        // When talking to Steve
        if (event.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID) {
            Widget npcChatName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
            Widget npcChatHead = client.getWidget(WidgetInfo.DIALOG_NPC_HEAD_MODEL);

            clientThread.invokeLater(() -> {
                if (npcChatHead == null || npcChatName == null) {
                    return;
                }

                if (npcChatName.getText().equals("Steve")) {
                    npcChatName.setText("Nieve");
                }

                if (npcChatHead.getModelId() == 6798) {
                    npcChatHead.setModelId(6797);
                }
            });
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
        if (!this.varbitReplaced) {
            return;
        }

        // Hover entries (Spellbook, NPC contact item etc.)
        if (menuEntryAdded.getOption().equals("Steve")) {
            menuEntryAdded.getMenuEntry().setOption("Nieve");
        }
    }

    @Subscribe
    protected void onChatMessage(ChatMessage event) {
        if (!this.varbitReplaced) {
            return;
        }

        // When doing "Examine" on Nieve's grave.
        if (event.getMessage().equals("In memory of Nieve, she looks rich and dead.")) {
            final MessageNode messageNode = event.getMessageNode();
            messageNode.setRuneLiteFormatMessage("In memory of Steve, he's in a different place now.");

            client.refreshChat();
        }

        // When interacting with a Slayer monster off-task within the Slayer Cave.
        if (event.getMessage().equals("Steve wants you to stick to your Slayer assignments.")) {
            final MessageNode messageNode = event.getMessageNode();
            messageNode.setRuneLiteFormatMessage("Nieve wants you to stick to your Slayer assignments.");

            client.refreshChat();
        }
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
}
