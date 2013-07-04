/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.git.server.jgit;

import org.exoplatform.ide.git.shared.RmRequest;

import java.io.File;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: RmTest.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public class RmTest extends BaseTest {
    public void testRmNotCached() throws Exception {
        RmRequest req = new RmRequest(new String[]{"README.txt"});
        req.setCached(false);
        getDefaultConnection().rm(req);
        assertFalse(new File(getDefaultRepository().getWorkTree(), "README.txt").exists());
        checkNoFilesInCache(getDefaultRepository(), "README.txt");
    }

    public void testRmCached() throws Exception {
        RmRequest req = new RmRequest(new String[]{"README.txt"});
        req.setCached(true);
        getDefaultConnection().rm(req);
        assertTrue(new File(getDefaultRepository().getWorkTree(), "README.txt").exists());
        checkNoFilesInCache(getDefaultRepository(), "README.txt");
    }
}