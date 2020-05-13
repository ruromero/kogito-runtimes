/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.tests;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class MilestoneTest extends AbstractCodegenTest {

    @Test
    public void testSimpleMilestone() throws Exception {
        
        Application app = generateCodeProcessesOnly("cases/SimpleMilestone.bpmn");
        assertThat(app).isNotNull();
                
        Process<? extends Model> p = app.processes().processById("TestCase.SimpleMilestone");
        
        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);
        ProcessInstance<?> processInstance = p.createInstance(m);

        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_PENDING);
        processInstance.complete();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_PENDING);

        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        processInstance.complete();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }
}
