package com.sword.gsa.spis.scs.extracting.detect;

import com.sword.gsa.spis.scs.extracting.config.SCSServiceLoader;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.Detector;

import java.util.Collection;
import java.util.List;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.utils.ServiceLoaderUtils;

public class SCSDefaultDetector extends CompositeDetector {
    private static final long serialVersionUID = -8170114575326908027L;
    private final transient SCSServiceLoader loader;

    private static List<Detector> getDefaultDetectors(MimeTypes types, SCSServiceLoader loader) {
        List<Detector> detectors = loader.loadStaticServiceProviders(Detector.class);
        ServiceLoaderUtils.sortLoadedClasses(detectors);
        detectors.add(types);
        return detectors;
    }

    public SCSDefaultDetector(MimeTypes types, SCSServiceLoader loader, Collection<Class<? extends Detector>> excludeDetectors) {
        super(types.getMediaTypeRegistry(), getDefaultDetectors(types, loader), excludeDetectors);
        this.loader = loader;
    }

    public SCSDefaultDetector(MimeTypes types, SCSServiceLoader loader) {
        this(types, loader, (Collection)null);
    }

    public SCSDefaultDetector(MimeTypes types, ClassLoader loader) {
        this(types, new SCSServiceLoader(loader));
    }

    public SCSDefaultDetector(ClassLoader loader) {
        this(MimeTypes.getDefaultMimeTypes(), loader);
    }

    public SCSDefaultDetector(MimeTypes types) {
        this(types, new SCSServiceLoader());
    }

    public SCSDefaultDetector() {
        this(MimeTypes.getDefaultMimeTypes());
    }

    public List<Detector> getDetectors() {
        if(this.loader != null) {
            List<Detector> detectors = this.loader.loadDynamicServiceProviders(Detector.class);
            detectors.addAll(super.getDetectors());
            return detectors;
        } else {
            return super.getDetectors();
        }
    }
}
