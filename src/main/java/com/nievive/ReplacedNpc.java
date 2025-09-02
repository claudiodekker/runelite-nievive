package com.nievive;

import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;

public abstract class ReplacedNpc
{
	public final int varbitId;
	protected final int intendedValue;

	protected int realValue = -1;
	protected boolean replaced = false;

	public ReplacedNpc(int varbitId, int intendedValue)
	{
		this.varbitId = varbitId;
		this.intendedValue = intendedValue;
	}

	public void apply(Client client, ClientThread clientThread)
	{
		if (this.realValue == -1)
		{
			this.realValue = client.getVarbitValue(varbitId);
		}

		if (this.realValue != this.intendedValue && !this.replaced)
		{
			this.replaced = true;
			clientThread.invokeLater(() -> client.setVarbit(this.varbitId, this.intendedValue));
		}
	}

	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getVarbitId() != this.varbitId)
		{
			return;
		}

		// When a quest updates the state, we'll want to know about it, so we can restore it later.
		if (!this.replaced)
		{
			this.realValue = varbitChanged.getValue();
			return;
		}

		// If the server re-transmits the varbit (e.g. after a disconnect), we'll want to mark the
		// npc as no longer being replaced. This'll make it so we can re-patch it next game tick.
		if (varbitChanged.getValue() == this.realValue)
		{
			this.replaced = false;
		}
	}

	public void reset(Client client, ClientThread clientThread)
	{
		if (!this.replaced)
		{
			return;
		}

		clientThread.invokeLater(() -> {
			client.setVarbit(this.varbitId, this.realValue);
			this.realValue = -1;
			this.replaced = false;
		});
	}
}
