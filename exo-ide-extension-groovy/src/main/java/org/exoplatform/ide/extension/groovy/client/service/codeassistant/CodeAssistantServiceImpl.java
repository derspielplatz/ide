/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.extension.groovy.client.service.codeassistant;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.RequestBuilder;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.ide.client.framework.codeassistant.TokenExt;
import org.exoplatform.ide.extension.groovy.client.codeassistant.autocompletion.GroovyClass;
import org.exoplatform.ide.extension.groovy.client.service.codeassistant.marshal.ClassDescriptionUnmarshaller;
import org.exoplatform.ide.extension.groovy.client.service.codeassistant.marshal.FindClassesUnmarshaller;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CodeAssistantService}
 * <br>
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 17, 2010 4:44:53 PM evgen $
 *
 */
public class CodeAssistantServiceImpl extends CodeAssistantService
{

   private static final String FIND_URL = "/ide/code-assistant/find?class=";
   
   private static final String GET_CLASS_URL = "/ide/code-assistant/class-description?fqn=";
   
   private static final String FIND_CLASS_BY_PREFIX = "/ide/code-assistant/find-by-prefix/";
   
   private static final String FIND_TYPE = "/ide/code-assistant/find-by-type/";

   private HandlerManager eventBus;

   private Loader loader;
   
   private String restServiceContext;

   public CodeAssistantServiceImpl(HandlerManager eventBus, String restServiceContext,Loader loader)
   {
      this.eventBus = eventBus;
      this.loader = loader;
      this.restServiceContext = restServiceContext;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.service.codeassistant.CodeAssistantService#findClass(java.lang.String)
    */
   @Override
   public void findClass(String className, String fileHref, TokensCallback tokensCallback)
   {
      String url = restServiceContext + FIND_URL + className;
      
      List<TokenExt> tokens = new ArrayList<TokenExt>();
      tokensCallback.setTokens(tokens);
      FindClassesUnmarshaller unmarshaller = new FindClassesUnmarshaller(tokens);
      
      AsyncRequestCallback callback = new AsyncRequestCallback(eventBus,unmarshaller, tokensCallback);
      AsyncRequest.build(RequestBuilder.GET, url, loader).header(HTTPHeader.LOCATION, fileHref).send(callback);
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.service.codeassistant.CodeAssistantService#getClassDescription(java.lang.String)
    */
   @Override
   public void getClassDescription(String fqn, String fileHref, ClassInfoCallback classInfoCallback)
   {
      String url = restServiceContext + GET_CLASS_URL + fqn;
      
      GroovyClass classInfo = new GroovyClass();
      classInfoCallback.setClassInfo(classInfo);
      ClassDescriptionUnmarshaller unmarshaller = new ClassDescriptionUnmarshaller(classInfo);
      AsyncRequestCallback callback = new AsyncRequestCallback(eventBus,unmarshaller, classInfoCallback);
      AsyncRequest.build(RequestBuilder.GET, url, loader).header(HTTPHeader.LOCATION, fileHref).send(callback);
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.service.codeassistant.CodeAssistantService#findClassesByPrefix(java.lang.String)
    */
   @Override
   public void findClassesByPrefix(String prefix, String fileHref, TokensCallback tokensCallback)
   {
      String url = restServiceContext + FIND_CLASS_BY_PREFIX + prefix + "?where=className";
      
      List<TokenExt> tokens = new ArrayList<TokenExt>();
      tokensCallback.setTokens(tokens);
      FindClassesUnmarshaller unmarshaller = new FindClassesUnmarshaller(tokens);
      
      AsyncRequestCallback callback = new AsyncRequestCallback(eventBus,unmarshaller, tokensCallback);
      AsyncRequest.build(RequestBuilder.GET, url, loader).header(HTTPHeader.LOCATION, fileHref).send(callback);
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.service.codeassistant.CodeAssistantService#fintType(org.exoplatform.ide.client.module.groovy.service.codeassistant.Types)
    */
   @Override
   public void fintType(Types type, String prefix, TokensCallback tokensCallback)
   {
      String url = restServiceContext + FIND_TYPE + type.toString();
      if(prefix != null && !prefix.isEmpty())
      {
       url += "?prefix=" + prefix;
      }
      List<TokenExt> tokens = new ArrayList<TokenExt>();
      tokensCallback.setTokens(tokens);
      FindClassesUnmarshaller unmarshaller = new FindClassesUnmarshaller(tokens);
      
      AsyncRequestCallback callback = new AsyncRequestCallback(eventBus,unmarshaller, tokensCallback);
      AsyncRequest.build(RequestBuilder.GET, url, loader).send(callback);
   }

}
