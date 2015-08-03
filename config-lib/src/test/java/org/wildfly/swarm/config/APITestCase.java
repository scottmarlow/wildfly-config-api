package org.wildfly.swarm.config;

import org.junit.Assert;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.Test;
import org.wildfly.apigen.invocation.Address;
import org.wildfly.apigen.invocation.Binding;
import org.wildfly.apigen.invocation.Subresource;
import org.wildfly.swarm.config.datasources.ConnectionProperties;
import org.wildfly.swarm.config.datasources.DataSource;

import java.io.InputStream;

/**
 * @author Heiko Braun
 * @since 29/07/15
 */
public class APITestCase {

    /**
     * This test just makes sure the generated classes can be instantiated.
     */
    @Test
    public void testClassInstances() {

        // attributes
        DataSource dataSource = new DataSource();
        dataSource.userName("john_doe")
            .password("password");

        // subresources
        ConnectionProperties prop = new ConnectionProperties();
        prop.value("foo-bar");
        dataSource.getConnectionPropertiess().add(prop);
    }

    /**
     * Verification of the binding meta on the generated classes
     */
    @Test
    public void testBindingMetaData() throws Exception {
        Indexer indexer = new Indexer();
        String className = DataSource.class.getCanonicalName().replace(".","/") + ".class";

        InputStream stream = DataSource.class.getClassLoader()
                                       .getResourceAsStream(className);
        indexer.index(stream);
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName(DotName.createSimple(DataSource.class.getCanonicalName()));

        DotName bindingMeta = DotName.createSimple(Binding.class.getCanonicalName());
        DotName addressMeta = DotName.createSimple(Address.class.getCanonicalName());
        DotName subresourceMeta = DotName.createSimple(Subresource.class.getCanonicalName());

        // verify @Address annotations are present
        clazz.annotations().keySet().contains(addressMeta);

        // verify @Binding annotations are present
        for (MethodInfo method : clazz.methods()) {
          if(method.name().startsWith("get") && !method.hasAnnotation(subresourceMeta))
          {

              Assert.assertTrue(method.hasAnnotation(bindingMeta));
          }
        }

    }

}
