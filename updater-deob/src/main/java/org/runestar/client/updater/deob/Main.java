package org.runestar.client.updater.deob;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.runestar.client.updater.deob.rs.DeobToDeob;

import java.nio.file.Paths;

public class Main {

    public static final HashMap<String, List<Number>> dumbShitMap = new HashMap<>();

    public static void main(String[] args)
    {
        Transformer.Companion.getDEFAULT().transform(Paths.get("./gamepack.jar"),Paths.get("./deob.jar"));
        dumbShitMap.forEach((key, value) -> {
            if (value.size() != 2)
            {
                return;
            }

            boolean same = value.get(0).equals(value.get(1));
            System.err.println(key + ": " + value.get(0) + " " + value.get(1) + " " + (same ? "same" : "different"));
        });
    }
}
