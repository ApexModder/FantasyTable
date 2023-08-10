package xyz.apex.minecraft.fantasydice.common;

import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.registry.Registrar;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;

@ApiStatus.NonExtendable
public interface FantasyDice
{
    Logger LOGGER = LogManager.getLogger();
    String ID = "fantasydice";

    FantasyDice INSTANCE = Services.singleton(FantasyDice.class);
    Registrar REGISTRAR = Registrar.create(ID);


    default void bootstrap()
    {
        REGISTRAR.register();
        registerGenerators();
    }

    private void registerGenerators()
    {
        var descriptionKey = "pack.%s.description".formatted(ID);

        ProviderTypes.LANGUAGES.addListener(ID, (provider, lookup) -> provider
                .enUS()
                    .add(descriptionKey, "Fantasy's Dice")
                .end()
        );

        ProviderTypes.registerDefaultMcMetaGenerator(ID, Component.translatable(descriptionKey));
    }
}
