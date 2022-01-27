package xyz.apex.forge.fantasytable.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;

import xyz.apex.forge.fantasytable.FantasyTable;
import xyz.apex.forge.fantasytable.init.DiceType;
import xyz.apex.forge.fantasytable.item.DiceItem;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class DiceHelper
{
	public static int roll(Random rng, int min, int max, boolean loaded)
	{
		if(max < min)
		{
			int tmp = max;
			max = min;
			min = tmp;
		}

		min = Math.max(min, 0);
		max = Math.max(max, 1);

		if(loaded)
			min = Math.max(1, max / 2);

		int roll = rng.nextInt(max) + 1;

		if(roll < min)
		{
			while(roll < min)
			{
				roll = rng.nextInt(max) + 1;
			}
		}

		return roll;
	}

	public static boolean throwDice(World level, PlayerEntity player, Hand hand, ItemStack stack, int min)
	{
		if(stack.isEmpty())
			return false;
		if(level.isClientSide)
			return true;

		DiceItem die = (DiceItem) stack.getItem();
		int sides = die.getSides();
		boolean loaded = false;
		int[] rolls = IntStream.range(0, stack.getCount()).map(i -> roll(level.random, min, sides, loaded)).toArray();
		rolls = die.getDiceType().onRoll(player, hand, stack, min, rolls);
		int roll = Arrays.stream(rolls).sum();
		IFormattableTextComponent textComponent = createTextComponent(player, stack, die, roll, sides);
		sendMessageToPlayers(player, textComponent);

		return true;
	}

	private static IFormattableTextComponent createTextComponent(PlayerEntity player, ItemStack stack, DiceItem die, int roll, int sides)
	{
		String strAmount = "";
		int count = stack.getCount();
		DiceType<?, ?> diceType = die.getDiceType();

		if(count > 1)
			strAmount += count;

		return new TranslationTextComponent(
				FantasyTable.DIE_ROLL_KEY,
				player.getDisplayName(),
				new TranslationTextComponent(FantasyTable.DIE_ROLL_RESULT_KEY, roll, strAmount, sides).withStyle(style -> diceType.withStyle(stack, style))
		).withStyle(style -> style
				.withHoverEvent(
						new HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								stack.getHoverName().plainCopy().withStyle(hoverStyle -> diceType.withStyle(stack, hoverStyle))
						)
				)
		);
	}

	public static void sendMessageToPlayers(PlayerEntity player, ITextComponent component)
	{
		MinecraftServer server = player.getServer();
		UUID playerID = player.getGameProfile().getId();

		if(server == null)
		{
			player.sendMessage(component, playerID);
			return;
		}

		int rollRange = 16; // TODO: Config

		for(PlayerEntity plr : server.getPlayerList().getPlayers())
		{
			if(!plr.level.dimensionType().equalTo(plr.level.dimensionType()))
				continue;
			if(plr.distanceTo(player) > rollRange)
				continue;

			plr.sendMessage(component, playerID);
		}
	}
}
