package com.nievive.npcs;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Animation;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.ModelData;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuEntryAdded;

public class Duradel
{
	private RuneLiteObject transmog;
	private NPC targetNpc;
	private LocalPoint lastLocation;
	private int lastAnimation = -1;
	private int lastOrientation = -1;

	private void init(Client client)
	{
		int duradelNpcId = 13622;
		NPCComposition npcDefinition = client.getNpcDefinition(duradelNpcId);

		List<Integer> modelIds = new ArrayList<>();
		for (int model : npcDefinition.getModels())
		{
			modelIds.add(model);
		}

		ModelData[] modelDataArray = modelIds.stream().map(client::loadModelData).toArray(ModelData[]::new);
		ModelData model = client.mergeModels(modelDataArray);

		transmog = client.createRuneLiteObject();
		transmog.setModel(model.light());
		transmog.setActive(true);
	}

	public void syncToNpc(NPC actor, Client client)
	{
		if (actor == null)
		{
			return;
		}

		this.targetNpc = actor;

		if (transmog == null)
		{
			init(client);
		}

		// Sync location and orientation every client tick for smooth movement
		LocalPoint loc = actor.getLocalLocation();
		boolean moving = lastLocation != null && !lastLocation.equals(loc);
		transmog.setLocation(loc, client.getPlane());
		lastLocation = loc;

		int orientation = actor.getOrientation();
		if (orientation != lastOrientation)
		{
			transmog.setOrientation(orientation);
			lastOrientation = orientation;
		}

		// Keep animation in sync with NPC, including default pose/walk when no explicit animation is set
		int newAnimId = pickAnimationId(actor, moving);
		if (newAnimId != lastAnimation)
		{
			Animation animation = newAnimId >= 0 ? client.loadAnimation(newAnimId) : null;
			if (animation != null)
			{
				transmog.setAnimation(animation);
			}
			lastAnimation = newAnimId;
		}

		// Ensure the object stays active
		if (!transmog.isActive())
		{
			transmog.setActive(true);
		}
	}

	private int pickAnimationId(NPC actor, boolean moving)
	{
		// Prefer explicit animation when set
		int anim = actor.getAnimation();
		if (anim != -1)
		{
			return anim;
		}
		int walk = actor.getWalkAnimation();
		int pose = actor.getPoseAnimation();
		int idle = actor.getIdlePoseAnimation();

		// Determine movement either by location change or by pose equaling walk
		boolean inferredMoving = moving || (walk != -1 && pose == walk);
		if (inferredMoving && walk != -1)
		{
			return walk;
		}
		if (idle != -1)
		{
			return idle;
		}
		if (pose != -1)
		{
			return pose;
		}
		return walk;
	}

	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (targetNpc == null)
		{
			return;
		}

		// Only modify entries that actually point at our target NPC (or contain its name)
		boolean isOurNpc = event.getIdentifier() == targetNpc.getIndex();
		String targetText = event.getTarget();
		MenuEntry entry = event.getMenuEntry();

		if (isOurNpc || (targetText != null && targetText.contains("Kuradal")))
		{
			// Replace occurrences of Kuradal with Duradel while preserving any color tags in target
			if (targetText != null && targetText.contains("Kuradal"))
			{
				entry.setTarget(targetText.replace("Kuradal", "Duradel"));
			}
			// Some client builds also include the name in option for certain menu types; handle just in case
			String option = entry.getOption();
			if (option != null && option.contains("Kuradal"))
			{
				entry.setOption(option.replace("Kuradal", "Duradel"));
			}
			// Ensure the entry continues to target the hidden NPC (forwarding)
			entry.setIdentifier(targetNpc.getIndex());
		}
	}

	public void cleanup()
	{
		if (transmog != null)
		{
			try
			{
				transmog.setActive(false);
			}
			catch (Exception ignored)
			{
			}
			transmog = null;
		}

		targetNpc = null;
		lastLocation = null;
		lastAnimation = -1;
		lastOrientation = -1;
	}
}
