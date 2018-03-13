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

import static org.junit.Assert.assertNotNull;

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
import org.junit.Test;

/**
 * Test property using Person.
 */
public class TestMetaInvoke {

    @Test
    public void test_method_call_compiles() {
        @SuppressWarnings("unchecked")
        AbstractResult.Meta<Address> a = AbstractResult.meta();
        assertNotNull(a);
        
        AbstractResult.Meta<Address> a2 = AbstractResult.metaAbstractResult(Address.class);
        assertNotNull(a2);
        
        Address.Meta b = Address.meta();
        assertNotNull(b);
        
        AddressResult.Meta c = AddressResult.meta();
        assertNotNull(c);
        
        CompanyAddress.Meta d = CompanyAddress.meta();
        assertNotNull(d);
        
        CompanyAddressMidResult.Meta e = CompanyAddressMidResult.meta();
        assertNotNull(e);
        
        CompanyAddressResult.Meta f = CompanyAddressResult.meta();
        assertNotNull(f);
        
        @SuppressWarnings("unchecked")
        Documentation.Meta<String> g = Documentation.meta();
        assertNotNull(g);
        
        Documentation.Meta<String> g2 = Documentation.metaDocumentation(String.class);
        assertNotNull(g2);
        
        DocumentationHolder.Meta h = DocumentationHolder.meta();
        assertNotNull(h);
        
        FinalFieldBean.Meta i = FinalFieldBean.meta();
        assertNotNull(i);
        
        @SuppressWarnings("unchecked")
        GenericSubWrapper.Meta<Address> j = GenericSubWrapper.meta();
        assertNotNull(j);
        
        GenericSubWrapper.Meta<Address> j2 = GenericSubWrapper.metaGenericSubWrapper(Address.class);
        assertNotNull(j2);
        
        @SuppressWarnings("unchecked")
        GenericWrapperDocumentation.Meta<Address> k = GenericWrapperDocumentation.meta();
        assertNotNull(k);
        
        GenericWrapperDocumentation.Meta<Address> k2 = GenericWrapperDocumentation.metaGenericWrapperDocumentation(Address.class);
        assertNotNull(k2);
        
        @SuppressWarnings("unchecked")
        MidAbstractResult.Meta<Address> l = MidAbstractResult.meta();
        assertNotNull(l);
        
        MidAbstractResult.Meta<Address> l2 = MidAbstractResult.metaMidAbstractResult(Address.class);
        assertNotNull(l2);
        
        NoGenEquals.Meta m = NoGenEquals.meta();
        assertNotNull(m);
        
        NoProperties.Meta n = NoProperties.meta();
        assertNotNull(n);
        
        Pair.Meta o = Pair.meta();
        assertNotNull(o);
        
        Person.Meta p = Person.meta();
        assertNotNull(p);
        
        PersonDocumentation.Meta q = PersonDocumentation.meta();
        assertNotNull(q);
        
        RWOnlyBean.Meta r = RWOnlyBean.meta();
        assertNotNull(r);
        
        @SuppressWarnings("unchecked")
        SubPerson.Meta<String> s = SubPerson.meta();
        assertNotNull(s);
        
        SubPerson.Meta<String> s2 = SubPerson.metaSubPerson(String.class);
        assertNotNull(s2);
        
        SubWrapper.Meta t = SubWrapper.meta();
        assertNotNull(t);
        
        TweakedPair.Meta u = TweakedPair.meta();
        assertNotNull(u);
        
        ValidateBean.Meta v = ValidateBean.meta();
        assertNotNull(v);
        
        @SuppressWarnings("unchecked")
        Wrapper.Meta<Address> w = Wrapper.meta();
        assertNotNull(w);
    }

}
