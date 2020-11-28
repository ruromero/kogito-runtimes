package org.kie.kogito.integrationtests;

import java.util.Collection;
import java.util.Collections;
import javax.enterprise.context.ApplicationScoped;

import org.kie.kogito.transport.TransportFilter;

@ApplicationScoped
public class OtherTransportFilter implements TransportFilter {

    @Override
    public Collection<String> getValues() {
        return Collections.singletonList("other");
    }
}
