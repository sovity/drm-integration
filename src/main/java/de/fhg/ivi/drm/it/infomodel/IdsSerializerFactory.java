package de.fhg.ivi.drm.it.infomodel;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class IdsSerializerFactory {

    @Singleton
    public de.fraunhofer.iais.eis.ids.jsonld.Serializer getRDFSerializer() {
        return new Serializer();
    }
}
