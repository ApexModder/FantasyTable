package xyz.apex.forge.dicemod;

import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.TagsProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;
import xyz.apex.forge.dicemod.data.ItemModelGenerator;
import xyz.apex.forge.dicemod.data.LanguageGenerator;
import xyz.apex.forge.dicemod.item.DiceItem;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Dice
{
	BONE("bone", TextFormatting.WHITE, Tags.Items.BONES),
	IRON("iron", TextFormatting.GRAY, Tags.Items.NUGGETS_IRON),
	GOLD("gold", TextFormatting.YELLOW, Tags.Items.NUGGETS_GOLD),
	DIAMOND("diamond", TextFormatting.AQUA, Tags.Items.GEMS_DIAMOND),
	;

	public static final Dice[] TYPES = values();

	public final Tags.IOptionalNamedTag<Item> tag;
	public final ITag<Item> craftingItem;
	public final RegistryObject<Item> six_sided_die;
	public final RegistryObject<Item> twenty_sided_die;
	public final String typeName;
	public final String typeLangKey;
	public final String typeLangUsingKey;
	public final TextFormatting typeColor;
	public final Rarity rarity;

	Dice(String typeName, TextFormatting typeColor, ITag<Item> craftingItem)
	{
		this.typeName = typeName;
		this.typeColor = typeColor;
		this.craftingItem = craftingItem;
		this.rarity = Rarity.create(DiceMod.ID + ":" + typeName, typeColor);

		typeLangKey = DiceMod.ID + ".dice." + typeName;
		typeLangUsingKey = typeLangKey + ".using";

		tag = ItemTags.createOptional(new ResourceLocation(DiceMod.ID, "dice/" + typeName));

		six_sided_die = register(this, "six_sided_die");
		twenty_sided_die = register(this, "twenty_sided_die");
	}

	public void onGenerateLanguage(BiConsumer<Supplier<Item>, String> addItem, BiConsumer<String, String> addGen)
	{
		String typeNameEnglish = typeName.substring(0, 1).toUpperCase(Locale.ROOT) + typeName.substring(1).toLowerCase();

		addGen.accept(typeLangKey, typeNameEnglish);
		addGen.accept(typeLangUsingKey, "Using a " + typeNameEnglish + " Die");

		addItem.accept(six_sided_die, typeNameEnglish + " d6");
		addItem.accept(twenty_sided_die, typeNameEnglish + " d20");
	}

	public void onGenerateTags(Function<ITag.INamedTag<Item>, TagsProvider.Builder<Item>> builder, TagsProvider.Builder<Item> sixSidedBuilder, TagsProvider.Builder<Item> twentySidedBuilder)
	{
		builder.apply(tag).add(
				six_sided_die.get(),
				twenty_sided_die.get()
		);

		sixSidedBuilder.add(six_sided_die.get());
		twentySidedBuilder.add(twenty_sided_die.get());
	}

	public void onGenerateItemModel(Function<Supplier<Item>, ItemModelBuilder> builder)
	{
		// 6
		builder.apply(six_sided_die)
		       .parent(ItemModelGenerator.GENERATED)
		       .texture("layer0", new ResourceLocation(DiceMod.ID, "item/" + typeName + "/six_sided_die"));

		// 20
		builder.apply(twenty_sided_die)
		       .parent(ItemModelGenerator.GENERATED)
		       .texture("layer0", new ResourceLocation(DiceMod.ID, "item/" + typeName + "/twenty_sided_die"));
	}

	public void onGenerateRecipes(Consumer<IFinishedRecipe> consumer, Function<ITag<Item>, InventoryChangeTrigger.Instance> hasTag, Function<IItemProvider, InventoryChangeTrigger.Instance> hasItem)
	{
		// 6
		ShapedRecipeBuilder.shaped(six_sided_die::get, 8)
		                   .define('I', craftingItem)
		                   .pattern("II ")
		                   .pattern("II ")
		                   .pattern("   ")
		                   .group("dice/" + typeName)
		                   .unlockedBy("has_item", hasTag.apply(craftingItem))
		                   .save(consumer, new ResourceLocation(DiceMod.ID, typeName + "/six_sided_die"));

		// 20
		ShapedRecipeBuilder.shaped(twenty_sided_die::get, 8)
		                   .define('I', craftingItem)
		                   .pattern(" I ")
		                   .pattern("III")
		                   .pattern(" I ")
		                   .group("dice/" + typeName)
		                   .unlockedBy("has_item", hasTag.apply(craftingItem))
		                   .save(consumer, new ResourceLocation(DiceMod.ID, typeName + "/twenty_sided_die"));
	}

	public Style getTextComponentStyle(Style style)
	{
		return style.withItalic(true).withColor(typeColor);
	}

	public IFormattableTextComponent createTextComponent(PlayerEntity thrower, ItemStack die, int roll, int min, int max)
	{
		return new TranslationTextComponent(
				LanguageGenerator.DICE_ROLL_KEY,
				thrower.getDisplayName(),
				new StringTextComponent("" + roll).withStyle(this::getTextComponentStyle),
				die.getDisplayName().plainCopy().withStyle(this::getTextComponentStyle)
		)
				.withStyle(style ->
						style.withHoverEvent(
								new HoverEvent(
										HoverEvent.Action.SHOW_TEXT,
										new TranslationTextComponent(typeLangUsingKey).withStyle(hoverStyle -> hoverStyle.withColor(typeColor))
								)
						)
				);
	}

	public static Dice byItem(ItemStack stack)
	{
		return Arrays.stream(Dice.TYPES).filter(dice -> stack.getItem().is(dice.tag)).findFirst().orElse(BONE);
	}

	static void register() { }

	private static RegistryObject<Item> register(Dice dice, String itemName)
	{
		return DiceMod.ITEMS.register(dice.typeName + "_" + itemName, () -> new DiceItem(new Item.Properties().tab(DiceMod.ITEM_GROUP).stacksTo(8).rarity(dice.rarity)));
	}
}
