package com.nievive;

import com.nievive.npcs.Duradel;
import com.nievive.npcs.Nieve;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
	name = "Nievive"
)
public class NievivePlugin extends Plugin
{
	@Inject
	protected Client client;

	@Inject
	protected ClientThread clientThread;

	protected Nieve nieve = new Nieve();
	protected Duradel duradel = new Duradel();

	protected NPC kuradal;

	@Override
	protected void shutDown()
	{
		duradel.cleanup();
		kuradal = null;
		this.resetVarbits();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			this.resetVarbits();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		nieve.apply(client, clientThread);
	}

	@Subscribe
	public void onClientTick(ClientTick tick)
	{
		if (kuradal != null)
		{
			duradel.syncToNpc(kuradal, client);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		nieve.onVarbitChanged(varbitChanged);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		nieve.onWidgetLoaded(client, clientThread, event);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		nieve.onMenuEntryAdded(event);
		duradel.onMenuEntryAdded(event);
	}

	@Subscribe
	protected void onChatMessage(ChatMessage event)
	{
		nieve.onChatMessage(client, event);
	}

	protected void resetVarbits()
	{
		nieve.reset(client, clientThread);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC actor = npcSpawned.getNpc();

		if (actor.getId() != NpcID.SLAYER_MASTER_5_KURADAL)
		{
			return;
		}

		kuradal = actor;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC actor = npcDespawned.getNpc();
		if (kuradal != null && actor == kuradal)
		{
			duradel.cleanup();
			kuradal = null;
		}
	}
}
