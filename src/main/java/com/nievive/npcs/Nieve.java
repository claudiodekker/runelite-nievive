package com.nievive.npcs;

import com.nievive.ReplacedNpc;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;

public class Nieve extends ReplacedNpc
{
	public Nieve()
	{
		super(VarbitID.MM2_SLAYER_MASTER, 0);
	}

	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!this.replaced)
		{
			return;
		}

		// Hover entries (Spellbook, NPC contact item etc.)
		if (event.getOption().equals("Steve"))
		{
			event.getMenuEntry().setOption("Nieve");
		}
	}

	public void onWidgetLoaded(Client client, ClientThread clientThread, WidgetLoaded event)
	{
		if (!this.replaced)
		{
			// Attempt to "reset" Steve's character model when talking to him directly.
			// Does not always work, as some states of Steve/Nieve seem to be based off of an inaccessible variable.
			Widget npcChatHead = client.getWidget(InterfaceID.ChatLeft.HEAD);
			if (npcChatHead != null && npcChatHead.getModelId() == 6797)
			{
				npcChatHead.setModelId(6798);
			}
			return;
		}

		attemptToReplaceNpcContactEntry(client, clientThread, event);
		attemptToReplaceChatHead(client, clientThread, event);
		attemptToReplaceGraveText(client, event);
	}

	public void onChatMessage(Client client, ChatMessage event) {
		if (! this.replaced) {
			return;
		}

		// When doing "Examine" on Nieve's grave.
		if (event.getMessage().equals("In memory of Nieve, she looks rich and dead."))
		{
			final MessageNode messageNode = event.getMessageNode();
			messageNode.setRuneLiteFormatMessage("In memory of Steve, he's in a different place now.");

			client.refreshChat();
		}

		// When interacting with a Slayer monster off-task within the Slayer Cave.
		if (event.getMessage().equals("Steve wants you to stick to your Slayer assignments."))
		{
			final MessageNode messageNode = event.getMessageNode();
			messageNode.setRuneLiteFormatMessage("Nieve wants you to stick to your Slayer assignments.");

			client.refreshChat();
		}
	}

	private static void attemptToReplaceNpcContactEntry(Client client, ClientThread clientThread, WidgetLoaded event)
	{
		if (event.getGroupId() == 75)
		{
			Widget steveHead = client.getWidget(75, 32);
			Widget steveLabel = client.getWidget(75, 33);

			clientThread.invokeLater(() -> {
				if (steveHead == null || steveLabel == null)
				{
					return;
				}

				steveLabel.setText("Nieve");
				steveHead.setModelId(16282);
			});
		}
	}

	private static void attemptToReplaceChatHead(Client client, ClientThread clientThread, WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.CHAT_LEFT)
		{
			Widget npcChatName = client.getWidget(InterfaceID.ChatLeft.NAME);
			Widget npcChatHead = client.getWidget(InterfaceID.ChatLeft.HEAD);

			clientThread.invokeLater(() -> {
				if (npcChatHead == null || npcChatName == null)
				{
					return;
				}

				if (npcChatName.getText().equals("Steve"))
				{
					npcChatName.setText("Nieve");
				}

				if (npcChatHead.getModelId() == 6798)
				{
					npcChatHead.setModelId(6797);
				}
			});
		}
	}

	private static void attemptToReplaceGraveText(Client client, WidgetLoaded event)
	{
		// Nieve's Grave
		if (event.getGroupId() == 221)
		{
			Widget nieveGraveHeaderTitle = client.getWidget(221, 2);
			Widget nieveGraveHeaderOne = client.getWidget(221, 4);
			Widget nieveGraveHeaderTwo = client.getWidget(221, 5);
			Widget nieveGraveHeaderThree = client.getWidget(221, 4);
			Widget nieveGraveHonour = client.getWidget(221, 12);

			if (nieveGraveHeaderTitle == null || nieveGraveHeaderOne == null || nieveGraveHeaderTwo == null || nieveGraveHeaderThree == null || nieveGraveHonour == null)
			{
				return;
			}

			nieveGraveHeaderTitle.setText("Steve");
			nieveGraveHeaderOne.setText("Once, this place was a monument to Nieve");
			nieveGraveHeaderTwo.setText("Now, this is where we remember Steve");
			nieveGraveHeaderThree.setText("Once, this place was a monument to Nieve");
			nieveGraveHonour.setText(nieveGraveHonour.getText().replace("Nieve's honour", "Steve's absence"));
		}
	}
}
