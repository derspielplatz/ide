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
package org.exoplatform.ide.client.gadget;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import org.exoplatform.gwtframework.commons.loader.EmptyLoader;
import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.client.AbstractGwtTest;
import org.exoplatform.ide.client.framework.vfs.File;
import org.exoplatform.ide.client.framework.vfs.FileContentSaveCallback;
import org.exoplatform.ide.client.framework.vfs.NodeTypeUtil;
import org.exoplatform.ide.client.framework.vfs.VirtualFileSystem;
import org.exoplatform.ide.client.module.vfs.webdav.WebDavVirtualFileSystem;
import org.exoplatform.ide.extension.gadget.client.service.DeployUndeployGadgetCallback;
import org.exoplatform.ide.extension.gadget.client.service.GadgetService;
import org.exoplatform.ide.extension.gadget.client.service.GadgetServiceImpl;
import org.exoplatform.ide.extension.gadget.client.service.SecurityTokenCallback;
import org.exoplatform.ide.extension.gadget.client.service.TokenRequest;
import org.exoplatform.ide.extension.gadget.client.service.TokenResponse;

import java.util.HashMap;

/**
 * Created by The eXo Platform SAS.
 *	
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id:   ${date} ${time}
 *
 */
public class GwtTestGadgetService extends AbstractGwtTest
{
   private HandlerManager eventBus;

   private Loader loader = new EmptyLoader();

   private final int DELAY_TEST = 5000;

   private String fileURL;

   private String context;

   private String publicContext = "/rest";

   private String gadgetServer;

   /**
    * @see com.google.gwt.junit.client.GWTTestCase#gwtSetUp()
    */
   @Override
   protected void gwtSetUp() throws Exception
   {
      super.gwtSetUp();
      eventBus = new HandlerManager(null);
      fileURL = "http://" + Window.Location.getHost() + "/ideall/jcr/repository/dev-monit/";
      context = "http://" + Window.Location.getHost() + "/ideall/rest/private";
      gadgetServer = "http://" + Window.Location.getHost() + "/ide/gadgets/";
      new GadgetServiceImpl(eventBus, loader, context, gadgetServer, publicContext);
   }

   /**
    * Test getting security token.
    */
   public void testGetSecurityToken()
   {
      final String owner = "root";
      final String viewer = "root";
      final Long moduleId = 0L;
      final String container = "default";
      final String domain = null;

      final File gadget = createGadget();

      VirtualFileSystem.getInstance().saveContent(gadget, null, new FileContentSaveCallback(eventBus)
      {
         public void onResponseReceived(Request request, Response response)
         {
            assertNotNull(this.getFile());
            assertEquals(this.getFile().getContent(), gadget.getContent());
            String href = this.getFile().getHref();
            TokenRequest tokenRequest = new TokenRequest(URL.encode(href), owner, viewer, moduleId, container, domain);
            GadgetService.getInstance().getSecurityToken(tokenRequest, new SecurityTokenCallback()
            {
               
               @Override
               public void onResponseReceived(Request request, Response response)
               {
                  TokenResponse tokenResponse = this.getTokenResponse();
                  assertEquals(moduleId, tokenResponse.getModuleId());
                  assertEquals(gadget.getHref(), tokenResponse.getGadgetURL());
                  assertNotNull(tokenResponse.getSecurityToken());
                  finishTest();
               }
               
               @Override
               public void handleError(Throwable exc)
               {
                  fail(exc.getMessage());
               }
            });
         }
      });

      delayTestFinish(DELAY_TEST);
   }

