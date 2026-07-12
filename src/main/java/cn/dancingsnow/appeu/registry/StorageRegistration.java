package cn.dancingsnow.appeu.registry;

import appeng.api.AEApi;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStackType;
import cn.dancingsnow.appeu.storage.EUConstants;
import cn.dancingsnow.appeu.storage.EUStackType;
import cn.dancingsnow.appeu.storage.cell.EUCellHandler;

public final class StorageRegistration {

    private StorageRegistration() {}

    public static void registerStackType() {
        IAEStackType<?> registeredType = AEStackTypeRegistry.getType(EUConstants.STACK_TYPE_ID);
        if (registeredType == null) {
            AEStackTypeRegistry.register(EUStackType.INSTANCE);
        } else if (registeredType != EUStackType.INSTANCE) {
            throw new IllegalStateException(
                "AE stack type ID is already registered by a different instance: " + EUConstants.STACK_TYPE_ID);
        }
    }

    public static void registerCellHandler() {
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(EUCellHandler.INSTANCE);
    }
}
