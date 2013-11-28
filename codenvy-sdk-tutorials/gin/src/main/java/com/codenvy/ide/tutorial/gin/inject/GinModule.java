package com.codenvy.ide.tutorial.gin.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.api.parts.ConsolePart;
import com.codenvy.ide.tutorial.gin.annotation.*;
import com.codenvy.ide.tutorial.gin.factory.MyFactory;
import com.codenvy.ide.tutorial.gin.factory.assited.SomeImplementationWithAssistedParam;
import com.codenvy.ide.tutorial.gin.factory.assited.SomeInterface;
import com.codenvy.ide.tutorial.gin.part.TutorialHowToView;
import com.codenvy.ide.tutorial.gin.part.TutorialHowToViewImpl;
import com.codenvy.ide.tutorial.gin.sample.MyImplementation;
import com.codenvy.ide.tutorial.gin.sample.MyInterface;
import com.codenvy.ide.tutorial.gin.singleton.MySingletonImplementation;
import com.codenvy.ide.tutorial.gin.singleton.MySingletonInterface;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@ExtensionGinModule
public class GinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(TutorialHowToView.class).to(TutorialHowToViewImpl.class);

        bind(MyInterface.class).to(MyImplementation.class);
        bind(MySingletonInterface.class).to(MySingletonImplementation.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().implement(SomeInterface.class, SomeImplementationWithAssistedParam.class)
                                             .build(MyFactory.class));
        bind(String.class).annotatedWith(MyString.class).toProvider(MyStringProvider.class).in(Singleton.class);
        bind(SimpleInterface.class).annotatedWith(SimpleClass.class).to(SimpleImplementation.class).in(Singleton.class);
    }

    @Provides
    @Named("myString")
    @Singleton
    protected String provideMyString() {
        return "my string value from named annotation";
    }

    @Provides
    @Singleton
    protected String provideStringValue(ConsolePart console) {
        console.print("initialize string value in gin module");
        return "my string value from provider method";
    }
}