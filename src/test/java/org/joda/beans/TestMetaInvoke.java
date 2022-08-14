/*
 *  Copyright 2001-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.beans.sample.AbstractResult;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.AddressResult;
import org.joda.beans.sample.CompanyAddress;
import org.joda.beans.sample.CompanyAddressMidResult;
import org.joda.beans.sample.CompanyAddressResult;
import org.joda.beans.sample.Documentation;
import org.joda.beans.sample.DocumentationHolder;
import org.joda.beans.sample.FinalFieldBean;
import org.joda.beans.sample.GenericSubWrapper;
import org.joda.beans.sample.GenericWrapperDocumentation;
import org.joda.beans.sample.MidAbstractResult;
import org.joda.beans.sample.NoGenEquals;
import org.joda.beans.sample.NoProperties;
import org.joda.beans.sample.Pair;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.PersonDocumentation;
import org.joda.beans.sample.RWOnlyBean;
import org.joda.beans.sample.SubPerson;
import org.joda.beans.sample.SubWrapper;
import org.joda.beans.sample.TweakedPair;
import org.joda.beans.sample.ValidateBean;
import org.joda.beans.sample.Wrapper;
import org.junit.jupiter.api.Test;

/**
 * Test property using Person.
 */
public class TestMetaInvoke {

    @Test
    public void test_method_call_compiles() {
        @SuppressWarnings("unchecked")
        AbstractResult.Meta<Address> a = AbstractResult.meta();
        assertThat(a).isNotNull();
        
        AbstractResult.Meta<Address> a2 = AbstractResult.metaAbstractResult(Address.class);
        assertThat(a2).isNotNull();
        
        Address.Meta b = Address.meta();
        assertThat(b).isNotNull();
        
        AddressResult.Meta c = AddressResult.meta();
        assertThat(c).isNotNull();
        
        CompanyAddress.Meta d = CompanyAddress.meta();
        assertThat(d).isNotNull();
        
        CompanyAddressMidResult.Meta e = CompanyAddressMidResult.meta();
        assertThat(e).isNotNull();
        
        CompanyAddressResult.Meta f = CompanyAddressResult.meta();
        assertThat(f).isNotNull();
        
        @SuppressWarnings("unchecked")
        Documentation.Meta<String> g = Documentation.meta();
        assertThat(g).isNotNull();
        
        Documentation.Meta<String> g2 = Documentation.metaDocumentation(String.class);
        assertThat(g2).isNotNull();
        
        DocumentationHolder.Meta h = DocumentationHolder.meta();
        assertThat(h).isNotNull();
        
        FinalFieldBean.Meta i = FinalFieldBean.meta();
        assertThat(i).isNotNull();
        
        @SuppressWarnings("unchecked")
        GenericSubWrapper.Meta<Address> j = GenericSubWrapper.meta();
        assertThat(j).isNotNull();
        
        GenericSubWrapper.Meta<Address> j2 = GenericSubWrapper.metaGenericSubWrapper(Address.class);
        assertThat(j2).isNotNull();
        
        @SuppressWarnings("unchecked")
        GenericWrapperDocumentation.Meta<Address> k = GenericWrapperDocumentation.meta();
        assertThat(k).isNotNull();
        
        GenericWrapperDocumentation.Meta<Address> k2 = GenericWrapperDocumentation.metaGenericWrapperDocumentation(Address.class);
        assertThat(k2).isNotNull();
        
        @SuppressWarnings("unchecked")
        MidAbstractResult.Meta<Address> l = MidAbstractResult.meta();
        assertThat(l).isNotNull();
        
        MidAbstractResult.Meta<Address> l2 = MidAbstractResult.metaMidAbstractResult(Address.class);
        assertThat(l2).isNotNull();
        
        NoGenEquals.Meta m = NoGenEquals.meta();
        assertThat(m).isNotNull();
        
        NoProperties.Meta n = NoProperties.meta();
        assertThat(n).isNotNull();
        
        Pair.Meta o = Pair.meta();
        assertThat(o).isNotNull();
        
        Person.Meta p = Person.meta();
        assertThat(p).isNotNull();
        
        PersonDocumentation.Meta q = PersonDocumentation.meta();
        assertThat(q).isNotNull();
        
        RWOnlyBean.Meta r = RWOnlyBean.meta();
        assertThat(r).isNotNull();
        
        @SuppressWarnings("unchecked")
        SubPerson.Meta<String> s = SubPerson.meta();
        assertThat(s).isNotNull();
        
        SubPerson.Meta<String> s2 = SubPerson.metaSubPerson(String.class);
        assertThat(s2).isNotNull();
        
        SubWrapper.Meta t = SubWrapper.meta();
        assertThat(t).isNotNull();
        
        TweakedPair.Meta u = TweakedPair.meta();
        assertThat(u).isNotNull();
        
        ValidateBean.Meta v = ValidateBean.meta();
        assertThat(v).isNotNull();
        
        @SuppressWarnings("unchecked")
        Wrapper.Meta<Address> w = Wrapper.meta();
        assertThat(w).isNotNull();
    }

}