   /**
    * Test getting gadget's metadata.
    */
  /*
   *TODO
   *  public void testGetMetaData()
   {
      final String owner = "root";
      final String viewer = "root";
      final Long moduleId = 0L;
      final String container = "default";
      final String domain = null;

      final File gadget = createGadget();

      eventBus.addHandler(FileContentSavedEvent.TYPE, new FileContentSavedHandler()
      {
         public void onFileContentSaved(FileContentSavedEvent event)
         {
            assertNotNull(event.getFile());
            assertEquals(event.getFile().getContent(), gadget.getContent());
            String href = event.getFile().getHref();
            TokenRequest tokenRequest = new TokenRequest(URL.encode(href), owner, viewer, moduleId, container, domain);
            GadgetService.getInstance().getSecurityToken(tokenRequest);
         }
      });

      eventBus.addHandler(SecurityTokenRecievedEvent.TYPE, new SecurityTokenRecievedHandler()
      {
         public void onSecurityTokenRecieved(SecurityTokenRecievedEvent securityTokenRecievedEvent)
         {
            GadgetService.getInstance().getGadgetMetadata(securityTokenRecievedEvent.getTokenResponse());
         }

      });

      eventBus.addHandler(ExceptionThrownEvent.TYPE, new ExceptionThrownHandler()
      {
         public void onError(ExceptionThrownEvent event)
         {
            fail(event.getErrorMessage());
         }
      });

      eventBus.addHandler(GadgetMetadaRecievedEvent.TYPE, new GadgetMetadaRecievedHandler()
      {
         public void onMetadataRecieved(GadgetMetadaRecievedEvent event)
         {
            finishTest();
         }
      });
      VirtualFileSystem.getInstance().saveContent(gadget);
      delayTestFinish(DELAY_TEST);
   }
*/
   /**
    * Test deploying gadget.
    */
   public void testDeployGadget()
   {
      final File gadget = createGadget();

      VirtualFileSystem.getInstance().saveContent(gadget, null, new FileContentSaveCallback(eventBus)
      {
         
         public void onResponseReceived(Request request, Response response)
         {
            String href = this.getFile().getHref();
            GadgetService.getInstance().deployGadget(href, new DeployUndeployGadgetCallback()
            {
               
               @Override
               public void onResponseReceived(Request request, Response response)
               {
                  finishTest();
               }
               
               @Override
               public void handleError(Throwable exc)
               {
                  fail(exc.getMessage());
               }
            });
         }
      });
      delayTestFinish(DELAY_TEST);
   }

   /**
    * Test undeploying deployed gadget.
    */
   public void testUndeployGadget()
   {
      final File gadget = createGadget();

      VirtualFileSystem.getInstance().saveContent(gadget, null, new FileContentSaveCallback(eventBus)
      {
         
         public void onResponseReceived(Request request, Response response)
         {
            String href = this.getFile().getHref();
            GadgetService.getInstance().deployGadget(href, new DeployUndeployGadgetCallback()
            {
               
               @Override
               public void onResponseReceived(Request request, Response response)
               {
                  GadgetService.getInstance().undeployGadget(gadget.getContent(), new DeployUndeployGadgetCallback()
                  {
                     
                     @Override
                     public void onResponseReceived(Request request, Response response)
                     {
                        finishTest();
                     }
                     
                     @Override
                     public void handleError(Throwable exc)
                     {
                        fail(exc.getMessage());
                     }
                  });
               }
               
               @Override
               public void handleError(Throwable exc)
               {
                  fail();
               }
            });
         }
      });
      delayTestFinish(DELAY_TEST);
   }

   /**
    * Test deploy the gadget that doesn't exist.
    */
   public void testDeployNotExitedGadget()
   {
      String href = fileURL + "nogadget";

      GadgetService.getInstance().deployGadget(href, new DeployUndeployGadgetCallback()
      {
         
         @Override
         public void onResponseReceived(Request request, Response response)
         {
            fail();
            
         }
         
         @Override
         public void onError(Request request, Throwable exception)
         {
            fail(exception.getMessage());
         }
         
         @Override
         public void onUnsuccess(Throwable exception)
         {
            assertNotNull(exception);
            finishTest();
         }
         
         @Override
         public void handleError(Throwable exc)
         {
            finishTest();
         }
      });
      delayTestFinish(DELAY_TEST);
   }

   /**
    * Test undeploy the gadget that doesn't exist.
    */
   public void testUndeployNotExitedGadget()
   {
      String href = fileURL + "nogadget";

      GadgetService.getInstance().undeployGadget(href, new DeployUndeployGadgetCallback()
      {
         @Override
         public void onResponseReceived(Request request, Response response)
         {
            fail();
         }
         
         @Override
         public void onError(Request request, Throwable exception)
         {
            fail(exception.getMessage());
         }
         
         @Override
         public void onUnsuccess(Throwable exception)
         {
            assertNotNull(exception);
            finishTest();
         }
         
         @Override
         public void handleError(Throwable exc)
         {
            finishTest();
         }
      });
      delayTestFinish(DELAY_TEST);
   }

   /**
    * Creates new file with gadget mime type.
    * 
    * @return File gadget
    */
   private File createGadget()
   {
      new WebDavVirtualFileSystem(eventBus, new EmptyLoader(), new HashMap<String, String>(), "/rest");
      final String fileContent =
         "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<Module>" + "<ModulePrefs title=\"Hello World!\" />"
            + "<Content type=\"html\">" + "<![CDATA[ Hello, world!Hello, world!]]></Content></Module>";
      String fileName = "gadget";

      final File file = new File(fileURL + fileName);
      file.setContentType(MimeType.GOOGLE_GADGET);
      file.setJcrContentNodeType(NodeTypeUtil.getContentNodeType(MimeType.GOOGLE_GADGET));
      file.setNewFile(true);
      file.setContent(fileContent);
      file.setContentChanged(true);
      return file;
   }
}
