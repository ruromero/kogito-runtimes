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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.services.identity.StaticIdentityProvider;

import static org.assertj.core.api.Assertions.assertThat;


public class AdHocSubProcessTest extends AbstractCodegenTest {

    private Policy<?> securityPolicy = SecurityPolicy.of(new StaticIdentityProvider("role"));

    @Test
    public void testSimpleAdHoc() throws Exception {
        Application app = generateCodeProcessesOnly("cases/SimpleAdHoc.bpmn");
        assertThat(app).isNotNull();
                
        Process<? extends Model> p = app.processes().processById("TestCase.SimpleAdHoc");
        
        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);
        
        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
//        processInstance.stages();
        assertThat(processInstance.workItems().size()).isEqualTo(1);
        WorkItem workItem = processInstance.workItems(securityPolicy).get(0);

        Map<String, Object> variables = new HashMap<>();
        variables.put("updatedName", "Paul");
        processInstance.completeWorkItem(workItem.getId(), variables, securityPolicy);

        assertThat(((Model)processInstance.variables()).toMap().get("driver")).isEqualTo("Paul");
        assertThat(processInstance.workItems().size()).isEqualTo(0);
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        processInstance.complete();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testMultipleAdHoc() throws Exception {
        Application app = generateCodeProcessesOnly("cases/MultipleAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.MultipleAdHoc");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("val", 0);
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();

        assertThat(((Model)processInstance.variables()).toMap().get("val")).isEqualTo(0);
        assertThat(processInstance.workItems().size()).isEqualTo(0);
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        processInstance.complete();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testCaseFile() throws Exception {
                Application app = generateCodeProcessesOnly("cases/CaseFileAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.CaseFileAdHoc");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("caseFile_name", "Foo");
        parameters.put("name", "Bar");
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);

        processInstance.complete();
        //Process variable should not be updated in the sub-process
        assertThat(((Model)processInstance.variables()).toMap().get("name")).isEqualTo("Bar-process-subprocess");
        //CaseFile variable should be updated in the sub-process
        assertThat(((Model)processInstance.variables()).toMap().get("caseFile_name")).isEqualTo("Foo-process-subprocess");
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }
}
