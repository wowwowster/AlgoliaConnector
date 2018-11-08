package com.sword.gsa.spis.scs.extracting.detect;

import com.sword.gsa.spis.scs.extracting.config.SCSServiceLoader;
import org.apache.tika.detect.CompositeEncodingDetector;
import org.apache.tika.detect.EncodingDetector;

import java.util.Collection;


public class SCSDefaultEncodingDetector extends CompositeEncodingDetector {
    public SCSDefaultEncodingDetector() {
        this(new SCSServiceLoader(SCSDefaultEncodingDetector.class.getClassLoader()));
    }

    public SCSDefaultEncodingDetector(SCSServiceLoader loader) {
        super(loader.loadServiceProviders(EncodingDetector.class));
    }

    public SCSDefaultEncodingDetector(SCSServiceLoader loader, Collection<Class<? extends EncodingDetector>> excludeEncodingDetectors) {
        super(loader.loadServiceProviders(EncodingDetector.class), excludeEncodingDetectors);
    }
}

