package cn.dancingsnow.appeu.registry;

import static gregtech.api.enums.GTValues.VN;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import cn.dancingsnow.appeu.hatch.HatchDirection;
import cn.dancingsnow.appeu.hatch.HatchFamily;
import cn.dancingsnow.appeu.hatch.HatchSpec;
import cn.dancingsnow.appeu.hatch.HatchSpecs;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamo;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamoMulti;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamoTunnel;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergy;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergyMulti;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergyTunnel;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;

public final class HatchRegistration {

    private static final Map<String, ItemStack> REGISTERED_HATCHES = new LinkedHashMap<>();
    private static final Map<Integer, String> REGISTERED_IDS = new LinkedHashMap<>();
    private static final Set<String> DEFAULT_HATCH_NAMES = new LinkedHashSet<>();

    private static Integer defaultStart;

    private HatchRegistration() {}

    public static synchronized Map<String, ItemStack> registerAll(int start) {
        if (defaultStart != null) {
            if (defaultStart == start) {
                return copyStacks(DEFAULT_HATCH_NAMES);
            }
            throw new IllegalStateException(
                "Default hatch matrix is already registered at id " + defaultStart
                    + "; cannot register HatchSpecs at id "
                    + start);
        }

        List<HatchSpec> specs = createDefaultSpecs(start);
        validateAll(specs);
        registerValidated(specs);
        defaultStart = start;
        for (HatchSpec spec : specs) {
            DEFAULT_HATCH_NAMES.add(spec.name());
        }
        return copyStacks(DEFAULT_HATCH_NAMES);
    }

    public static synchronized Map<String, ItemStack> registerLaserSeries(int startId, int amperage) {
        List<HatchSpec> specs = createLaserSpecs(startId, amperage);
        validateAll(specs);
        registerValidated(specs);
        return copyStacks(namesOf(specs));
    }

    public static synchronized Map<String, ItemStack> getDefaultHatches() {
        return copyStacks(DEFAULT_HATCH_NAMES);
    }

    public static synchronized Map<String, ItemStack> getRegisteredHatches() {
        return copyStacks(REGISTERED_HATCHES.keySet());
    }

    private static List<HatchSpec> createDefaultSpecs(int start) {
        if (start <= 0) {
            throw invalidRequest(start, "HatchSpecs.create", "start id must be positive", null);
        }
        try {
            return HatchSpecs.create(start);
        } catch (RuntimeException exception) {
            throw invalidRequest(start, "HatchSpecs.create", "could not create the default hatch matrix", exception);
        }
    }

    private static List<HatchSpec> createLaserSpecs(int startId, int amperage) {
        if (startId <= 0) {
            throw invalidRequest(startId, "HatchSpecs.createLaserSeries", "start id must be positive", null);
        }
        try {
            return HatchSpecs.createLaserSeries(startId, amperage);
        } catch (RuntimeException exception) {
            throw invalidRequest(
                startId,
                "HatchSpecs.createLaserSeries(" + amperage + "A)",
                "could not create the laser hatch series",
                exception);
        }
    }

    private static void validateAll(List<HatchSpec> specs) {
        if (specs.isEmpty()) {
            throw invalidRequest(-1, "<empty HatchSpecs>", "registration contains no hatches", null);
        }

        HatchSpec first = specs.get(0);
        long end = (long) first.id() + specs.size() - 1L;
        if (end > Integer.MAX_VALUE) {
            throw invalidSpec(first, "ending id overflows int: " + end, null);
        }

        Set<Integer> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (int index = 0; index < specs.size(); index++) {
            HatchSpec spec = specs.get(index);
            long expectedId = (long) first.id() + index;
            if (spec.id() != expectedId) {
                throw invalidSpec(spec, "non-contiguous id at index " + index + ", expected " + expectedId, null);
            }
            validateSpec(spec, ids, names);
        }
        if (!GregTechAPI.sPreloadStarted || GregTechAPI.sPostloadStarted) {
            throw invalidSpec(first, "GregTech is not in its MetaTileEntity load phase", null);
        }
    }

    private static void validateSpec(HatchSpec spec, Set<Integer> ids, Set<String> names) {
        int id = spec.id();
        String name = spec.name();
        if (id < 0 || id >= GregTechAPI.MAXIMUM_METATILE_IDS) {
            throw invalidSpec(spec, "id must be in [0, " + GregTechAPI.MAXIMUM_METATILE_IDS + ")", null);
        }
        if (id >= GregTechAPI.METATILEENTITIES.length) {
            throw invalidSpec(spec, "id exceeds METATILEENTITIES length " + GregTechAPI.METATILEENTITIES.length, null);
        }
        if (!ids.add(id)) {
            throw invalidSpec(spec, "duplicate id in registration request", null);
        }
        if (!names.add(name)) {
            throw invalidSpec(spec, "duplicate name in registration request", null);
        }
        if (REGISTERED_IDS.containsKey(id)) {
            throw invalidSpec(spec, "id is already owned by " + REGISTERED_IDS.get(id), null);
        }
        if (REGISTERED_HATCHES.containsKey(name)) {
            throw invalidSpec(spec, "name is already registered by this registrar", null);
        }
        if (GregTechAPI.METATILEENTITIES[id] != null) {
            throw invalidSpec(
                spec,
                "global slot is occupied by " + GregTechAPI.METATILEENTITIES[id].getMetaName(),
                null);
        }
    }

