package me.shirkneko.drgflares;

import me.shirkneko.drgflares.util.ServerSyncMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loader-agnostic anchor for the mod: shared constants, the logger,
 * and runtime state that is not tied to any mod loader.
 */
public final class DRGFlaresCommon
{
    public static final String MOD_ID = "drg_flares";
    public static final Logger LOGGER = LogManager.getLogger("DRGFlares");

    /**
     * How the client and the server agree on flare behavior.
     * Mutable at runtime (set by the loader-specific event handlers).
     */
    public static volatile ServerSyncMode serverSyncMode = ServerSyncMode.UNDEFINED;

    private DRGFlaresCommon()
    {
    }
}
