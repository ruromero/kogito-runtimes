package org.kie.kogito.integrationtests;

import java.util.Arrays;
import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;

import org.kie.kogito.transport.TransportFilter;

@ApplicationScoped
public class ExampleTransportFilter implements TransportFilter {

    @Override
    public Collection<String> getValues() {
        return Arrays.asList("example1", "example2");
    }
}