    private static void registerValidated(List<HatchSpec> specs) {
        for (HatchSpec spec : specs) {
            try {
                MetaTileEntity hatch = construct(spec);
                if (GregTechAPI.METATILEENTITIES[spec.id()] != hatch) {
                    throw new IllegalStateException("constructor did not retain ownership of its global MTE slot");
                }
                ItemStack stack = hatch.getStackForm(1);
                if (stack == null) {
                    throw new IllegalStateException("getStackForm(1) returned null");
                }
                ModItems.setHatch(spec, stack);
                REGISTERED_IDS.put(spec.id(), spec.name());
                REGISTERED_HATCHES.put(spec.name(), stack.copy());
            } catch (RuntimeException | LinkageError exception) {
                int occupiedInRequest = countOccupiedSlots(specs);
                throw invalidSpec(
                    spec,
                    "construction failed with " + occupiedInRequest
                        + " occupied slots in this request; occupied slots were not rolled back",
                    exception);
            }
        }
    }

    private static int countOccupiedSlots(List<HatchSpec> specs) {
        int occupied = 0;
        for (HatchSpec spec : specs) {
            if (spec.id() >= 0 && spec.id() < GregTechAPI.METATILEENTITIES.length
                && GregTechAPI.METATILEENTITIES[spec.id()] != null) {
                occupied++;
            }
        }
        return occupied;
    }

    private static MetaTileEntity construct(HatchSpec spec) {
        String regionalName = regionalName(spec);
        HatchDirection direction = spec.direction();
        HatchFamily family = spec.family();

        if (family == HatchFamily.STANDARD) {
            if (direction == HatchDirection.ENERGY) {
                return new MTEHatchMEEnergy(spec.id(), spec.name(), regionalName, spec.tier());
            }
            return new MTEHatchMEDynamo(spec.id(), spec.name(), regionalName, spec.tier(), spec.amperage());
        }
        if (family == HatchFamily.MULTI_AMP) {
            if (direction == HatchDirection.ENERGY) {
                return new MTEHatchMEEnergyMulti(spec.id(), spec.name(), regionalName, spec.tier(), spec.amperage());
            }
            return new MTEHatchMEDynamoMulti(spec.id(), spec.name(), regionalName, spec.tier(), spec.amperage());
        }
        if (family == HatchFamily.LASER) {
            if (direction == HatchDirection.ENERGY) {
                return new MTEHatchMEEnergyTunnel(spec.id(), spec.name(), regionalName, spec.tier(), spec.amperage());
            }
            return new MTEHatchMEDynamoTunnel(spec.id(), spec.name(), regionalName, spec.tier(), spec.amperage());
        }
        throw invalidSpec(spec, "unsupported hatch family " + family, null);
    }

    private static String regionalName(HatchSpec spec) {
        String tierName = VN[spec.tier()];
        if (spec.family() == HatchFamily.LASER) {
            return spec.amperage() + "A "
                + tierName
                + " ME Laser "
                + (spec.direction() == HatchDirection.ENERGY ? "Target" : "Source")
                + " Hatch";
        }
        StringBuilder name = new StringBuilder();
        if (spec.amperage() != 2) {
            name.append(spec.amperage())
                .append("A ");
        }
        name.append(tierName)
            .append(" ME ")
            .append(spec.direction() == HatchDirection.ENERGY ? "Energy" : "Dynamo");
        return name.append(" Hatch")
            .toString();
    }

    private static Set<String> namesOf(List<HatchSpec> specs) {
        Set<String> names = new LinkedHashSet<>();
        for (HatchSpec spec : specs) {
            names.add(spec.name());
        }
        return names;
    }

    private static Map<String, ItemStack> copyStacks(Iterable<String> names) {
        Map<String, ItemStack> copy = new LinkedHashMap<>();
        for (String name : names) {
            ItemStack stack = REGISTERED_HATCHES.get(name);
            if (stack != null) {
                copy.put(name, stack.copy());
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static IllegalStateException invalidSpec(HatchSpec spec, String reason, Throwable cause) {
        String message = "Cannot register " + spec + ": " + reason;
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }

    private static IllegalStateException invalidRequest(int id, String name, String reason, Throwable cause) {
        String message = "Cannot register hatch id " + id + " (" + name + "): " + reason;
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
