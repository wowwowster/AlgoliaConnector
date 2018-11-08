package com.sword.gsa.spis.scs.extracting.language.translate;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sword.gsa.spis.scs.extracting.config.SCSServiceLoader;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.translate.Translator;

public class SCSDefaultTranslator implements Translator {
    private transient final SCSServiceLoader loader;

    public SCSDefaultTranslator(SCSServiceLoader loader) {
        this.loader = loader;
    }
    public SCSDefaultTranslator() {
        this(new SCSServiceLoader());
    }

    /**
     * Finds all statically loadable translators and sort the list by name,
     * rather than discovery order.
     *
     * @param loader service loader
     * @return ordered list of statically loadable translators
     */
    private static List<Translator> getDefaultTranslators(SCSServiceLoader loader) {
        List<Translator> translators = loader.loadStaticServiceProviders(Translator.class);
        Collections.sort(translators, new Comparator<Translator>() {
            public int compare(Translator t1, Translator t2) {
                String n1 = t1.getClass().getName();
                String n2 = t2.getClass().getName();
                boolean tika1 = n1.startsWith("org.apache.extracting.");
                boolean tika2 = n2.startsWith("org.apache.extracting.");
                if (tika1 == tika2) {
                    return n1.compareTo(n2);
                } else if (tika1) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return translators;
    }
    /**
     * Returns the first available translator, or null if none are
     */
    private static Translator getFirstAvailable(SCSServiceLoader loader) {
        for (Translator t : getDefaultTranslators(loader)) {
            if (t.isAvailable()) return t;
        }
        return null;
    }

    /**
     * Translate, using the first available service-loaded translator
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) throws TikaException, IOException {
        Translator t = getFirstAvailable(loader);
        if (t != null) {
            return t.translate(text, sourceLanguage, targetLanguage);
        }
        throw new TikaException("No translators currently available");
    }

    /**
     * Translate, using the first available service-loaded translator
     */
    public String translate(String text, String targetLanguage) throws TikaException, IOException {
        Translator t = getFirstAvailable(loader);
        if (t != null) {
            return t.translate(text, targetLanguage);
        }
        throw new TikaException("No translators currently available");
    }

    /**
     * Returns all available translators
     */
    public List<Translator> getTranslators() {
        return getDefaultTranslators(loader);
    }
    /**
     * Returns the current translator
     */
    public Translator getTranslator() {
        return getFirstAvailable(loader);
    }

    public boolean isAvailable() {
        return getFirstAvailable(loader) != null;
    }
}

