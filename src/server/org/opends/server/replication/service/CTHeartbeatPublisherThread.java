/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.server.replication.service;

import org.opends.server.api.DirectoryThread;
import static org.opends.server.loggers.debug.DebugLogger.*;

import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.replication.common.ChangeNumber;
import org.opends.server.replication.protocol.ChangeTimeHeartbeatMsg;
import org.opends.server.replication.protocol.ProtocolSession;
import org.opends.server.util.TimeThread;

import java.io.IOException;

/**
 * This thread publishes a heartbeat message on a given protocol session at
 * regular intervals when there are no other replication messages being
 * published.
 */
public class CTHeartbeatPublisherThread extends DirectoryThread
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * For test purposes only to simulate loss of heartbeats.
   */
  static private boolean heartbeatsDisabled = false;

  /**
   * The session on which heartbeats are to be sent.
   */
  private ProtocolSession session;

  /**
   * The time in milliseconds between heartbeats.
   */
  private long heartbeatInterval;
  private int serverId;

  /**
   * Set this to stop the thread.
   */
  private Boolean shutdown = false;
  private final Object shutdown_lock = new Object();

  /**
   * Create a heartbeat thread.
   * @param threadName The name of the heartbeat thread.
   * @param session The session on which heartbeats are to be sent.
   * @param heartbeatInterval The interval between heartbeats sent
   *                          (in milliseconds).
   * @param serverId2 The serverId of the sender domain.
   */
  public CTHeartbeatPublisherThread(String threadName, ProtocolSession session,
                  long heartbeatInterval, int serverId2)
  {
    super(threadName);
    this.session = session;
    this.heartbeatInterval = heartbeatInterval;
    this.serverId = serverId2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run()
  {
    try
    {
      if (debugEnabled())
      {
        TRACER.debugInfo(getName() + " is starting, interval is %d",
                  heartbeatInterval);
      }

      while (!shutdown)
      {
        long now = System.currentTimeMillis();
        ChangeTimeHeartbeatMsg ctHeartbeatMsg =
         new ChangeTimeHeartbeatMsg(
             new ChangeNumber(TimeThread.getTime(),0, serverId));

        if (now > session.getLastPublishTime() + heartbeatInterval)
        {
          if (!heartbeatsDisabled)
          {
            session.publish(ctHeartbeatMsg);
          }
        }

        try
        {
          long sleepTime = session.getLastPublishTime() +
              heartbeatInterval - now;
          if (sleepTime <= 0)
          {
            sleepTime = heartbeatInterval;
          }

          synchronized (shutdown_lock)
          {
            if (!shutdown)
            {
              shutdown_lock.wait(sleepTime);
            }
          }
        }
        catch (InterruptedException e)
        {
          // Keep looping.
        }
      }
    }
    catch (IOException e)
    {
      if (debugEnabled())
      {
        TRACER.debugInfo(getName() + "could not send a heartbeat." +
            e.getMessage() + e.toString());
      }
      // This will be caught in another thread.
    }
    finally
    {
      if (debugEnabled())
      {
        TRACER.debugInfo(getName()+" is exiting.");
      }
    }
  }


  /**
   * Call this method to stop the thread.
   * This method is blocking until the thread has stopped.
   */
  public void shutdown()
  {
    synchronized (shutdown_lock)
    {
      shutdown = true;
      shutdown_lock.notifyAll();
    }
  }


  /**
   * For testing purposes only to simulate loss of heartbeats.
   * @param heartbeatsDisabled Set true to prevent heartbeats from being sent.
   */
  public static void setHeartbeatsDisabled(boolean heartbeatsDisabled)
  {
    CTHeartbeatPublisherThread.heartbeatsDisabled = heartbeatsDisabled;
  }
}
