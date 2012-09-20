/**
 * Copyright (C) 2009 eXo Platform SAS.
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
 *
 */

package org.exoplatform.ide.rest;

import org.exoplatform.ide.commons.exception.JobNotFoundException;
import org.exoplatform.ide.commons.exception.ServerException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;

/**
 * Created by The eXo Platform SAS .
 * 
 * Wrapper under RequestBuilder to simplify the stuffs
 * 
 * @version $Id: $
 */

public class AsyncRequest
{

   protected final RequestBuilder builder;

   protected AsyncRequestLoader loader;

   protected boolean async;

   protected int delay = 5000;

   protected RequestStatusHandler handler;

   protected String requestStatusUrl;

   private AsyncRequestCallback<?> callback;

   protected AsyncRequest(RequestBuilder builder)
   {
      this.builder = builder;
      this.loader = new EmptyLoader();
      async = false;
   }

   protected AsyncRequest(RequestBuilder builder, boolean async)
   {
      this(builder);
      this.async = async;
   }

   private static native String getProxyServiceContext() /*-{
                                                         return $wnd.proxyServiceContext;
                                                         }-*/;

   private static String getCheckedURL(String url)
   {
      String proxyServiceContext = getProxyServiceContext();
      if (proxyServiceContext == null || "".equals(proxyServiceContext))
      {
         return url;
      }

      if (!(url.startsWith("http://") || url.startsWith("https://")))
      {
         return url;
      }

      String currentHost = Location.getProtocol() + "//" + Location.getHost();
      if (url.startsWith(currentHost))
      {
         return url;
      }

      return proxyServiceContext + "?url=" + URL.encodeQueryString(url);
   }

   public static final AsyncRequest build(Method method, String url)
   {
      return build(method, url, false);
   }

   /**
    * Build AsyncRequest with run REST Service in async mode
    * 
    * @param method HTTP method
    * @param url of service
    * @param async is run async
    * @return instance of {@link AsyncRequest}
    */
   public static final AsyncRequest build(Method method, String url, boolean async)
   {
      if (async)
      {
         if (url.contains("?"))
         {
            url += "&async=true";
         }
         else
         {
            url += "?async=true";
         }
      }
      String checkedURL = getCheckedURL(url);
      return new AsyncRequest(new RequestBuilder(method, checkedURL), async);
   }

   public final AsyncRequest header(String header, String value)
   {
      builder.setHeader(header, value);
      return this;
   }

   public final AsyncRequest user(String user)
   {
      builder.setUser(user);
      return this;
   }

   public final AsyncRequest password(String password)
   {
      builder.setPassword(password);
      return this;
   }

   public final AsyncRequest data(String requestData)
   {
      builder.setRequestData(requestData);
      return this;
   }

   public final AsyncRequest loader(AsyncRequestLoader loader)
   {
      this.loader = loader;
      return this;
   }

   /**
    * Set delay between requests to async REST Service<br>
    * (Default: 5000)
    * 
    * @param delay in milliseconds
    * @return
    */
   public final AsyncRequest delay(int delay)
   {
      this.delay = delay;
      return this;
   }

   /**
    * Set handler of async REST Service status
    * 
    * @param handler
    * @return
    */
   public final AsyncRequest requestStatusHandler(RequestStatusHandler handler)
   {
      this.handler = handler;
      return this;
   }

   private void sendRequest(AsyncRequestCallback<?> callback) throws RequestException
   {
      callback.setLoader(loader);
      callback.setRequest(this);
      builder.setCallback(callback);
      loader.show();
      builder.send();
   }

   public final void send(AsyncRequestCallback<?> callback) throws RequestException
   {
      this.callback = callback;
      if (async)
      {
         sendRequest(initCallback);
      }
      else
      {
         sendRequest(callback);
      }
   }

   public AsyncRequestCallback<?> getCallback()
   {
      return callback;
   }

   private AsyncRequestCallback<StringBuilder> initCallback = new AsyncRequestCallback<StringBuilder>(
      new LocationUnmarshaller(new StringBuilder()))
   {
      {
         setSuccessCodes(new int[]{Response.SC_ACCEPTED});
      }

      @Override
      protected void onSuccess(StringBuilder result)
      {
         requestStatusUrl = result.toString();
         if (handler != null)
            handler.requestInProgress(requestStatusUrl);

         requesTimer.schedule(delay);
      }

      @Override
      protected void onFailure(Throwable exception)
      {
         if (handler != null)
            handler.requestError(requestStatusUrl, exception);

         callback.onError(null, exception);
      }
   };

   private Timer requesTimer = new Timer()
   {

      @Override
      public void run()
      {
         RequestBuilder request = new RequestBuilder(RequestBuilder.GET, requestStatusUrl);
         request.setCallback(new RequestCallback()
         {

            public void onResponseReceived(Request request, Response response)
            {
               if (Response.SC_NOT_FOUND == response.getStatusCode())
               {
                  callback.onError(request, new JobNotFoundException(response));
                  if (handler != null)
                  {
                     handler.requestError(requestStatusUrl, new JobNotFoundException(response));
                  }
               }
               else if (response.getStatusCode() != Response.SC_ACCEPTED)
               {
                  callback.onResponseReceived(request, response);
                  if (handler != null)
                  {
                     // check is response successfull, for correct handling failed responses
                     if (callback.isSuccessful(response))
                        handler.requestFinished(requestStatusUrl);
                     else
                        handler.requestError(requestStatusUrl, new ServerException(response));
                  }
               }
               else
               {
                  if (handler != null)
                     handler.requestInProgress(requestStatusUrl);

                  requesTimer.schedule(delay);
               }
            }

            public void onError(Request request, Throwable exception)
            {
               if (handler != null)
                  handler.requestError(requestStatusUrl, exception);

               callback.onError(request, exception);
            }
         });
         try
         {
            request.send();
         }
         catch (RequestException e)
         {
            e.printStackTrace();
            if (handler != null)
               handler.requestError(requestStatusUrl, e);
            callback.onFailure(e);
         }
      }
   };

   private class EmptyLoader implements AsyncRequestLoader
   {
      public void hide()
      {
      }

      public void show()
      {
      }
   }

}
